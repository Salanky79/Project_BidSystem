package com.auction.share.exceptions;

/**
 * Ném ra khi xảy ra sự cố về mạng, ví dụ như mất kết nối giữa Client và Server,
 * hoặc server không thể đạt được.
 */
public class NetworkConnectionException extends AuctionSystemException {
    /**
     * Khởi tạo ngoại lệ liên quan đến lỗi kết nối mạng.
     *
     * @param message Chi tiết về lỗi mạng, có thể kèm theo địa chỉ host hoặc port.
     */
    public NetworkConnectionException(String message) {
        super(message);
    }
}