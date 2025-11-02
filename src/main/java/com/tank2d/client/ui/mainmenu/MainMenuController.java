package com.tank2d.client.ui.mainmenu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainMenuController {

    @FXML private Label lblTitle;
    @FXML private Button btnCreateRoom, btnJoinRoom, btnSettings, btnQuit;

    @FXML
    public void initialize() {
        lblTitle.setText("PIXEL TANK ONLINE - MAIN MENU");

        btnCreateRoom.setOnAction(e -> onCreateRoom());
        btnJoinRoom.setOnAction(e -> onJoinRoom());
        btnSettings.setOnAction(e -> onSettings());
        btnQuit.setOnAction(e -> System.exit(0));
    }

    private void onCreateRoom() {
        // TODO: load create room scene
        System.out.println("Create Room clicked!");
//        UiNavigator.loadScene("create_room.fxml");
    }

    private void onJoinRoom() {
        // TODO: load join room scene
        System.out.println("Join Room clicked!");
//        UiNavigator.loadScene("join_room.fxml");
    }

    private void onSettings() {
        // TODO: load settings scene
        System.out.println("Settings clicked!");
//        UiNavigator.loadScene("settings.fxml");
    }
}