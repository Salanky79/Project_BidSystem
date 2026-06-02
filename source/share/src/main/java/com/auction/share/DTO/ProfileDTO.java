package com.auction.share.DTO;

import java.io.Serializable;
import java.util.List;

/** Đối tượng truyền dữ liệu (DTO) chứa thông tin hồ sơ của người dùng. */
public class ProfileDTO implements Serializable {
  private static final long serialVersionUID = 1L;

  private final UserDTO user;
  private final List<ProfileBidTransactionDTO> bidTransactions;

  public ProfileDTO(UserDTO user, List<ProfileBidTransactionDTO> bidTransactions) {
    this.user = user;
    this.bidTransactions = bidTransactions;
  }

  public UserDTO getUser() {
    return user;
  }

  public List<ProfileBidTransactionDTO> getBidTransactions() {
    return bidTransactions;
  }
}
