package com.auction.server.network;

import com.auction.share.DTO.Response;
import java.io.IOException;
import java.io.ObjectOutputStream;

/** Quản lý phiên làm việc của một Client kết nối đến Server, chứa luồng xuất (OutputStream). */
public class ClientSession {
  private final ObjectOutputStream outputStream;
  private volatile String userId;

  public ClientSession(ObjectOutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  // đóng gói và gửi đối tượng Response từ Server về Client an toàn
  public void send(Response<?> response) throws IOException {
    // khóa synchronized block để tránh lỗi luồng khi nhiều thread cùng ghi ra mạng
    synchronized (outputStream) {
      outputStream.writeObject(response);
      outputStream.flush();
    }
  }
}
