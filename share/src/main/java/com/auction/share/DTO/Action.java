package com.auction.share.DTO;

public final class Action {
    public static final String LOGIN = "LOGIN";
    public static final String REGISTER = "REGISTER";
    public static final String LIST_AUCTIONS = "LIST_AUCTIONS";
    public static final String GET_AUCTION_DETAIL = "GET_AUCTION_DETAIL";
    public static final String UNSUBSCRIBE_AUCTION = "UNSUBSCRIBE_AUCTION";
    public static final String CREATE_AUCTION = "CREATE_AUCTION";
    public static final String PLACE_BID = "PLACE_BID";
    public static final String SET_AUTO_BID = "SET_AUTO_BID"; // AUTO_BIDDING
    public static final String CANCEL_AUTO_BID = "CANCEL_AUTO_BID";
    public static final String UPDATE_PROFILE = "UPDATE_PROFILE";
    public static final String GET_PROFILE = "GET_PROFILE";

    private Action() {
    }
}
