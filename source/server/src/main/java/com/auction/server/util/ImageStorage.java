package com.auction.server.util;

import java.io.IOException;

public interface ImageStorage {
    /**
     * Tải hình ảnh dạng mảng byte lên Storage.
     *
     * @param imageBytes Dữ liệu nhị phân của ảnh
     * @param fileName Tên file ban đầu
     * @return URL bảo mật (secure_url) trỏ đến ảnh
     * @throws IOException khi kết nối mạng hoặc upload lỗi
     */
    String uploadImage(byte[] imageBytes, String fileName) throws IOException;
}
