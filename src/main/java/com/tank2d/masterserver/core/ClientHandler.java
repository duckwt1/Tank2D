package com.tank2d.masterserver.core;

import com.tank2d.masterserver.core.room.Room;
import com.tank2d.masterserver.core.room.RoomManager;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import com.tank2d.masterserver.ui.MasterServerDashboard.ServerEvent;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private Consumer<ServerEvent> eventCallback;
    private String clientIP;
    private Room currentRoom;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.clientIP = socket.getInetAddress().getHostAddress();
    }

    public ClientHandler(Socket socket, Consumer<ServerEvent> eventCallback) {
        this.socket = socket;
        this.eventCallback = eventCallback;
        this.clientIP = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Client connected: " + socket.getInetAddress());

            while (true) {
                Packet p = (Packet) in.readObject();
                handlePacket(p);
            }

        } catch (Exception e) {
            System.out.println("Client disconnected: " + (username != null ? username : clientIP));
            notifyEvent(new ServerEvent(ServerEvent.Type.CLIENT_DISCONNECTED, clientIP, username != null ? username : "Unknown"));
            
            // Remove from current room if in one
            if (currentRoom != null) {
                currentRoom.removePlayer(this);
                broadcastToRoom(currentRoom, PacketType.ROOM_UPDATE, "Player " + username + " left the room");
                if (currentRoom.getPlayers().isEmpty()) {
                    RoomManager.removeRoom(currentRoom.getId());
                }
            }
        } finally {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("Error closing client socket: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    private void handlePacket(Packet p) {
        switch (p.type) {
            case LOGIN -> handleLogin(p);
            case REGISTER -> handleRegister(p);
            case CREATE_ROOM -> handleCreateRoom(p);
            case JOIN_ROOM -> handleJoinRoom(p);
            case LEAVE_ROOM -> handleLeaveRoom(p);
            case PLAYER_READY -> handlePlayerReady(p);
            case START_GAME -> handleStartGame(p);
            case ROOM_LIST -> handleRoomList(p);
        }
    }

    private void handleLogin(Packet p) {
        username = (String) p.data.get("username");
        String password = (String) p.data.get("password");

        boolean success = AccountManager.login(username, password);
        Packet resp = new Packet(success ? PacketType.LOGIN_OK : PacketType.LOGIN_FAIL);
        resp.data.put("msg", success ? "Welcome " + username + "!" : "Invalid credentials!");
        send(resp);
        
        notifyEvent(new ServerEvent(ServerEvent.Type.CLIENT_LOGIN, clientIP, username + (success ? " (SUCCESS)" : " (FAILED)")));
    }

    private void handleRegister(Packet p) {
        String user = (String) p.data.get("username");
        String pass = (String) p.data.get("password");

        boolean ok = AccountManager.register(user, pass);
        Packet resp = new Packet(ok ? PacketType.REGISTER_OK : PacketType.REGISTER_FAIL);
        resp.data.put("msg", ok ? "Registered successfully!" : "Username already exists!");
        send(resp);
        
        notifyEvent(new ServerEvent(ServerEvent.Type.CLIENT_REGISTER, clientIP, user + (ok ? " (SUCCESS)" : " (FAILED)")));
    }

    private void handleRoomList(Packet p) {
        Packet resp = new Packet(PacketType.ROOM_LIST_DATA);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Room r : RoomManager.getRooms()) {
            Map<String, Object> roomInfo = new HashMap<>();
            roomInfo.put("id", r.getId());
            roomInfo.put("name", r.getName());
            roomInfo.put("players", r.getPlayers().size());
            roomInfo.put("maxPlayers", r.getMaxPlayers());
            roomInfo.put("hasPassword", r.hasPassword());
            list.add(roomInfo);
        }

        resp.data.put("rooms", list);
        send(resp);
    }


    private void handleCreateRoom(Packet p) {
        String roomName = (String) p.data.get("roomName");
        int maxPlayers = (int) p.data.get("maxPlayers");
        String password = (String) p.data.get("password");
        
        Room room = RoomManager.createRoom(roomName, this, maxPlayers, password);
        currentRoom = room;

        Packet resp = new Packet(PacketType.ROOM_CREATED);
        resp.data.put("roomId", room.getId());
        resp.data.put("roomName", room.getName());
        resp.data.put("maxPlayers", room.getMaxPlayers());
        resp.data.put("players", room.getPlayerNames());

        System.out.println("Room created: " + room.getName() + " (#" + room.getId() + ") by " + username);
        send(resp);
    }

    private void handleJoinRoom(Packet p) {
        int roomId = (int) p.data.get("roomId");
        String password = (String) p.data.get("password");
        
        Room room = RoomManager.getRoomById(roomId);
        
        if (room == null) {
            Packet resp = new Packet(PacketType.LOGIN_FAIL);
            resp.data.put("msg", "Room not found!");
            send(resp);
            return;
        }
        
        if (room.isFull()) {
            Packet resp = new Packet(PacketType.LOGIN_FAIL);
            resp.data.put("msg", "Room is full!");
            send(resp);
            return;
        }
        
        if (!room.checkPassword(password)) {
            Packet resp = new Packet(PacketType.LOGIN_FAIL);
            resp.data.put("msg", "Wrong password!");
            send(resp);
            return;
        }
        
        room.addPlayer(this);
        currentRoom = room;
        
        Packet resp = new Packet(PacketType.ROOM_JOINED);
        resp.data.put("roomId", room.getId());
        resp.data.put("roomName", room.getName());
        resp.data.put("maxPlayers", room.getMaxPlayers());
        resp.data.put("players", room.getPlayerNames());
        send(resp);
        
        // Notify other players
        broadcastToRoom(room, PacketType.ROOM_UPDATE, username + " joined the room");
        
        System.out.println(username + " joined room: " + room.getName());
    }

    private void handleLeaveRoom(Packet p) {
        if (currentRoom == null) {
            return;
        }
        
        Room room = currentRoom;
        room.removePlayer(this);
        
        // Notify others
        broadcastToRoom(room, PacketType.ROOM_UPDATE, username + " left the room");
        
        currentRoom = null;
        
        // Remove room if empty
        if (room.getPlayers().isEmpty()) {
            RoomManager.removeRoom(room.getId());
            System.out.println("Room " + room.getName() + " removed (empty)");
        }
        
        System.out.println(username + " left room: " + room.getName());
    }

    private void handlePlayerReady(Packet p) {
        if (currentRoom == null) return;
        
        boolean ready = (boolean) p.data.get("ready");
        broadcastToRoom(currentRoom, PacketType.ROOM_UPDATE, username + (ready ? " is ready!" : " is not ready"));
    }

    private void handleStartGame(Packet p) {
        if (currentRoom == null) return;
        if (!currentRoom.getHost().equals(this)) {
            Packet resp = new Packet(PacketType.LOGIN_FAIL);
            resp.data.put("msg", "Only host can start the game!");
            send(resp);
            return;
        }
        
        // Broadcast game start to all players in room
        broadcastToRoom(currentRoom, PacketType.START_GAME, "Game is starting!");
        System.out.println("Game started in room: " + currentRoom.getName());
    }

    private void broadcastToRoom(Room room, PacketType type, String message) {
        Packet p = new Packet(type);
        p.data.put("msg", message);
        p.data.put("eventType", "room_event");
        p.data.put("players", room.getPlayerNames());
        p.data.put("playerCount", room.getPlayerCount());
        
        for (ClientHandler client : room.getPlayers()) {
            client.send(p);
        }
    }


    private void send(Packet p) {
        try {
            out.writeObject(p);
            out.flush();
        } catch (IOException e) {
            System.out.println("Send error: " + e.getMessage());
        }
    }

    private void notifyEvent(ServerEvent event) {
        if (eventCallback != null) {
            eventCallback.accept(event);
        }
    }
}