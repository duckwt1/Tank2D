package com.tank2d.client.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class UiNavigator {
    private static Stage mainStage;

    public static void setStage(Stage stage) {
        mainStage = stage;
    }

    public static void loadScene(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(UiNavigator.class.getResource(fxml));
            Scene scene = new Scene(loader.load());
            mainStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}