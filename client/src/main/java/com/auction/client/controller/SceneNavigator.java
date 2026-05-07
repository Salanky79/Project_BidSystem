package com.auction.client.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

final class SceneNavigator {
    private SceneNavigator() {
    }

    static void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = stageFromEvent(event);
        stage.setScene(new Scene(root));
        stage.centerOnScreen();
        stage.show();
    }

    static Stage stageFromEvent(ActionEvent event) {
        return (Stage) ((Node) event.getSource()).getScene().getWindow();
    }
}

