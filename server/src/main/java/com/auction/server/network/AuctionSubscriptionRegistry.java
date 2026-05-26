package com.auction.server.network;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Sổ đăng ký (Registry) quản lý các phiên bản Client đang theo dõi realtime từng phiên đấu giá. */
public class AuctionSubscriptionRegistry {
  private final Map<String, Set<ClientSession>> subscribersByAuction = new ConcurrentHashMap<>();
  private final Map<ClientSession, Set<String>> auctionsBySession = new ConcurrentHashMap<>();

  public void subscribe(String auctionId, ClientSession session) {
    if (auctionId == null || auctionId.isBlank() || session == null) {
      return;
    }

    // dùng ConcurrentHashMap.newKeySet() để tránh race condition khi thêm session mới
    subscribersByAuction
        .computeIfAbsent(auctionId, ignored -> ConcurrentHashMap.newKeySet())
        .add(session);

    auctionsBySession
        .computeIfAbsent(session, ignored -> ConcurrentHashMap.newKeySet())
        .add(auctionId);
  }

  public Set<ClientSession> getSubscribers(String auctionId) {
    Set<ClientSession> sessions = subscribersByAuction.get(auctionId);
    if (sessions == null) {
      return Set.of();
    }
    return Set.copyOf(sessions);
  }

  public void unsubscribe(String auctionId, ClientSession session) {
    if (auctionId == null || auctionId.isBlank() || session == null) {
      return;
    }

    Set<ClientSession> sessions = subscribersByAuction.get(auctionId);
    if (sessions != null) {
      sessions.remove(session);
      if (sessions.isEmpty()) {
        subscribersByAuction.remove(auctionId, sessions);
      }
    }

    Set<String> auctionIds = auctionsBySession.get(session);
    if (auctionIds != null) {
      auctionIds.remove(auctionId);
      if (auctionIds.isEmpty()) {
        auctionsBySession.remove(session, auctionIds);
      }
    }
  }

  public void unsubscribeAll(ClientSession session) {
    if (session == null) {
      return;
    }

    Set<String> auctionIds = auctionsBySession.remove(session);
    if (auctionIds == null || auctionIds.isEmpty()) {
      return;
    }

    for (String auctionId : auctionIds) {
      Set<ClientSession> sessions = subscribersByAuction.get(auctionId);
      if (sessions == null) {
        continue;
      }

      sessions.remove(session);
      if (sessions.isEmpty()) {
        subscribersByAuction.remove(auctionId, sessions);
      }
    }
  }

  /** Cleanup toàn bộ subscribers cho 1 auction (thường dùng khi auction đã FINISHED/CANCELED). */
  public void clearAuction(String auctionId) {
    if (auctionId == null || auctionId.isBlank()) {
      return;
    }

    Set<ClientSession> sessions = subscribersByAuction.remove(auctionId);
    if (sessions == null || sessions.isEmpty()) {
      return;
    }

    // Gỡ auctionId khỏi map ngược (auctionsBySession) để tránh leak.
    for (ClientSession session : sessions) {
      if (session == null) {
        continue;
      }
      Set<String> auctionIds = auctionsBySession.get(session);
      if (auctionIds == null) {
        continue;
      }
      auctionIds.remove(auctionId);
      if (auctionIds.isEmpty()) {
        auctionsBySession.remove(session, auctionIds);
      }
    }
  }
}
