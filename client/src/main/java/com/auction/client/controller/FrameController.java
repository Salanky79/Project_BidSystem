package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

import java.io.IOException;

public class FrameController {

    @FXML
    protected ScrollPane scrollContent;

    public void setView(Node node) {
        scrollContent.setContent(node);
    }

    protected void changeView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Node newNode = loader.load();

            scrollContent.setContent(newNode);
            onViewChanged(newNode);

            if (newNode instanceof Region region) {
                region.setMaxWidth(Double.MAX_VALUE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: File not found " + fxmlFile);
        }
    }

    protected void onViewChanged(Node newNode) {
    }

    protected void showLogin(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/client/view/Login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(loginRoot);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: Login.fxml file not found.");
        }
    }
}
