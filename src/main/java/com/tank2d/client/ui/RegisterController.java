package com.tank2d.client.ui;

import com.tank2d.client.core.GameClient;
import com.tank2d.client.core.PacketListener;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController implements PacketListener {

    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private TextField txtEmail;
    @FXML private Label lblStatus;

    private GameClient client;

    @FXML
    public void initialize() {
        // Client will be set from LoginController
    }

    @FXML
    public void onRegisterClick() {
        String username = txtUsername.getText();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("Please fill in all information!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            lblStatus.setText("Password doesn't match!");
            return;
        }

        Packet p = new Packet(PacketType.REGISTER);
        p.data.put("username", username);
        p.data.put("password", password);
        p.data.put("email", txtEmail.getText());
        client.sendPacket(p);
    }

    @FXML
    public void onBackToLoginClick() {
        // Navigate back and pass the client
        Platform.runLater(() -> {
            LoginController controller = UiNavigator.loadSceneWithController("login.fxml");
            if (controller != null) {
                controller.setClient(client);
            }
        });
    }
    
    public void setClient(GameClient client) {
        this.client = client;
        client.setPacketListener(this);
    }
    
    // ========== PacketListener Implementation ==========
    
    @Override
    public void onRegisterSuccess(String message) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
            // Wait a bit then navigate to login
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> UiNavigator.loadScene("login.fxml"));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }
    
    @Override
    public void onRegisterFail(String message) {
        Platform.runLater(() -> lblStatus.setText(message));
    }
    
    @Override
    public void onDisconnected() {
        Platform.runLater(() -> lblStatus.setText("Disconnected from server"));
    }
}