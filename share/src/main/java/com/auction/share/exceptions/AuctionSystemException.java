package com.auction.share.exceptions;

/**
 * Lớp ngoại lệ gốc cho toàn bộ Hệ thống Đấu giá.
 * Các ngoại lệ khác sẽ kế thừa từ lớp này.
 */
public class AuctionSystemException extends Exception {
    public AuctionSystemException(String message) {
        super(message);
    }
}