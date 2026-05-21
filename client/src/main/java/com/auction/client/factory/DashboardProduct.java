package com.auction.client.factory;

import javafx.scene.Scene;
import java.io.IOException;

public interface DashboardProduct {
    Scene getScene() throws IOException;
    String getTitle();
}
