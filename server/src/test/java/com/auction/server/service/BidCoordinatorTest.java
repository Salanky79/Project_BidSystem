package com.auction.server.service;

import com.auction.share.DTO.PlaceBidRequest;
import com.auction.share.exceptions.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BidCoordinatorTest {

    @Mock
    private BidService bidService;

    @Mock
    private AutoBidService autoBidService;

    @InjectMocks
    private BidCoordinator bidCoordinator;

    @Test
    void placeBidAndTriggerAuto_success() throws SQLException, ValidationException {
        PlaceBidRequest request = new PlaceBidRequest("auc-1", "bidder-1", 100.0);
        when(bidService.placeBid(request, false)).thenReturn(true);

        boolean result = bidCoordinator.placeBidAndTriggerAuto(request);

        assertTrue(result);
        verify(bidService).placeBid(request, false);
        verify(autoBidService).triggerAutoBid("auc-1", "bidder-1");
    }
}
