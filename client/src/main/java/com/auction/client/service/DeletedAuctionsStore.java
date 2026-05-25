package com.auction.client.service;

import java.util.HashSet;
import java.util.Set;

/**
 * Singleton in-memory store tracking auction IDs that the seller has "deleted" from the UI.
 * Both Seller and Bidder views consult this store to hide deleted auctions.
 *
 * <p>Pattern mirrors {@link WatchlistService} — simple, no file I/O, lives for the session.
 */
public class DeletedAuctionsStore {

  private static final DeletedAuctionsStore INSTANCE = new DeletedAuctionsStore();
  private final Set<String> deletedIds = new HashSet<>();

  private DeletedAuctionsStore() {}

  public static DeletedAuctionsStore getInstance() {
    return INSTANCE;
  }

  /** Mark an auction as deleted (hidden from all views). */
  public void delete(String auctionId) {
    deletedIds.add(auctionId);
  }

  /** Check whether the given auction has been deleted. */
  public boolean isDeleted(String auctionId) {
    return deletedIds.contains(auctionId);
  }
}
