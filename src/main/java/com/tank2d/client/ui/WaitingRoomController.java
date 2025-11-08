package com.tank2d.client.ui;

import com.tank2d.client.core.GameClient;
import com.tank2d.client.core.PacketListener;
import com.tank2d.client.ui.UiNavigator;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class WaitingRoomController implements PacketListener {

    @FXML private Label lblRoomName;
    @FXML private Label lblPlayerCount;
    @FXML private Label lblStatus;
    @FXML private ListView<String> listPlayers;
    @FXML private Button btnReady;
    @FXML private Button btnStartGame;
    @FXML private Button btnLeaveRoom;

    private GameClient client;
    private int roomId;
    private String roomName;
    private int maxPlayers;
    private boolean isHost = true;

    // -------------------------------
    // SETUP METHODS
    // -------------------------------
    public void setClient(GameClient client) {
        this.client = client;
        client.setPacketListener(this);
    }

    public void setRoomData(int roomId, String roomName, int maxPlayers) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.maxPlayers = maxPlayers;
        lblRoomName.setText("Room: " + roomName + " (ID: " + roomId + ")");
        updatePlayerCount(1, maxPlayers);
        listPlayers.getItems().clear();
        listPlayers.getItems().add("You (Host)");
    }

    public void setHost(boolean host) {
        this.isHost = host;
        btnStartGame.setVisible(host);
        btnReady.setVisible(!host);
    }

    @FXML
    public void initialize() {
        lblStatus.setText("Waiting for other players...");
        btnReady.setVisible(false);
        btnStartGame.setVisible(false);
    }

    // -------------------------------
    // BUTTON HANDLERS
    // -------------------------------

    @FXML
    private void onReady() {
        if (client == null) return;
        lblStatus.setText("You are ready!");
        Packet ready = new Packet(PacketType.valueOf("PLAYER_READY"));
        ready.data.put("roomId", roomId);
        client.sendPacket(ready);
    }

    @FXML
    private void onStartGame() {
        if (client == null) return;
        lblStatus.setText("Starting game...");
        Packet start = new Packet(PacketType.valueOf("START_GAME"));
        start.data.put("roomId", roomId);
        client.sendPacket(start);
        // TODO: chuyển sang màn hình game thực tế
        // UiNavigator.loadScene("game_scene.fxml");
    }

    @FXML
    private void onLeaveRoom() {
        if (client == null) return;
        Packet leave = new Packet(PacketType.valueOf("LEAVE_ROOM"));
        leave.data.put("roomId", roomId);
        client.sendPacket(leave);
        
        // Navigate back to main menu with client
        Platform.runLater(() -> {
            MainMenuController controller = UiNavigator.loadSceneWithController("main_menu.fxml");
            controller.setClient(client);
        });
    }

    // -------------------------------
    // UPDATE HELPERS
    // -------------------------------
    public void updatePlayerList(List<String> players) {
        Platform.runLater(() -> {
            listPlayers.getItems().clear();
            for (String playerName : players) {
                listPlayers.getItems().add(playerName);
            }
            updatePlayerCount(players.size(), maxPlayers);
        });
    }
    public void addPlayer(String name) {
        Platform.runLater(() -> {
            if (!listPlayers.getItems().contains(name)) {
                listPlayers.getItems().add(name);
                updatePlayerCount(listPlayers.getItems().size(), 4);
            }
        });
    }

    public void removePlayer(String name) {
        Platform.runLater(() -> {
            listPlayers.getItems().remove(name);
            updatePlayerCount(listPlayers.getItems().size(), 4);
        });
    }

    public void updatePlayerCount(int current, int max) {
        Platform.runLater(() -> lblPlayerCount.setText("Players: " + current + "/" + max));
    }

    public void updateStatus(String msg) {
        Platform.runLater(() -> lblStatus.setText(msg));
    }
    
    // ========== PacketListener Implementation ==========
    
    @Override
    public void onRoomJoined(int roomId, String roomName, int maxPlayers, List<String> players) {
        Platform.runLater(() -> {
            setRoomData(roomId, roomName, maxPlayers);
            setHost(false);
            updatePlayerList(players);
        });
    }
    
    @Override
    public void onRoomUpdate(String message) {
        Platform.runLater(() -> {
            lblStatus.setText("[ROOM] " + message);
            // TODO: Parse message to update player list
        });
    }
    
    @Override
    public void onGameStart(String message) {
        Platform.runLater(() -> {
            lblStatus.setText(message);
            // TODO: Navigate to game screen
            // UiNavigator.loadScene("game_scene.fxml");
        });
    }
    
    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            lblStatus.setText("Disconnected from server");
            UiNavigator.loadScene("login.fxml");
        });
    }
}
