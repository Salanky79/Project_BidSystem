package com.auction.share.DTO;

// Server ưu tiên userId từ session, tránh lộ dữ liệu người khác.
/**
 * Request lấy thông tin hồ sơ người dùng.
 */
public class GetProfileRequest extends Request {
    // serialVersionUID để giữ tương thích khi serialize/deserialize.
    private static final long serialVersionUID = 1L;
    private final String userId;

    public GetProfileRequest(String userId) {
        super(Action.GET_PROFILE);
        this.userId = userId;
    }

    /**
     * Gắn userId theo session nếu request không có sẵn.
     */
    @Override
    public Request withUserId(String userId) {
        if (this.userId == null || this.userId.isBlank()) {
            return new GetProfileRequest(userId);
        }
        return this;
    }

    public String getUserId() {
        return userId;
    }
}