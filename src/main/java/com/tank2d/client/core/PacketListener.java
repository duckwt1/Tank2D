package com.tank2d.client.core;

import com.tank2d.shared.Packet;

import java.util.List;
import java.util.Map;

/**
 * Interface for handling packets received from server.
 * Controllers should implement this to handle server responses.
 */
public interface PacketListener {
    
    // Authentication events
    default void onLoginSuccess(String message) {}
    default void onLoginFail(String message) {}
    default void onRegisterSuccess(String message) {}
    default void onRegisterFail(String message) {}
    
    // Room events
    default void onRoomCreated(int roomId, String roomName, int maxPlayers, List<String> players) {}
    default void onRoomJoined(int roomId, String roomName, int maxPlayers, List<String> players) {}
    default void onRoomUpdate(String message, List<String> players) {}
    default void onRoomListReceived(List<Map<String, Object>> rooms) {}
    
    // Game events
    default void onGameStart(Packet p) {}
    
    // Connection events
    default void onDisconnected() {}
}
