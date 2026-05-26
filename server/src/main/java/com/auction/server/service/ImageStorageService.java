package com.auction.server.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageStorageService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ImageStorageService.class);
  private final String uploadDir;

  public ImageStorageService() {
    this("./uploads/items");
  }

  public ImageStorageService(String uploadDir) {
    this.uploadDir = uploadDir;
    createUploadDirectory();
  }

  private void createUploadDirectory() {
    try {
      Path path = Paths.get(uploadDir);
      if (!Files.exists(path)) {
        Files.createDirectories(path);
        LOGGER.info("Created upload directory: {}", path.toAbsolutePath());
      }
    } catch (IOException e) {
      LOGGER.error("Failed to create upload directory", e);
    }
  }

  /**
   * Lưu hình ảnh vào filesystem.
   *
   * @param itemId id của item dùng làm tên file
   * @param imageBytes dữ liệu ảnh dạng raw bytes
   * @return Đường dẫn tương đối của file đã lưu, hoặc null nếu không thành công hoặc không có dữ liệu
   */
  public String saveImage(String itemId, byte[] imageBytes) {
    if (imageBytes == null || imageBytes.length == 0) {
      return null;
    }

    try {
      Path filePath = Paths.get(uploadDir, itemId + ".jpg");
      Files.write(filePath, imageBytes);
      LOGGER.info("Saved image to: {}", filePath.toAbsolutePath());
      return filePath.toString(); // Trả về đường dẫn tương đối (ví dụ: ./uploads/items/123.jpg)
    } catch (IOException e) {
      LOGGER.error("Failed to save image for item: {}", itemId, e);
      return null;
    }
  }

  /**
   * Load hình ảnh từ filesystem.
   *
   * @param imagePath đường dẫn tương đối của file ảnh
   * @return byte[] dữ liệu ảnh, hoặc null nếu file không tồn tại hoặc lỗi
   */
  public byte[] loadImage(String imagePath) {
    if (imagePath == null || imagePath.isBlank()) {
      return null;
    }

    try {
      Path filePath = Paths.get(imagePath);
      if (Files.exists(filePath)) {
        return Files.readAllBytes(filePath);
      } else {
        LOGGER.warn("Image file does not exist: {}", filePath.toAbsolutePath());
        return null;
      }
    } catch (IOException e) {
      LOGGER.error("Failed to read image at path: {}", imagePath, e);
      return null;
    }
  }
}
