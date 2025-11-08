package com.tank2d.client.ui;

import com.tank2d.client.core.GameClient;
import com.tank2d.client.core.PacketListener;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController implements PacketListener {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblStatus;

    private GameClient client;

    @FXML
    public void initialize() {
        client = new GameClient();
        client.setPacketListener(this);
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
        Platform.runLater(() -> {
            RegisterController controller = UiNavigator.loadSceneWithController("register.fxml");
            // Pass the same client connection to RegisterController
            if (controller != null && client != null) {
                controller.setClient(client);
            }
        });
    }
    
    public void setClient(GameClient client) {
        if (client != null) {
            this.client = client;
            client.setPacketListener(this);
        }
    }

    // ========== PacketListener Implementation ==========
    
    @Override
    public void onLoginSuccess(String message) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
            MainMenuController controller = UiNavigator.loadSceneWithController("main_menu.fxml");
            controller.setClient(client);
            // Transfer listener to MainMenuController
            client.setPacketListener(controller);
        });
    }
    
    @Override
    public void onLoginFail(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }
    
    @Override
    public void onDisconnected() {
        Platform.runLater(() -> lblStatus.setText("Disconnected from server"));
    }
}