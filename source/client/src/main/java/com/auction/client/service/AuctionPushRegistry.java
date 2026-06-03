package com.auction.client.service;

import com.auction.client.network.SocketClient;
import com.auction.share.DTO.BidStepUpdateEvent;
import com.auction.share.DTO.BidUpdateEvent;
import com.auction.share.DTO.Response;
import javafx.application.Platform;
import java.util.function.Consumer;

public class AuctionPushRegistry {
    private final SocketClient socketClient;
    private final String auctionId;
    private Consumer<BidUpdateEvent> onBidUpdate;
    private Runnable onAuctionCancelled;
    private Runnable onAuctionFinished;
    private Consumer<Double> onBidStepUpdate;
    private Runnable onAutoBidCancelled;
    private Consumer<Response<?>> pushListener;

    public AuctionPushRegistry(SocketClient socketClient, String auctionId) {
        this.socketClient = socketClient;
        this.auctionId = auctionId;
    }

    public void setOnBidUpdate(Consumer<BidUpdateEvent> onBidUpdate) {
        this.onBidUpdate = onBidUpdate;
    }

    public void setOnAuctionCancelled(Runnable onAuctionCancelled) {
        this.onAuctionCancelled = onAuctionCancelled;
    }

    public void setOnAuctionFinished(Runnable onAuctionFinished) {
        this.onAuctionFinished = onAuctionFinished;
    }

    public void setOnBidStepUpdate(Consumer<Double> onBidStepUpdate) {
        this.onBidStepUpdate = onBidStepUpdate;
    }

    public void setOnAutoBidCancelled(Runnable onAutoBidCancelled) {
        this.onAutoBidCancelled = onAutoBidCancelled;
    }

    public void register() {
        if (socketClient == null || auctionId == null) {
            return;
        }
        
        if (pushListener != null) {
            unregister();
        }

        pushListener = response -> {
            if (response != null && response.isSuccess()) {
                if (response.getData() instanceof BidUpdateEvent event) {
                    if (auctionId.equals(event.getAuctionId())) {
                        if (onBidUpdate != null) {
                            Platform.runLater(() -> onBidUpdate.accept(event));
                        }
                    }
                } else if ("AUCTION_CANCELLED".equals(response.getMessage()) && response.getData() instanceof String cancelledId) {
                    if (auctionId.equals(cancelledId)) {
                        if (onAuctionCancelled != null) {
                            Platform.runLater(onAuctionCancelled);
                        }
                    }
                } else if ("AUCTION_FINISHED".equals(response.getMessage()) && response.getData() instanceof String finishedId) {
                    if (auctionId.equals(finishedId)) {
                        if (onAuctionFinished != null) {
                            Platform.runLater(onAuctionFinished);
                        }
                    }
                } else if ("BID_STEP_UPDATED".equals(response.getMessage()) && response.getData() instanceof BidStepUpdateEvent stepEvent) {
                    if (auctionId.equals(stepEvent.getAuctionId())) {
                        if (onBidStepUpdate != null) {
                            Platform.runLater(() -> onBidStepUpdate.accept(stepEvent.getBidStep()));
                        }
                    }
                } else if ("AUTO_BID_CANCELLED".equals(response.getMessage()) && response.getData() instanceof String cancelledId) {
                    if (auctionId.equals(cancelledId)) {
                        if (onAutoBidCancelled != null) {
                            Platform.runLater(onAutoBidCancelled);
                        }
                    }
                }
            }
        };

        socketClient.addPushListener(pushListener);
    }

    public void unregister() {
        if (socketClient != null && pushListener != null) {
            socketClient.send(new com.auction.share.DTO.UnsubscribeAuctionRequest(auctionId), null);
            socketClient.removePushListener(pushListener);
            pushListener = null;
        }
    }
}
