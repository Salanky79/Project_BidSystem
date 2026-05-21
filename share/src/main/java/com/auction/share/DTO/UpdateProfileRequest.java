package com.auction.share.DTO;

// Server ưu tiên userId từ session, không tin tuyệt đối dữ liệu từ Client.
/**
 * Request cập nhật hồ sơ người dùng.
 */
public class UpdateProfileRequest extends Request {
    // serialVersionUID để giữ tương thích khi serialize/deserialize.
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String fullName;
    private final String password;
    private final String phoneNumber;
    private final String email;
    private final String address;

    public UpdateProfileRequest(
            String userId,
            String fullName,
            String password,
            String phoneNumber,
            String email,
            String address
    ) {
        super(Action.UPDATE_PROFILE);
        this.userId = userId;
        this.fullName = fullName;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.address = address;
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Gắn userId theo session nếu request không có sẵn.
     */
    @Override
    public Request withUserId(String userId) {
        if (this.userId == null || this.userId.isBlank()) {
            return new UpdateProfileRequest(userId, fullName, password, phoneNumber, email, address);
        }
        return this;
    }
}