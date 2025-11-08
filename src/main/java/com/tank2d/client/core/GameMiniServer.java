package com.tank2d.client.core;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import com.tank2d.client.entity.Player;

public class GameMiniServer extends Thread {
    private final PlayPanel playPanel;
    private final int port;
    private boolean running = true;

    public GameMiniServer(PlayPanel playPanel, int port) {
        this.playPanel = playPanel;
        this.port = port;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket(port)) {
            System.out.println("[MiniServer] Running on UDP port " + port);
            byte[] buffer = new byte[1024];

            while (running) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String msg = new String(packet.getData(), 0, packet.getLength());

                // Parse message: UPDATE x y bodyAngle gunAngle
                String[] parts = msg.split(" ");
                if (parts[0].equals("UPDATE")) {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double bodyAngle = Double.parseDouble(parts[3]);
                    double gunAngle = Double.parseDouble(parts[4]);

                    // Apply state from client
                    playPanel.applyRemoteState(x, y, bodyAngle);
                    playPanel.getPlayer().setGunAngle(gunAngle);
                }

                // Send back confirmation
                Player p = playPanel.getPlayer();
                String reply = "STATE " + p.getX() + " " + p.getY() + " " + p.getBodyAngle();
                byte[] response = reply.getBytes();
                DatagramPacket resp = new DatagramPacket(response, response.length, packet.getAddress(), packet.getPort());
                socket.send(resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
    }
}
