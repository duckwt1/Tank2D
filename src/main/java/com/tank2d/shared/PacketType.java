package com.tank2d.shared;

// Gui goi tin theo dinh dang : packettype:data

/**
 * Định nghĩa tất cả các loại gói tin (packet)
 * được gửi qua lại giữa CLIENT ↔ SERVER.
 *
 * Giai đoạn này tập trung vào LOGIN / REGISTER.
 * Sau này có thể mở rộng thêm: ROOM, CHAT, GAMEPLAY...
 */
public enum PacketType {
    // === AUTHENTICATION ===
    LOGIN,
    LOGIN_OK,
    LOGIN_FAIL,

    REGISTER,
    REGISTER_OK,
    REGISTER_FAIL,

    // === SHOP / INVENTORY ===
    BUY,
    BUY_OK,
    BUY_FAIL,


    CREATE_ROOM_HOST,
    CREATE_ROOM_CLIENT
}