package com.auction.server.service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.auction.server.network.ClientSession;

/** Sổ đăng ký (Registry) quản lý các phiên bản Client đang kết nối để nhận realtime broadcast. */
public class AuctionSubscriptionRegistry {
  private final Set<ClientSession> allSessions = ConcurrentHashMap.newKeySet();

  public void addSession(ClientSession session) {
      if (session != null) {
          allSessions.add(session);
      }
  }

  public void removeSession(ClientSession session) {
      if (session != null) {
          allSessions.remove(session);
      }
  }

  public Set<ClientSession> getAllSessions() {
      return Set.copyOf(allSessions);
  }
}

