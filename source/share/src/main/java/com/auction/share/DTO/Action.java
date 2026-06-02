package com.auction.share.DTO;

/** Chứa các hằng số định nghĩa hành động trong hệ thống đấu giá. */
public final class Action {
  public static final String LOGIN = "LOGIN";
  public static final String REGISTER = "REGISTER";
  public static final String LIST_AUCTIONS = "LIST_AUCTIONS";
  public static final String GET_AUCTION_DETAIL = "GET_AUCTION_DETAIL";
  public static final String UNSUBSCRIBE_AUCTION = "UNSUBSCRIBE_AUCTION";
  public static final String CREATE_AUCTION = "CREATE_AUCTION";
  public static final String PLACE_BID = "PLACE_BID";
  public static final String REGISTER_AUTO_BID = "REGISTER_AUTO_BID";
  public static final String CANCEL_AUTO_BID = "CANCEL_AUTO_BID";
  public static final String SET_BID_STEP = "SET_BID_STEP";
  public static final String UPDATE_PROFILE = "UPDATE_PROFILE";
  public static final String GET_PROFILE = "GET_PROFILE";
  public static final String CANCEL_AUCTION = "CANCEL_AUCTION";

  private Action() {}
}
