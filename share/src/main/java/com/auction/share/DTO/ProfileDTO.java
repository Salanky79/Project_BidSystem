package com.auction.share.DTO;

import java.io.Serializable;
import java.util.List;

// Dùng DTO để tránh lộ dữ liệu nhạy cảm từ Entity, đồng thời gom dữ liệu màn hình profile.
/**
 * DTO tổng hợp dữ liệu hồ sơ người dùng.
 */
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