package com.tank2d.client.core;

import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private PacketListener listener;

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
        Packet p = new Packet(PacketType.LEAVE_ROOM);
        sendPacket(p);
    }

    public void setReady(boolean ready) {
        Packet p = new Packet(PacketType.PLAYER_READY);
        p.data.put("ready", ready);
        sendPacket(p);
    }

    public void startGame() {
        Packet p = new Packet(PacketType.START_GAME);
        sendPacket(p);
    }

    public void requestRoomList() {
        Packet p = new Packet(PacketType.ROOM_LIST);
        sendPacket(p);
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
                        listener.onRoomCreated(id, name, maxPlayers);
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
                        listener.onRoomUpdate(message);
                    }

                    case ROOM_LIST_DATA -> {
                        List<Map<String, Object>> rooms = (List<Map<String, Object>>) p.data.get("rooms");
                        listener.onRoomListReceived(rooms);
                    }

                    case START_GAME -> {
                        String message = (String) p.data.get("msg");
                        listener.onGameStart(message);
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