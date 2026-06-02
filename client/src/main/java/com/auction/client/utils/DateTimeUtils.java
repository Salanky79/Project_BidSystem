package com.auction.client.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter CHART_FMT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public static LocalDateTime parseDateTime(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        try { return LocalDateTime.parse(s, ISO_FMT); } catch (Exception ignored) {}

        return null;
    }

    public static String formatDateTimeForDisplay(String raw) {
        LocalDateTime dt = parseDateTime(raw);
        if (dt == null) return raw == null ? "N/A" : raw;
        return dt.format(DISPLAY_FMT);
    }
}
