package com.auction.share.exceptions;

/**
 * Ném ra khi người dùng không đủ tiền dư trong tài khoản để thanh toán hoặc để đặt cọc.
 */
public class InsufficientFundsException extends AuctionSystemException {
    /**
     * Khởi tạo ngoại lệ với một thông báo lỗi cụ thể.
     *
     * @param message Chi tiết lý do vì sao không đủ tiền (ví dụ: số dư hiện tại của bạn là...).
     */
    public InsufficientFundsException(String message) {
        super(message);
    }
}