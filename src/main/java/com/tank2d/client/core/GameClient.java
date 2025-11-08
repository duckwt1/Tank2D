package com.tank2d.client.core;

import com.tank2d.client.entity.Player;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import javafx.scene.shape.Polygon;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PacketListener listener;
    private GameMiniServer miniServer;
    private GameClientUDP udpClient;

    public GameClient() {
    }

    public void setPacketListener(PacketListener listener) {
        this.listener = listener;
    }

    public void connect(String host, int port) throws Exception {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        new Thread(this::listen).start();
    }

    public void sendPacket(Packet p) {
        try {
            out.writeObject(p);
            out.flush();
        } catch (IOException e) {
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }

    private void listen() {
        try {
            while (true) {
                Packet p = (Packet) in.readObject();
                if (listener == null) continue;

                switch (p.type) {
                    case LOGIN_OK -> {
                        String msg = (String) p.data.get("msg");
                        listener.onLoginSuccess(msg);
                    }

                    case LOGIN_FAIL -> {
                        String msg = (String) p.data.get("msg");
                        listener.onLoginFail(msg);
                    }

                    case REGISTER_OK -> {
                        String msg = (String) p.data.get("msg");
                        listener.onRegisterSuccess(msg);
                    }

                    case REGISTER_FAIL -> {
                        String msg = (String) p.data.get("msg");
                        listener.onRegisterFail(msg);
                    }

                    case ROOM_CREATED -> {
                        String name = (String) p.data.get("roomName");
                        int id = (int) p.data.get("roomId");
                        int maxPlayers = (int) p.data.get("maxPlayers");
                        List<String> players = (List<String>) p.data.get("players");
                        listener.onRoomCreated(id, name, maxPlayers, players);
                    }

                    case ROOM_JOINED -> {
                        String name = (String) p.data.get("roomName");
                        int id = (int) p.data.get("roomId");
                        int maxPlayers = (int) p.data.get("maxPlayers");
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

                    case START_GAME -> {
                        String message = (String) p.data.get("msg");
                        listener.onGameStart(message);
                    }
                    case CREATE_ROOM_HOST -> {
                        Player me = new Player(0, 0, new Polygon(), 3, "Pham Ngoc Duc");
                        PlayPanel playPanel = new PlayPanel(2, me, new ArrayList<>());

                        int udpPort = Integer.parseInt(p.data.getOrDefault("udp_port", "5000").toString());
                        miniServer = new GameMiniServer(playPanel, udpPort);
                        miniServer.start();
                    }

                    case CREATE_ROOM_CLIENT -> {
                        Player me = new Player(0, 0, new Polygon(), 3, "Pham Ngoc Duc");
                        PlayPanel playPanel = new PlayPanel(2, me, new ArrayList<>());

                        String hostIp = p.data.getOrDefault("host_ip", "127.0.0.1").toString();
                        int hostPort = Integer.parseInt(p.data.getOrDefault("host_udp_port", "5000").toString());
                        udpClient = new GameClientUDP(playPanel, hostIp, hostPort);
                        udpClient.start();
                    }
                }
            }
        } catch (Exception e) {
            if (listener != null) {
                listener.onDisconnected();
            }
        }
    }
}