package com.auction.client.controller;

import java.io.IOException;
import java.util.function.Consumer;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class FrameController {

  @FXML protected ScrollPane scrollContent;

  public void setView(Node node) {
    scrollContent.setContent(node);
  }


  protected void changeView(String fxmlFile, Consumer<Object> controllerConfigurator) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
      Node newNode = loader.load();

      if (controllerConfigurator != null) {
          controllerConfigurator.accept(loader.getController());
      }

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

  protected void onViewChanged(Node newNode) {}

  protected void showLogin(ActionEvent event) {
    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
    com.auction.client.factory.AppNavigator.showLogin(stage);
  }
}
