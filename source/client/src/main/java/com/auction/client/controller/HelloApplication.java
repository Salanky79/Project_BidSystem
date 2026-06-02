package com.auction.client.controller;

import com.auction.client.ClientContext;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
  @Override
  public void start(Stage stage) throws IOException {
    stage.setTitle("HanoiBid");
    com.auction.client.factory.AppNavigator.showLogin(stage);
  }

  @Override
  public void stop() throws Exception {
    ClientContext.socketClient().shutdown();
    super.stop();
  }
}
