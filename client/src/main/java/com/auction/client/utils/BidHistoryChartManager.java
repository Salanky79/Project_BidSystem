package com.auction.client.utils;

import com.auction.share.DTO.BidDTO;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidHistoryChartManager {

    private static final int MAX_CHART_POINTS = 5;
    private final LineChart<String, Number> chart;
    private final XYChart.Series<String, Number> series;

    public BidHistoryChartManager(LineChart<String, Number> chart) {
        this.chart = chart;
        this.series = new XYChart.Series<>();

        if (this.chart != null) {
            NumberAxis yAxis = (NumberAxis) this.chart.getYAxis();
            yAxis.setAutoRanging(true);
            yAxis.setForceZeroInRange(false);
            this.chart.getData().add(this.series);
        }
    }

    public void loadData(List<BidDTO> bidHistory, String startTimeISO, double startingPrice, double currentPrice) {
        if (chart == null) return;
        series.getData().clear();

        List<XYChart.Data<String, Number>> points = new ArrayList<>();

        if (bidHistory != null && !bidHistory.isEmpty()) {
            List<BidDTO> sortedBids = new ArrayList<>(bidHistory);
            sortedBids.sort((b1, b2) -> {
                try {
                    return LocalDateTime.parse(b1.getTimestamp(), DateTimeUtils.ISO_FMT)
                            .compareTo(LocalDateTime.parse(b2.getTimestamp(), DateTimeUtils.ISO_FMT));
                } catch (Exception e) { return 0; }
            });

            for (BidDTO bid : sortedBids) {
                try {
                    LocalDateTime bidTime = LocalDateTime.parse(bid.getTimestamp(), DateTimeUtils.ISO_FMT);
                    points.add(new XYChart.Data<>(bidTime.format(DateTimeUtils.CHART_FMT), bid.getAmount()));
                } catch (Exception ignored) {}
            }
        }

        if (points.isEmpty()) {
            String startLabel = "Start";
            if (startTimeISO != null) {
                try {
                    startLabel = LocalDateTime.parse(startTimeISO, DateTimeUtils.ISO_FMT).format(DateTimeUtils.CHART_FMT);
                } catch (Exception ignored) {}
            }
            points.add(new XYChart.Data<>(startLabel, startingPrice > 0 ? startingPrice : currentPrice));
        }

        int from = Math.max(0, points.size() - MAX_CHART_POINTS);
        series.getData().addAll(points.subList(from, points.size()));
    }

    public void appendPoint(double amount, String bidTimeISO) {
        if (chart == null) return;
        String timeStr;
        if (bidTimeISO != null) {
            try {
                timeStr = LocalDateTime.parse(bidTimeISO, DateTimeUtils.ISO_FMT).format(DateTimeUtils.CHART_FMT);
            } catch (Exception e) {
                timeStr = LocalDateTime.now().format(DateTimeUtils.CHART_FMT);
            }
        } else {
            timeStr = LocalDateTime.now().format(DateTimeUtils.CHART_FMT);
        }
        series.getData().add(new XYChart.Data<>(timeStr, amount));
        if (series.getData().size() > MAX_CHART_POINTS) {
            series.getData().remove(0);
        }
    }
}
