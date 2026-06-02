package com.auction.share.DTO;

/** Yêu cầu lấy thông tin hồ sơ (profile) của người dùng. */
public class GetProfileRequest extends Request {
  private static final long serialVersionUID = 1L;

  public GetProfileRequest(String userId) {
    super(Action.GET_PROFILE);
    withUserId(userId);
  }
}
