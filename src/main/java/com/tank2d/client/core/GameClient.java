package com.tank2d.client.core;

import com.tank2d.client.entity.Player;
import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;
import javafx.scene.shape.Polygon;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<String> messageCallback;
    private GameMiniServer miniServer;
    private GameClientUDP udpClient;

    public GameClient(Consumer<String> messageCallback) {
        this.messageCallback = messageCallback;
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
            messageCallback.accept("Send failed!");
        }
    }

    private void listen() {
        try {
            while (true) {
                Packet p = (Packet) in.readObject();

                switch (p.type) {
                    case LOGIN_OK -> messageCallback.accept("" + p.data.get("msg"));
                    case LOGIN_FAIL -> messageCallback.accept("" + p.data.get("msg"));
                    case REGISTER_OK -> messageCallback.accept("" + p.data.get("msg"));
                    case REGISTER_FAIL -> messageCallback.accept("" + p.data.get("msg"));

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
            messageCallback.accept("Disconnected from server");
        }
    }

}