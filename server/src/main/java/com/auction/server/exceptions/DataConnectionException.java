package com.auction.server.exceptions;

import com.auction.share.exceptions.AuctionSystemException;

/**
 * Ném ra khi hệ thống gặp lỗi về kết nối mạng hoặc lỗi đọc/ghi dữ liệu.
 */
public class DataConnectionException extends AuctionSystemException {
    public DataConnectionException(String message) {
        super(message);
    }
}