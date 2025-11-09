package com.tank2d.client.core;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import static com.tank2d.client.utils.DataTypeParser.toInt;
import static com.tank2d.shared.PacketType.*;

public class GameClient {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private PacketListener listener;
    private GameMiniServer miniServer;
    private GameClientUDP udpClient;

    private final Gson gson = new Gson();

    public GameClient() {}

    // --- Connection setup ---
    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        new Thread(this::listen).start();
    }

    // --- Sending packets ---
    public void sendPacket(Packet p) {
        try {
            String json = gson.toJson(p);
            out.write(json + "\n");
            out.flush();
        } catch (IOException e) {
            System.out.println("Send error: " + e.getMessage());
            if (listener != null) listener.onDisconnected();
        }
    }

    // --- Listening for packets ---
    private void listen() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Type packetType = new TypeToken<Packet>() {}.getType();
                Packet p = gson.fromJson(line, packetType);
                if (p == null || listener == null) continue;
                System.out.println(p.toJson());
                switch (p.type) {
                    case LOGIN_OK -> listener.onLoginSuccess((String) p.data.get("msg"));
                    case LOGIN_FAIL -> listener.onLoginFail((String) p.data.get("msg"));
                    case REGISTER_OK -> listener.onRegisterSuccess((String) p.data.get("msg"));
                    case REGISTER_FAIL -> listener.onRegisterFail((String) p.data.get("msg"));
                    case ROOM_CREATED -> {
                        String name = (String) p.data.get("roomName");
                        int id = ((Double) p.data.get("roomId")).intValue(); // Gson converts numbers to Double
                        int maxPlayers = ((Double) p.data.get("maxPlayers")).intValue();
                        List<String> players = (List<String>) p.data.get("players");
                        listener.onRoomCreated(id, name, maxPlayers, players);
                    }
                    case ROOM_JOINED -> {
                        String name = (String) p.data.get("roomName");
                        int id = toInt(p.data.get("roomId"));
                        int maxPlayers = toInt(p.data.get("maxPlayers"));
                        List<String> players = (List<String>) p.data.get("players");
                        listener.onRoomJoined(id, name, maxPlayers, players);
                    }
                    case ROOM_UPDATE -> {
                        String message = (String) p.data.get("msg");
                        List<String> players = (List<String>) p.data.get("players");
                        listener.onRoomUpdate(message, players);
                    }
                    case ROOM_LIST_DATA -> {
                        List<Map<String, Object>> rooms = (List<Map<String, Object>>) p.data.get("rooms");
                        listener.onRoomListReceived(rooms);
                    }
                    case START_GAME -> listener.onGameStart(p);
                }
            }
        } catch (Exception e) {
            System.out.println("Listen error: " + e.getMessage());
            if (listener != null) listener.onDisconnected();
        }
    }

    // --- Helper methods to send specific packets ---
    public void login(String username, String password) {
        Packet p = new Packet(PacketType.LOGIN);
        p.data.put("username", username);
        p.data.put("password", password);
        sendPacket(p);
    }

    public void register(String username, String password, String email) {
        Packet p = new Packet(PacketType.REGISTER);
        p.data.put("username", username);
        p.data.put("password", password);
        p.data.put("email", email);
        sendPacket(p);
    }

    public void createRoom(String roomName, int maxPlayers, String password) {
        Packet p = new Packet(PacketType.CREATE_ROOM);
        p.data.put("roomName", roomName);
        p.data.put("maxPlayers", maxPlayers);
        p.data.put("password", password);
        sendPacket(p);
    }

    public void joinRoom(int roomId, String password) {
        Packet p = new Packet(PacketType.JOIN_ROOM);
        p.data.put("roomId", roomId);
        p.data.put("password", password);
        sendPacket(p);
    }

    public void leaveRoom() {
        sendPacket(new Packet(PacketType.LEAVE_ROOM));
    }

    public void requestRoomList() {
        sendPacket(new Packet(PacketType.ROOM_LIST));
    }

    public void setReady(boolean ready) {
        Packet p = new Packet(PacketType.PLAYER_READY);
        p.data.put("ready", ready);
        sendPacket(p);
    }

    public void startGame() {
        sendPacket(new Packet(PacketType.START_GAME));
    }

    // --- Getters & Setters ---
    public void setPacketListener(PacketListener listener) {
        this.listener = listener;
    }

    public void setUdpClient(GameClientUDP udpClient) {
        this.udpClient = udpClient;
    }

    public void setMiniServer(GameMiniServer miniServer) {
        this.miniServer = miniServer;
    }

    public Socket getSocket() {
        return socket;
    }

    public GameClientUDP getUdpClient() {
        return udpClient;
    }


    public GameMiniServer getMiniServer() {
        return miniServer;
    }
}
