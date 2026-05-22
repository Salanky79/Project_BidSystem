package com.auction.client.controller;

import javafx.application.Application;

/**
 * Lớp Launcher độc lập để khởi động JavaFX, tránh lỗi thiếu thư viện JavaFX Runtime.
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}