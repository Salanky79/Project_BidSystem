package com.auction.server.util;

import io.github.cdimascio.dotenv.Dotenv;

public class AppConfig {
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

    public static String get(String key) {
        String systemValue = System.getenv(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue;
        }
        return dotenv.get(key);
    }

    public static String getOrDefault(String key, String defaultValue) {
        String value = get(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
