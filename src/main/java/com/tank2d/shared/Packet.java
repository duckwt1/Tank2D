package com.tank2d.shared;

import java.io.Serializable;
import java.util.HashMap;

public class Packet implements Serializable {
    public PacketType type;
    public HashMap<String, Object> data = new HashMap<>();

    public Packet(PacketType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "Packet{" + "type=" + type + ", data=" + data + '}';
    }
}