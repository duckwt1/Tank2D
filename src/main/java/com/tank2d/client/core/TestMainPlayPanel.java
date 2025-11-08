package com.tank2d.client.core;
// Pham Ngoc Duc - Lớp 23JIT - Trường VKU - MSSV: 23IT059

import com.tank2d.client.core.PlayPanel;
import com.tank2d.client.entity.Entity;
import com.tank2d.client.entity.Player;
import com.tank2d.shared.Constant;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.shape.Polygon;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestMainPlayPanel extends Application {

    @Override
    public void start(Stage stage) {
        PlayPanel playPanel = new PlayPanel(2, new Player(0, 0, new Polygon(), 3, "Pham Ngoc Duc"), new ArrayList<>());
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
