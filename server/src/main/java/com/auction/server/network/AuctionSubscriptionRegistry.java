package com.auction.server.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Quản lý danh sách client đang theo dõi phiên đấu giá.
 */
public class AuctionSubscriptionRegistry {
    private final Map<String, Set<ClientSession>> subscribersByAuction = new HashMap<>();
    private final Map<ClientSession, String> auctionBySession = new HashMap<>();

    public synchronized void subscribe(String auctionId, ClientSession session) {
        if (auctionId == null || auctionId.isBlank() || session == null) {
            return;
        }

        // Hủy đăng ký phiên trước nếu đổi sang phiên khác.
        String previousAuctionId = auctionBySession.get(session);
        if (previousAuctionId != null && !previousAuctionId.equals(auctionId)) {
            Set<ClientSession> previousSessions = subscribersByAuction.get(previousAuctionId);
            if (previousSessions != null) {
                previousSessions.remove(session);
                if (previousSessions.isEmpty()) {
                    subscribersByAuction.remove(previousAuctionId);
                }
            }
        }

        // Thêm session vào danh sách subscriber của auction.
        subscribersByAuction
                .computeIfAbsent(auctionId, ignored -> new HashSet<>())
                .add(session);
        // Ghi nhận session đang xem auction nào.
        auctionBySession.put(session, auctionId);
    }

    public synchronized Set<ClientSession> getSubscribers(String auctionId) {
        Set<ClientSession> sessions = subscribersByAuction.get(auctionId);
        if (sessions == null) {
            return Set.of();
        }
        return new HashSet<>(sessions);
    }

    public synchronized void unsubcribe(ClientSession session) {
        if (session == null) {
            return;
        }
        String auctionId = auctionBySession.remove(session);
        if (auctionId == null) {
            return;
        }
        Set<ClientSession> sessions = subscribersByAuction.get(auctionId);
        if (sessions == null) {
            return;
        }
        sessions.remove(session);
        if (sessions.isEmpty()) {
            subscribersByAuction.remove(auctionId);
        }
    }
}
