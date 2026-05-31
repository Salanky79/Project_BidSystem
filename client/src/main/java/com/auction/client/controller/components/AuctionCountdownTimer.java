package com.auction.client.controller.components;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AuctionCountdownTimer {

    private final Label countdownLabel;
    private Timeline timeline;
    private LocalDateTime endTime;
    private Runnable onEndedAction;

    public AuctionCountdownTimer(Label countdownLabel) {
        this.countdownLabel = countdownLabel;
    }

    public void setOnEndedAction(Runnable onEndedAction) {
        this.onEndedAction = onEndedAction;
    }

    public void start(LocalDateTime endTime) {
        this.endTime = endTime;
        if (timeline != null) {
            timeline.stop();
        }
        if (this.endTime == null) {
            if (countdownLabel != null) countdownLabel.setText("N/A");
            return;
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        update();
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    private void update() {
        if (countdownLabel == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(endTime) || now.isEqual(endTime)) {
            countdownLabel.setText("Ended");
            stop();
            if (onEndedAction != null) {
                onEndedAction.run();
            }
            return;
        }
        long days = ChronoUnit.DAYS.between(now, endTime);
        long hours = ChronoUnit.HOURS.between(now, endTime) % 24;
        long minutes = ChronoUnit.MINUTES.between(now, endTime) % 60;
        long seconds = ChronoUnit.SECONDS.between(now, endTime) % 60;
        countdownLabel.setText(String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds));
    }
}
