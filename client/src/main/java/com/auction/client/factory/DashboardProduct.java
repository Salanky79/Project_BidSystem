package com.auction.client.factory;

import javafx.scene.Scene;
import java.io.IOException;

/**
 * Giao diện dashboard theo vai trò.
 */
public interface DashboardProduct {
    Scene getScene() throws IOException;
    String getTitle();
}
