package com.tank2d.client.core;

import java.net.*;
import com.tank2d.client.entity.Player;

public class GameClientUDP extends Thread {
    private final PlayPanel playPanel;
    private final String hostIp;
    private final int hostPort;
    private boolean running = true;

    public GameClientUDP(PlayPanel playPanel, String hostIp, int hostPort) {
        this.playPanel = playPanel;
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    @Override
    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress address = InetAddress.getByName(hostIp);
            System.out.println("[ClientUDP] Connected to host " + hostIp + ":" + hostPort);

            byte[] buffer = new byte[1024];

            while (running) {
                Player player = playPanel.getPlayer();
                String msg = "UPDATE " + player.getX() + " " + player.getY() + " " + player.getBodyAngle() + " " + player.getGunAngle();
                byte[] data = msg.getBytes();

                DatagramPacket packet = new DatagramPacket(data, data.length, address, hostPort);
                socket.send(packet);

                // Receive state update
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                socket.receive(response);
                String reply = new String(response.getData(), 0, response.getLength());

                // Format: STATE x y bodyAngle
                String[] parts = reply.split(" ");
                if (parts[0].equals("STATE")) {
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double angle = Double.parseDouble(parts[3]);
                    playPanel.applyRemoteState(x, y, angle);
                }

                Thread.sleep(50); // 20 updates per second
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopClient() {
        running = false;
    }
}
