package com.tank2d.client.core;
// Pham Ngoc Duc - Lớp 23JIT - Trường VKU - MSSV: 23IT059

import com.tank2d.client.core.PlayPanel;
import com.tank2d.shared.Constant;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TestMainPlayPanel extends Application {

    @Override
    public void start(Stage stage) {
        PlayPanel playPanel = new PlayPanel();
        Scene scene = new Scene(playPanel);

        stage.setTitle("Tank 2D - VKU");
        stage.setScene(scene);
        stage.show();

        // Phải gọi sau khi stage hiển thị
        new Thread(playPanel).start();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
