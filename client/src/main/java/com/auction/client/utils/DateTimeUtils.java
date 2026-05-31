package com.auction.client.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {

    public static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
    public static final DateTimeFormatter LEGACY_DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter ISO_FMT =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final DateTimeFormatter ALT_DB_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter CHART_FMT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    public static LocalDateTime parseDateTime(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        try { return LocalDateTime.parse(s, ISO_FMT); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s, LEGACY_DISPLAY_FMT); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s, DISPLAY_FMT); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s, ALT_DB_FMT); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(s.replace(' ', 'T'), ISO_FMT); } catch (Exception ignored) {}

        return null;
    }

    public static String formatDateTimeForDisplay(String raw) {
        LocalDateTime dt = parseDateTime(raw);
        if (dt == null) return raw == null ? "N/A" : raw;
        return dt.format(DISPLAY_FMT);
    }
}
