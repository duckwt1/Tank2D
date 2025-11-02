package com.tank2d.masterserver.ui;

import com.tank2d.masterserver.core.MasterServer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MasterServerDashboard implements Initializable {
    
    @FXML private Button btnStartStop;
    @FXML private Label lblServerStatus;
    @FXML private Label lblPort;
    @FXML private Label lblTotalClients;
    @FXML private Label lblTotalUsers;
    
    @FXML private TableView<ClientInfo> tblClients;
    @FXML private TableColumn<ClientInfo, String> colClientIP;
    @FXML private TableColumn<ClientInfo, String> colUsername;
    @FXML private TableColumn<ClientInfo, String> colConnectTime;
    
    @FXML private TextArea txtLog;
    @FXML private TextField txtPort;
    
    private MasterServer server;
    private boolean serverRunning = false;
    private ObservableList<ClientInfo> clientList = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        setupUI();
        addLog("Master Server Dashboard initialized");
    }
    
    private void setupTable() {
        colClientIP.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colConnectTime.setCellValueFactory(new PropertyValueFactory<>("connectTime"));
        tblClients.setItems(clientList);
    }
    
    private void setupUI() {
        txtPort.setText("5000");
        lblServerStatus.setText("Stopped");
        lblServerStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        lblPort.setText("N/A");
        lblTotalClients.setText("0");
        lblTotalUsers.setText("0");
        btnStartStop.setText("Start Server");
    }
    
    @FXML
    private void onStartStopClick() {
        if (!serverRunning) {
            startServer();
        } else {
            stopServer();
        }
    }
    
    private void startServer() {
        try {
            int port = Integer.parseInt(txtPort.getText());
            server = new MasterServer(this::onServerEvent);
            
            // Start server in background thread
            new Thread(() -> {
                try {
                    server.start(port);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        addLog("Error starting server: " + e.getMessage());
                        onServerStopped();
                    });
                }
            }).start();
            
            serverRunning = true;
            Platform.runLater(() -> {
                btnStartStop.setText("Stop Server");
                lblServerStatus.setText("Running");
                lblServerStatus.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                lblPort.setText(String.valueOf(port));
                txtPort.setDisable(true);
                addLog("Server started on port " + port);
            });
            
        } catch (NumberFormatException e) {
            addLog("Invalid port number");
        }
    }
    
    private void stopServer() {
        if (server != null) {
            server.stop();
            serverRunning = false;
            onServerStopped();
            addLog("Server stopped");
        }
    }
    
    private void onServerStopped() {
        Platform.runLater(() -> {
            btnStartStop.setText("Start Server");
            lblServerStatus.setText("Stopped");
            lblServerStatus.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            lblPort.setText("N/A");
            txtPort.setDisable(false);
            clientList.clear();
            lblTotalClients.setText("0");
        });
    }
    
    public void onServerEvent(ServerEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case CLIENT_CONNECTED -> {
                    ClientInfo clientInfo = new ClientInfo(
                        event.getClientIP(), 
                        "Unknown", 
                        LocalDateTime.now().format(timeFormatter)
                    );
                    clientList.add(clientInfo);
                    lblTotalClients.setText(String.valueOf(clientList.size()));
                    addLog("Client connected: " + event.getClientIP());
                }
                case CLIENT_DISCONNECTED -> {
                    clientList.removeIf(client -> client.getIpAddress().equals(event.getClientIP()));
                    lblTotalClients.setText(String.valueOf(clientList.size()));
                    addLog("Client disconnected: " + event.getClientIP());
                }
                case CLIENT_LOGIN -> {
                    // Update username in table
                    for (ClientInfo client : clientList) {
                        if (client.getIpAddress().equals(event.getClientIP())) {
                            client.setUsername(event.getMessage());
                            tblClients.refresh();
                            break;
                        }
                    }
                    addLog("Login: " + event.getMessage() + " from " + event.getClientIP());
                }
                case CLIENT_REGISTER -> {
                    addLog("Register: " + event.getMessage() + " from " + event.getClientIP());
                }
                case SERVER_ERROR -> {
                    addLog("ERROR: " + event.getMessage());
                }
            }
        });
    }
    
    private void addLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String logEntry = "[" + timestamp + "] " + message + "\n";
        txtLog.appendText(logEntry);
    }
    
    @FXML
    private void onClearLog() {
        txtLog.clear();
    }
    
    // Inner classes for data models
    public static class ClientInfo {
        private String ipAddress;
        private String username;
        private String connectTime;
        
        public ClientInfo(String ipAddress, String username, String connectTime) {
            this.ipAddress = ipAddress;
            this.username = username;
            this.connectTime = connectTime;
        }
        
        // Getters and setters
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getConnectTime() { return connectTime; }
        public void setConnectTime(String connectTime) { this.connectTime = connectTime; }
    }
    
    public static class ServerEvent {
        public enum Type {
            CLIENT_CONNECTED, CLIENT_DISCONNECTED, CLIENT_LOGIN, CLIENT_REGISTER, SERVER_ERROR
        }
        
        private Type type;
        private String clientIP;
        private String message;
        
        public ServerEvent(Type type, String clientIP, String message) {
            this.type = type;
            this.clientIP = clientIP;
            this.message = message;
        }
        
        public Type getType() { return type; }
        public String getClientIP() { return clientIP; }
        public String getMessage() { return message; }
    }
}