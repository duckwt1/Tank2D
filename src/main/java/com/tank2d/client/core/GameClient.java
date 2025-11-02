package com.tank2d.client.core;

import com.tank2d.shared.Packet;
import com.tank2d.shared.PacketType;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class GameClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Consumer<String> messageCallback;

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
                }
            }
        } catch (Exception e) {
            messageCallback.accept("Disconnected from server");
        }
    }
}