package com.tank2d.client;

import com.tank2d.client.ui.UiNavigator;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        UiNavigator.setStage(stage);
        UiNavigator.loadScene("login.fxml");
        stage.setTitle("Pixel Tank Online");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}