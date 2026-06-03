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

        BidUpdateEvent event = new BidUpdateEvent(BroadcastService.BID_UPDATED, "a-1", "b-1", "Bidder 1", 100, 100, "now", 1);
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

        BidUpdateEvent event = new BidUpdateEvent(BroadcastService.BID_UPDATED, "a-1", "b-1", "Bidder 1", 100, 100, "now", 1);
        broadcastService.broadcastBidUpdate(event);

        // Await until unsubscribeAll is called
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> 
            verify(registry).removeSession(session1)
        );
    }
}
