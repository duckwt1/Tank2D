package com.tank2d.masterserver.core;

import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import com.tank2d.masterserver.ui.MasterServerDashboard.ServerEvent;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private String username;
    private Consumer<ServerEvent> eventCallback;
    private String clientIP;

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

    private void handlePacket(Packet p) {
        switch (p.type) {
            case LOGIN -> handleLogin(p);
            case REGISTER -> handleRegister(p);
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