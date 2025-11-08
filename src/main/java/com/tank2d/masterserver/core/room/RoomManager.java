package com.tank2d.masterserver.core.room;

import com.tank2d.masterserver.core.ClientHandler;
import java.util.*;

public class RoomManager {
    private static final Map<Integer, Room> rooms = new HashMap<>();
    private static int nextId = 1;

    public static synchronized Room createRoom(String name, ClientHandler host, int maxPlayers, String password) {
        Room room = new Room(nextId++, name, host, maxPlayers, password);
        rooms.put(room.getId(), room);
        return room;
    }

    public static Collection<Room> getRooms() {
        return rooms.values();
    }

    public static Room getRoomById(int id) {
        return rooms.get(id);
    }

    public static synchronized void removeRoom(int id) {
        rooms.remove(id);
    }
    
    public static synchronized void removeEmptyRooms() {
        rooms.entrySet().removeIf(entry -> entry.getValue().getPlayers().isEmpty());
    }
}
