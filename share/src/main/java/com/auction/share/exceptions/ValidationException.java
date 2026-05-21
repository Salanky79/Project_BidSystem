package com.auction.share.exceptions;

/**
 * Ném ra khi dữ liệu đầu vào không hợp lệ hoặc không vượt qua được quá trình kiểm tra
 * (ví dụ: tên bị trống, định dạng email sai, hoặc trường bắt buộc bị thiếu).
 */
public class ValidationException extends AuctionSystemException {
    /**
     * Khởi tạo ngoại lệ lỗi xác thực dữ liệu.
     *
     * @param message Mô tả cụ thể trường nào bị lỗi và nguyên nhân lỗi.
     */
    public ValidationException(String message) {
        super(message);
    }
}
