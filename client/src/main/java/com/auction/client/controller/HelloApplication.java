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
    FXMLLoader fxmlLoader =
        new FXMLLoader(HelloApplication.class.getResource("/com/auction/client/view/Login.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    stage.setTitle("HanoiBid");
    stage.setScene(scene);
    stage.show();
  }

  @Override
  public void stop() throws Exception {
    ClientContext.socketClient().shutdown();
    super.stop();
  }
}
