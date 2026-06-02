package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.AutoBidConfig;
import com.auction.share.DTO.CancelAutoBidRequest;
import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.DTO.RegisterAutoBidRequest;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.exceptions.ConcurrentBidException;
import com.auction.share.exceptions.InsufficientBalanceException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dịch vụ xử lý logic đặt giá tự động (Proxy Bidding giống eBay). */
public class AutoBidService  {
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoBidService.class);

    private final DataSource dataSource;
    private final AutoBidRegistry registry;
    private final BidService bidService;
    private final UserDAO userDAO;
    private final AuctionDAO auctionDAO;

    // Quản lý Lock theo từng mã phiên đấu giá (AuctionId)
    private final ConcurrentHashMap<String, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    public AutoBidService(
            DataSource dataSource,
            AutoBidRegistry registry,
            BidService bidService,
            UserDAO userDAO,
            AuctionDAO auctionDAO) {
        this.dataSource = dataSource;
        this.registry = registry;
        this.bidService = bidService;
        this.userDAO = userDAO;
        this.auctionDAO = auctionDAO;
    }

    public void shutdown() {
        // No-op (ExecutorService removed in favor of ReentrantLocks)
    }

    public boolean register(RegisterAutoBidRequest request) throws SQLException, ValidationException {
        validateConfig(request.getMaxBid(), request.getIncrement());

        try (Connection conn = dataSource.getConnection()) {
            Auction auction = auctionDAO.findById(conn, request.getAuctionId());
            if (auction == null || !auction.isRunning()) {
                throw new ValidationException(auction == null ? "Auction not found." : "Auction is not running.");
            }
            if (request.getMaxBid() <= auction.getCurrentHighestBid()) {
                throw new ValidationException("Auto-bid max must be higher than current highest bid.");
            }

            User bidderUser = userDAO.findById(conn, request.getUserId());
            if (!(bidderUser instanceof Bidder)) {
                throw new ValidationException("User is not a bidder.");
            }
        }

        AutoBidConfig config =
                new AutoBidConfig(
                        request.getUserId(),
                        request.getAuctionId(),
                        request.getMaxBid(),
                        request.getIncrement(),
                        LocalDateTime.now());
        registry.register(config);
        triggerAutoBid(request.getAuctionId(), request.getUserId());
        return true;
    }

    public boolean cancel(CancelAutoBidRequest request) {
        return registry.cancel(request.getAuctionId(), request.getUserId());
    }

    /** Điểm kích hoạt đặt giá tự động đồng bộ có sử dụng Lock cục bộ theo từng Auction */
    public void triggerAutoBid(String auctionId, String lastBidderId) {
        ReentrantLock lock = auctionLocks.computeIfAbsent(auctionId, ignored -> new ReentrantLock());
        lock.lock();
        try {
            processAutoBid(auctionId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Thuật toán Proxy Bidding trung tâm xử lý đấu giá tự động.
     */
    public void processAutoBid(String auctionId) {
        while (true) {
            Auction auction;
            List<AutoBidConfig> configs;
            String currentHighestBidderId;

            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                try {
                    // 1. Lấy thông tin đấu giá hiện tại từ CSDL
                    auction = auctionDAO.findById(conn, auctionId);
                    if (auction == null || !auction.isRunning()) {
                        conn.rollback();
                        return;
                    }

                    currentHighestBidderId = auction.getHighestBidder() != null 
                            ? auction.getHighestBidder().getId() : null;

                    // 2. Lấy toàn bộ cấu hình Auto-bid của phiên này và lọc ra các cấu hình hợp lệ
                    configs = getAndValidateConfigs(conn, auctionId, auction.getCurrentHighestBid(), auction.getBidStep(), currentHighestBidderId);
                    conn.commit();
                } catch (SQLException e) {
                    conn.rollback();
                    throw e;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                LOGGER.error("Database error in processAutoBid for auction {}", auctionId, e);
                return;
            }

            if (configs.isEmpty()) {
                return;
            }

            // Sắp xếp giảm dần theo maxBid. Nếu bằng nhau, ưu tiên người đăng ký trước (registeredAt tăng dần)
            configs.sort((c1, c2) -> {
                int cmp = Double.compare(c2.getMaxBid(), c1.getMaxBid());
                if (cmp != 0) return cmp;
                return c1.getRegisteredAt().compareTo(c2.getRegisteredAt());
            });

            AutoBidConfig winnerConfig = configs.get(0);
            String winnerBidderId = winnerConfig.getBidderId();

            double finalPrice;
            if (configs.size() >= 2) {
                // Kịch bản có nhiều hơn 1 người cấu hình Auto-bid: Đấu giá ngầm quyết định giá cuối
                AutoBidConfig runnerUpConfig = configs.get(1);
                finalPrice = Math.min(winnerConfig.getMaxBid(), runnerUpConfig.getMaxBid() + winnerConfig.getIncrement());
                finalPrice = Math.max(finalPrice, auction.getCurrentHighestBid() + winnerConfig.getIncrement());
                finalPrice = Math.max(finalPrice, auction.getCurrentHighestBid() + auction.getBidStep());
                finalPrice = Math.min(finalPrice, winnerConfig.getMaxBid());
            } else {
                // Kịch bản chỉ có duy nhất 1 người cấu hình Auto-bid
                if (winnerBidderId.equals(currentHighestBidderId)) {
                    // Đã là người dẫn đầu, không cần tự nâng giá chống lại chính mình
                    return;
                }
                finalPrice = auction.getCurrentHighestBid() + winnerConfig.getIncrement();
                finalPrice = Math.max(finalPrice, auction.getCurrentHighestBid() + auction.getBidStep());
                finalPrice = Math.min(finalPrice, winnerConfig.getMaxBid());
            }

            // 3. Thực hiện đặt bid sử dụng phương thức đặt bid có sẵn ở BidService
            try {
                bidService.placeBid(new PlaceBidRequest(auctionId, winnerBidderId, finalPrice), false);
                // Đặt bid thành công! Kết thúc quá trình xử lý.
                break;
            } catch (ConcurrentBidException e) {
                // Trùng lệnh đặt giá (ai đó bid chen ngang), tiếp tục vòng lặp để tính toán lại giá
                continue;
            } catch (InsufficientBalanceException e) {
                // Người thắng bị cạn kiệt số dư khả dụng. Hủy cấu hình của họ và chạy lại vòng lặp chọn người thắng mới
                registry.cancel(auctionId, winnerBidderId);
            } catch (SQLException | ValidationException e) {
                LOGGER.error("Error placing proxy bid for auction {}", auctionId, e);
                return;
            }
        }
    }

    /** Lấy danh sách cấu hình và tự động loại bỏ (Hủy) các cấu hình không còn hợp lệ */
    private List<AutoBidConfig> getAndValidateConfigs(
            Connection conn, 
            String auctionId, 
            double currentHighestBid, 
            double bidStep,
            String currentHighestBidderId) throws SQLException {
        List<AutoBidConfig> configs = registry.getConfigs(auctionId);
        List<AutoBidConfig> validConfigs = new ArrayList<>();

        for (AutoBidConfig config : configs) {
            String bidderId = config.getBidderId();

            // Quy tắc 1: Hạn mức maxBid <= Giá cao nhất hiện tại
            if (config.getMaxBid() <= currentHighestBid) {
                registry.cancel(auctionId, bidderId);
                continue;
            }

            // Quy tắc 1.5: Max bid không đủ để vượt qua giá tối thiểu yêu cầu (current + bidStep)
            if (config.getMaxBid() < currentHighestBid + bidStep) {
                registry.cancel(auctionId, bidderId);
                continue;
            }

            // Quy tắc 2: Không đủ số dư khả dụng
            double balance = userDAO.findBalanceForUpdate(conn, bidderId);
            double reserved = auctionDAO.sumAuctionCurrentPrices(conn, bidderId, auctionId);
            double available = balance - reserved;

            double currentBid = 0.0;
            if (bidderId.equals(currentHighestBidderId)) {
                currentBid = currentHighestBid;
            }

            if (available + currentBid < currentHighestBid + Math.max(config.getIncrement(), bidStep)) {
                registry.cancel(auctionId, bidderId);
                continue;
            }

            validConfigs.add(config);
        }
        return validConfigs;
    }

    private static void validateConfig(double maxBid, double increment) throws ValidationException {
        if (maxBid <= 0) {
            throw new ValidationException("Auto-bid max must be greater than 0.");
        }
        if (increment <= 0) {
            throw new ValidationException("Auto-bid increment must be greater than 0.");
        }
    }
}

