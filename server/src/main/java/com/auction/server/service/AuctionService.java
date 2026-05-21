package com.auction.server.service;

import com.auction.server.dao.AuctionDAO;
import com.auction.server.dao.BidTransactionDAO;
import com.auction.server.dao.ItemDAO;
import com.auction.server.dao.UserDAO;
import com.auction.server.util.DatabaseConnection;
import com.auction.share.DTO.*;
import com.auction.share.enums.AuctionStatus;
import com.auction.share.enums.Category;
import com.auction.share.exceptions.ValidationException;
import com.auction.share.models.auction.Auction;
import com.auction.share.models.auction.BidTransaction;
import com.auction.share.models.item.Item;
import com.auction.share.models.user.Bidder;
import com.auction.share.models.user.Seller;
import com.auction.share.models.user.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.PriorityBlockingQueue;

public class AuctionService {
    private static final int MAX_AUTO_BID_STEPS_PER_TRIGGER = 500;

    private final AuctionDAO auctionDAO;
    private final ItemDAO itemDAO;
    private final BidTransactionDAO bidTransactionDAO;
    private final UserDAO userDAO;
    private final BidBroadcastService bidBroadcastService;
    // AUTO_BIDDING ATTRIBUTES
    // ---------  auctionID  /  danh sách những người bật auto-bid cho auction đó
    // ưu tiên maxBid > increment
    private final Map<String, PriorityBlockingQueue<AutoBidOrder>> autoBidQueues = new ConcurrentHashMap<>();
    // tránh duyệt toàn bộ heap O(n) -> O(1)
    private final Map<String, AutoBidOrder> autoBidByAuctionAndBidder = new ConcurrentHashMap<>();
    // tránh race condition khi cùng autoBid
    private final AtomicLong autoBidSequence = new AtomicLong();
    // tránh tình trạng race condition trong PriorityQueue
    private final Object autoBidLock = new Object();

    public AuctionService(
            AuctionDAO auctionDAO,
            ItemDAO itemDAO,
            BidTransactionDAO bidTransactionDAO,
            UserDAO userDAO,
            BidBroadcastService bidBroadcastService
    ) {
        this.auctionDAO = auctionDAO;
        this.itemDAO = itemDAO;
        this.bidTransactionDAO = bidTransactionDAO;
        this.userDAO = userDAO;
        this.bidBroadcastService = bidBroadcastService;
    }

    public Auction createAuction(CreateAuctionRequest req) throws SQLException, ValidationException {
        User sellerUser = userDAO.findById(req.getSellerId());
        if (!(sellerUser instanceof Seller seller)) {
            throw new ValidationException("User is not a seller.");
        }

        LocalDateTime startTime = parseDateTime(req.getStartTime());
        LocalDateTime endTime = parseDateTime(req.getEndTime());
        if (!startTime.isBefore(endTime)) {
            throw new ValidationException("Start time must be before end time.");
        }

        Category category = Category.valueOf(req.getCategory().trim().toUpperCase());
        Item item = new Item(req.getItemName(), req.getDescription(), req.getStartingPrice(), req.getSellerId(), category);
        Auction auction = new Auction(item, seller, startTime, endTime);

        itemDAO.saveItem(item);
        auctionDAO.saveAuction(auction);
        return auction;
    }

    public boolean placeBid(PlaceBidRequest req) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(req.getAuctionId());
        validateAuctionRunning(auction);

        User bidderUser = userDAO.findById(req.getBidderId());
        if (!(bidderUser instanceof Bidder bidder)) {
            throw new ValidationException("User is not a bidder.");
        }

        if (req.getAmount() <= auction.getCurrentHighestBid()) {
            throw new ValidationException("Bid amount must be higher than current highest bid.");
        }

