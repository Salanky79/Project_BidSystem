package com.auction.server.service;

import com.auction.server.network.ClientSession;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BroadcastServiceTest {

    private AuctionSubscriptionRegistry registry;
    private BroadcastService broadcastService;

    @BeforeEach
    void setUp() {
        registry = mock(AuctionSubscriptionRegistry.class);
        broadcastService = new BroadcastService(registry);
    }

    @AfterEach
    void tearDown() {
        broadcastService.shutdown();
    }

    @Test
    void broadcastBidUpdate_successfulSend_allClientsReceive() throws Exception {
        ClientSession session1 = mock(ClientSession.class);
        ClientSession session2 = mock(ClientSession.class);
        
        when(registry.getAllSessions()).thenReturn(Set.of(session1, session2));

        AtomicInteger sentCount = new AtomicInteger(0);
        doAnswer(invocation -> {
            sentCount.incrementAndGet();
            return null;
        }).when(session1).send(any(Response.class));
        
        doAnswer(invocation -> {
            sentCount.incrementAndGet();
            return null;
        }).when(session2).send(any(Response.class));

        BidUpdateEvent event = new BidUpdateEvent(BroadcastService.BID_UPDATED, "a-1", "b-1", "Bidder 1", 100, 100, "now", 1, "future");
        broadcastService.broadcastBidUpdate(event);

        // Use Awaitility since sending happens in an ExecutorService
        await().atMost(5, TimeUnit.SECONDS).until(() -> sentCount.get() == 2);
        
        verify(session1, times(1)).send(any(Response.class));
        verify(session2, times(1)).send(any(Response.class));
    }
    
    @Test
    void broadcastBidUpdate_ioException_unsubscribesClient() throws Exception {
        ClientSession session1 = mock(ClientSession.class);
        
        when(registry.getAllSessions()).thenReturn(Set.of(session1));

        doThrow(new IOException("Connection reset by peer")).when(session1).send(any(Response.class));

        BidUpdateEvent event = new BidUpdateEvent(BroadcastService.BID_UPDATED, "a-1", "b-1", "Bidder 1", 100, 100, "now", 1, "future");
        broadcastService.broadcastBidUpdate(event);

        // Await until unsubscribeAll is called
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            verify(registry).removeSession(session1)
        );
    }

    @Test
    void broadcastAutoBidCancelled_onlySendsToTargetUser() throws Exception {
        ClientSession targetSession = mock(ClientSession.class);
        ClientSession otherSession = mock(ClientSession.class);

        when(targetSession.getUserId()).thenReturn("u-target");
        when(otherSession.getUserId()).thenReturn("u-other");
        when(registry.getAllSessions()).thenReturn(Set.of(targetSession, otherSession));

        broadcastService.broadcastAutoBidCancelled("u-target", "a-1", "Insufficient balance");

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(targetSession, times(1)).send(any(com.auction.share.DTO.Response.class));
            verify(otherSession, never()).send(any());
        });
    }

    @Test
    void broadcastAutoBidCancelled_payloadContainsReasonAndAuctionId() throws Exception {
        ClientSession session = mock(ClientSession.class);
        when(session.getUserId()).thenReturn("u-1");
        when(registry.getAllSessions()).thenReturn(Set.of(session));

        CopyOnWriteArrayList<com.auction.share.DTO.Response<?>> captured = new CopyOnWriteArrayList<>();
        doAnswer(inv -> { captured.add(inv.getArgument(0)); return null; }).when(session).send(any());

        broadcastService.broadcastAutoBidCancelled("u-1", "a-1", "Max bid reached");

        await().atMost(5, TimeUnit.SECONDS).until(() -> !captured.isEmpty());

        com.auction.share.DTO.Response<?> response = captured.get(0);
        assertEquals("AUTO_BID_CANCELLED", response.getMessage());
        com.auction.share.DTO.AutoBidCancelledEvent event =
                (com.auction.share.DTO.AutoBidCancelledEvent) response.getData();
        assertEquals("a-1", event.getAuctionId());
        assertEquals("Max bid reached", event.getReason());
    }

    @Test
    void broadcastBidStepUpdated_sendsToAllSessions() throws Exception {
        ClientSession session1 = mock(ClientSession.class);
        ClientSession session2 = mock(ClientSession.class);
        when(registry.getAllSessions()).thenReturn(Set.of(session1, session2));

        AtomicInteger count = new AtomicInteger();
        doAnswer(inv -> { count.incrementAndGet(); return null; }).when(session1).send(any());
        doAnswer(inv -> { count.incrementAndGet(); return null; }).when(session2).send(any());

        broadcastService.broadcastBidStepUpdated("a-1", 50.0);

        await().atMost(5, TimeUnit.SECONDS).until(() -> count.get() == 2);
        verify(session1, times(1)).send(any());
        verify(session2, times(1)).send(any());
    }
}
