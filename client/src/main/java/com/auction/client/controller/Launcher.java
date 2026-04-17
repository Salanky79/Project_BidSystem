package com.auction.client.controller;

import javafx.application.Application;

public class Launcher {
    public static void main(String[] args) {
        // Thay vì gọi HelloApplication.class, hãy gọi đầy đủ đường dẫn package
        Application.launch(com.auction.client.controller.HelloApplication.class, args);
    }
}