        validateBidBudget(req.getAuctionId(), bidder.getId(), bidder.getBalance(), req.getAmount(), false);
        placeBidAndBroadcast(auction, bidder, req.getAmount());
        // trigger auto-bid engine
        // phải chạy processAutoBids() để các auto-bidder có  phản ứng và vượt giá mới
        processAutoBids(req.getAuctionId());
        return true;
    }

    public boolean setAutoBid(SetAutoBidRequest req) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(req.getAuctionId());
        validateAuctionRunning(auction);

        User bidderUser = userDAO.findById(req.getBidderId());
        if (!(bidderUser instanceof Bidder bidder)) {
            throw new ValidationException("User is not a bidder.");
        }

        if (req.getIncrement() <= 0) {
            throw new ValidationException("Auto-bid increment must be greater than 0.");
        }

        if (req.getMaxBid() <= auction.getCurrentHighestBid()) {
            throw new ValidationException("Auto-bid max must be higher than current highest bid.");
        }

        validateBidBudget(req.getAuctionId(), bidder.getId(), bidder.getBalance(), req.getMaxBid(), true);

        synchronized (autoBidLock) {
            String key = autoBidKey(req.getAuctionId(), bidder.getId());
            AutoBidOrder previous = autoBidByAuctionAndBidder.get(key);
            // disable auto-bid cũ
            if (previous != null) {
                previous.deactivate();
            }
            // tạo auto-bid mới
            AutoBidOrder order = new AutoBidOrder(
                    req.getAuctionId(),
                    bidder,
                    req.getMaxBid(),
                    req.getIncrement(),
                    autoBidSequence.incrementAndGet()
            );

            autoBidByAuctionAndBidder.put(key, order);
            // add vào heap
            // nếu auction này chưa có ai setAutoBid thì tạo PQ mới
            autoBidQueues
                    .computeIfAbsent(req.getAuctionId(), ignored -> newAutoBidQueue())
                    .offer(order); // add phần tử vào heap
        }

        processAutoBids(req.getAuctionId());
        return true;
    }

    public boolean cancelAutoBid(String auctionId, String bidderId) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(auctionId);
        validateAuctionRunning(auction);

        User bidderUser = userDAO.findById(bidderId);
        if (!(bidderUser instanceof Bidder)) {
            throw new ValidationException("User is not a bidder.");
        }

        synchronized (autoBidLock) {
            AutoBidOrder order = autoBidByAuctionAndBidder.remove(autoBidKey(auctionId, bidderId));
            if (order == null) {
                return false;
            }
            order.deactivate();
            return true;
        }
    }

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public AuctionDetailDTO getAuctionDetail(String auctionId) throws SQLException, ValidationException {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) {
            throw new ValidationException("Auction not found.");
        }

        List<BidTransaction> transactions = bidTransactionDAO.findByAuction(auction);
        List<BidDTO> bidHistory = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (BidTransaction tx : transactions) {
            bidHistory.add(new BidDTO(
                    tx.getBidder().getFullName(),
                    tx.getAmount(),
                    tx.getTimestamp().format(formatter)
            ));
        }

        String highestBidderName = auction.getHighestBidder() != null ? auction.getHighestBidder().getFullName() : null;

        return new AuctionDetailDTO(
                auction.getId(),
                auction.getItem().getName(),
                auction.getItem().getDescription(),
                auction.getItem().getCategory().name(),
                auction.getSeller().getFullName(),
                auction.getItem().getStartingPrice(),
                auction.getCurrentHighestBid(),
                auction.getStatus().name(),
                auction.getStartTime().format(formatter),
                auction.getEndTime().format(formatter),
                highestBidderName,
                bidHistory
        );
    }

    public List<AuctionSummaryDTO> listAuctions(ListAuctionRequest req) throws SQLException {
        List<Auction> auctions = resolveAuctionsByFilter(req);
        List<AuctionSummaryDTO> summaries = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (Auction auction : auctions) {
            summaries.add(new AuctionSummaryDTO(
                    auction.getId(),
                    auction.getItem().getName(),
                    auction.getItem().getCategory().name(),
                    auction.getCurrentHighestBid(),
                    auction.getStatus().name(),
                    auction.getEndTime().format(formatter)
            ));
        }
        return summaries;
    }

    // Cập nhật giá bid cao nhất hiện tại và thông báo cho clients
    private void placeBidAndBroadcast(Auction auction, Bidder bidder, double amount) throws SQLException, ValidationException {
        BidTransaction transaction = new BidTransaction(auction, bidder, amount);

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                boolean updated = auctionDAO.updateHighestBidIfHigher(conn, auction.getId(), bidder.getId(), amount);
                if (!updated) {
                    conn.rollback();
                    throw new ValidationException("Bid rejected: auction is not running, already ended, insufficient balance, or current price changed.");
                }

                bidTransactionDAO.saveBidTransaction(conn, transaction);
                conn.commit();
                bidBroadcastService.broadcastBidUpdate(new BidUpdateEvent(
                        BidBroadcastService.BID_UPDATED,
                        auction.getId(),
                        bidder.getId(),
                        bidder.getFullName(),
                        amount,
                        amount,
                        transaction.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                ));
            } catch (SQLException | ValidationException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // AUTO-BID ENGINE
    private void processAutoBids(String auctionId) throws SQLException, ValidationException {
        synchronized (autoBidLock) {
            // tiếp tục auto-bidding cho tới khi không ai bid nổi nữa
            int steps = 0;
            while (steps++ < MAX_AUTO_BID_STEPS_PER_TRIGGER) {
                // truy cập DB để lấy trạng thái mới nhất
                Auction auction = auctionDAO.findById(auctionId);
                // check status liên tục
                try {
                    validateAuctionRunning(auction);
                } catch (ValidationException e) {
                    return;
                }

                AutoBidOrder nextOrder = findBestChallenger(auction);
                if (nextOrder == null) {
                    return;
                }

                double previousPrice = auction.getCurrentHighestBid();
                // min để lấy giá chạm trần
                double nextAmount = Math.min(
                        nextOrder.getMaxBid(),
                        previousPrice + nextOrder.getIncrement()
                );
                if (nextAmount <= previousPrice) {
                    deactivateAutoBidOrder(nextOrder);
                    continue;
                }

                try {
                    placeBidAndBroadcast(auction, nextOrder.getBidder(), nextAmount);
                    Auction refreshedAuction = auctionDAO.findById(auctionId);
                    if (refreshedAuction == null || refreshedAuction.getCurrentHighestBid() <= previousPrice) {
                        deactivateAutoBidOrder(nextOrder);
                        return;
                    }
                } catch (ValidationException e) {
                    deactivateAutoBidOrder(nextOrder);
                }
            }
        }
    }

    private void deactivateAutoBidOrder(AutoBidOrder order) {
        order.deactivate();
        autoBidByAuctionAndBidder.remove(autoBidKey(order.getAuctionId(), order.getBidder().getId()));
    }

    // Chặn user đặt bid/auto-bid quá khả năng tài chính
    private void validateBidBudget(
            String auctionId,
            String bidderId,
            double balance,
            double requestedAmount,
            boolean replacingCurrentAuctionAutoBid
    )
            throws SQLException, ValidationException {
        double activeAutoBidCommitment = 0;
        double currentAuctionAutoBidCommitment = 0;
        Set<String> autoBidAuctionIds = new HashSet<>();
        synchronized (autoBidLock) {
            for (AutoBidOrder order : autoBidByAuctionAndBidder.values()) {
                if (!order.isActive()) {
                    continue;
                }
                if (!order.getBidder().getId().equals(bidderId)) {
                    continue;
                }
                if (order.getAuctionId().equals(auctionId)) {
                    if (!replacingCurrentAuctionAutoBid) {
                        currentAuctionAutoBidCommitment = Math.max(currentAuctionAutoBidCommitment, order.getMaxBid());
                    }
                    continue;
                }
                activeAutoBidCommitment += order.getMaxBid();
                autoBidAuctionIds.add(order.getAuctionId());
            }
        }

        Set<String> excludedAuctionIds = new HashSet<>(autoBidAuctionIds);
        excludedAuctionIds.add(auctionId);
        double winningBidCommitment = auctionDAO.sumRunningWinningBidsByBidder(bidderId, excludedAuctionIds);


        double currentAuctionCommitment = Math.max(requestedAmount, currentAuctionAutoBidCommitment);
        double required = currentAuctionCommitment + activeAutoBidCommitment + winningBidCommitment;
        if (required > balance) {
            throw new ValidationException("Bid rejected: amount exceeds available balance after active commitments.");
        }
    }

    // tìm auto-bidder mạnh nhất có thể outbid current winner
    private AutoBidOrder findBestChallenger(Auction auction) {
        PriorityBlockingQueue<AutoBidOrder> queue = autoBidQueues.get(auction.getId());
        if (queue == null) {
            return null;
        }

        // poll() sẽ remove order nên cần lưu tạm vào skipped
        List<AutoBidOrder> skipped = new ArrayList<>();
        AutoBidOrder selected = null;
        String highestBidderId = auction.getHighestBidder() == null ? null : auction.getHighestBidder().getId();

        while (!queue.isEmpty()) {
            AutoBidOrder order = queue.poll();
            if (!order.isActive()) {
                continue;
            }
            // nếu mà giá maxBid hiện tại bé hơn giá max hiện tại => hủy autoBid
            if (order.getMaxBid() <= auction.getCurrentHighestBid()) {
                deactivateAutoBidOrder(order);
                continue;
            }
            if (order.getBidder().getId().equals(highestBidderId)) {
                skipped.add(order);
                continue;
            }
            // nếu gặp người hợp lệ thì chọn luôn
            selected = order;
            break;
        }

        for (AutoBidOrder order : skipped) {
            queue.offer(order);
        }
        if (selected != null) {
            queue.offer(selected);
        }
        return selected;
    }

    private PriorityBlockingQueue<AutoBidOrder> newAutoBidQueue() {
        return new PriorityBlockingQueue<>(
                11,
                Comparator.<AutoBidOrder>comparingDouble(AutoBidOrder::getMaxBid)
                        .reversed()
                        .thenComparing(Comparator.comparingDouble(AutoBidOrder::getIncrement).reversed())
                        .thenComparingLong(AutoBidOrder::getSequence)
        );
    }

    private void validateAuctionRunning(Auction auction) throws ValidationException {
        if (auction == null) {
            throw new ValidationException("Auction not found.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getEndTime()) || now.isBefore(auction.getStartTime())) {
            throw new ValidationException("Auction is not running.");
        }
    }

    private String autoBidKey(String auctionId, String bidderId) {
        return auctionId + ":" + bidderId;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) throws ValidationException {
        try {
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date format. Please use ISO format.");
        }
    }

    private List<Auction> resolveAuctionsByFilter(ListAuctionRequest req) throws SQLException {
        if (req == null || req.getStatus() == null || req.getStatus().isBlank()) {
            return auctionDAO.findAll();
        }

        try {
            AuctionStatus status = AuctionStatus.valueOf(req.getStatus().trim().toUpperCase());
            return auctionDAO.findByStatus(status);
        } catch (IllegalArgumentException e) {
            return auctionDAO.findAll();
        }
    }
}
