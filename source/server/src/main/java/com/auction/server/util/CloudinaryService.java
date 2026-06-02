package com.auction.server.util;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Dịch vụ tích hợp Cloudinary hỗ trợ upload mảng byte hình ảnh lên Cloud Storage. */
public class CloudinaryService implements ImageStorage {
  private static final Logger LOGGER = LoggerFactory.getLogger(CloudinaryService.class);
  private final Cloudinary cloudinary;

  public CloudinaryService() {
      String cloudName = AppConfig.get("CLOUDINARY_CLOUD_NAME");
      String apiKey = AppConfig.get("CLOUDINARY_API_KEY");
      String apiSecret = AppConfig.get("CLOUDINARY_API_SECRET");

      if (cloudName == null || cloudName.isBlank() ||
          apiKey == null || apiKey.isBlank() ||
          apiSecret == null || apiSecret.isBlank()) {
        LOGGER.warn("Cloudinary is NOT configured. Image uploads will fail. Make sure CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET are set.");
        this.cloudinary = null;
      } else {
        LOGGER.info("Initializing Cloudinary using individual configuration variables.");
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        this.cloudinary = new Cloudinary(config);
      }
  }

  /**
   * Tải hình ảnh dạng mảng byte lên Cloudinary.
   *
   * @param imageBytes Dữ liệu nhị phân của ảnh
   * @param fileName Tên file ban đầu để tối ưu hóa SEO và tên hiển thị trên cloud
   * @return URL bảo mật (secure_url) trỏ đến ảnh trên Cloudinary
   * @throws IOException khi kết nối mạng hoặc upload lỗi
   */
  public String uploadImage(byte[] imageBytes, String fileName) throws IOException {
    if (this.cloudinary == null) {
      throw new IllegalStateException("Cloudinary is not configured. Cannot upload image.");
    }
    if (imageBytes == null || imageBytes.length == 0) {
      return null;
    }

    LOGGER.info("Uploading image to Cloudinary, size={} bytes, fileName={}", imageBytes.length, fileName);
    
    String publicId = null;
    if (fileName != null && !fileName.isBlank()) {
      int dotIdx = fileName.lastIndexOf('.');
      String nameWithoutExt = dotIdx > 0 ? fileName.substring(0, dotIdx) : fileName;
      // Chuẩn hóa tên file, thay thế ký tự lạ bằng dấu gạch dưới
      publicId = "items/" + nameWithoutExt.replaceAll("[^a-zA-Z0-9_-]", "_") + "_" + System.currentTimeMillis();
    } else {
      publicId = "items/item_" + System.currentTimeMillis();
    }

    Map<Object, Object> params = new HashMap<>();
    params.put("folder", "auction_system");
    params.put("public_id", publicId);
    params.put("resource_type", "image");

    Map<?, ?> uploadResult = cloudinary.uploader().upload(imageBytes, params);
    
    String secureUrl = (String) uploadResult.get("secure_url");
    LOGGER.info("Image uploaded successfully to Cloudinary. secure_url={}", secureUrl);
    return secureUrl;
  }
}
