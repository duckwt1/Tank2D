package com.tank2d.client.ui.login;

import com.tank2d.client.core.GameClient;
import com.tank2d.client.ui.UiNavigator;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private GameClient client;

    @FXML
    public void initialize() {
        client = new GameClient(this::handleServerMessage);
        try {
            client.connect("localhost", 5000);
            lblStatus.setText("Connected to server");
        } catch (Exception e) {
            lblStatus.setText("Cannot connect to server");
        }
    }

    @FXML
    public void onLoginClick() {
        Packet p = new Packet(PacketType.LOGIN);
        p.data.put("username", txtUsername.getText());
        p.data.put("password", txtPassword.getText());
        client.sendPacket(p);
    }

    @FXML
    public void onRegisterClick() {
        Packet p = new Packet(PacketType.REGISTER);
        p.data.put("username", txtUsername.getText());
        p.data.put("password", txtPassword.getText());
        client.sendPacket(p);
    }

    private void handleServerMessage(String msg) {
        Platform.runLater(() -> lblStatus.setText(msg));
        if (msg.startsWith("Welcome")) {
            Platform.runLater(() -> UiNavigator.loadScene("main_menu.fxml"));
        }
    }

}