package com.auction.client.factory;

import java.io.IOException;
import javafx.scene.Scene;

public interface DashboardProduct {
  Scene getScene() throws IOException;

  String getTitle();
}
