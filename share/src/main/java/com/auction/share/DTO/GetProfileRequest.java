package com.auction.share.DTO;

/** Yêu cầu lấy thông tin hồ sơ (profile) của người dùng. */
public class GetProfileRequest extends Request {
  private static final long serialVersionUID = 1L;

  private final String userId;

  public GetProfileRequest(String userId) {
    super(Action.GET_PROFILE);
    this.userId = userId;
  }

  public String getUserId() {
    return userId;
  }

  @Override
  public Request withUserId(String userId) {
    if (userId == null || userId.isBlank()) {
      return this;
    }
    // Always trust server-side session userId, never client-supplied userId.
    return new GetProfileRequest(userId);
  }
}
