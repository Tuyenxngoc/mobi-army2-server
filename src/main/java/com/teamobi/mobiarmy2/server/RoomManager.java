package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.entity.Room;
import lombok.Getter;

public class RoomManager {
    public static final String[] ROOM_NAME_VI = {"PHÒNG SƠ CẤP", "PHÒNG TRUNG CẤP", "PHÒNG VIP", "PHÒNG ĐẤU TRƯỜNG", "PHÒNG TỰ DO", "PHÒNG ĐẤU TRÙM", "PHÒNG ĐẤU ĐỘI"};
    public static final String[] ROOM_NAME_EN = {"NEWBIE ROOM", "INTERMEDIATE ROOM", "VIP ROOM", "ARENA", "FREEDOM ROOM", "BOSS BATTLE ROOM", "CLAN BATTLE ROOM"};
    public static final String[] BOSS_ROOM_NAME = {"Bom", "Nhện máy", "Người máy", "T-rex máy", "UFO", "Khí cầu", "Nhện độc", "Ma", "Trùm tự chọn", "Đấu trùm liên hoàn"};
    public static final byte[] ROOM_QUANTITY = {25, 3, 1, 3, 1, 10, 2};
    public static final int[] ROOM_MAX_XU = {1000, 5000, 10000, 5000, 10000, 100, 100000000};
    public static final int[] ROOM_MIN_XU = {0, 1000, 5000, 2000, 0, 100, 0};
    public static final byte[][] BOSS_ROOM_MAP_LIMIT = {{30, 31}, {32}, {33}, {34}, {35}, {36}, {37}, {38, 39}, null, null};
    public static final byte[] BOSS_ROOM_BOSS_ID = {12, 12, 13, 14, 15, 16, 17, 22, 25, 26};
    public static final byte[] BOSS_ROOM_MAP_ID = {30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
    public static final byte[] ROOM_MAX_MAP = {29, 29, 29, 29, 29, 39, 29};
    public static final byte[] ROOM_MIN_MAP = {0, 0, 0, 0, 0, 30, 0};
    public static final byte NUM_AREA = 20;
    public static final byte MAX_PLAYER_FIGHT = 8;
    public static final byte MAX_ELEMENT_FIGHT = 100;
    public static final byte NUM_PLAYER_PER_ROOM = 12;
    public static final byte NUM_PLAYER_INIT_ROOM = 8;
    public static final byte TRAINING_MAP_ID = 0;
    public static final byte ROOM_ICON_TYPE = 0;

    @Getter
    private Room[] rooms;
    @Getter
    private int startMapBoss;

    public void init() {
        int totalRooms = 0;

        for (int quantity : ROOM_QUANTITY) {
            totalRooms += quantity;
        }

        // Calculate startMapBoss: index where boss rooms start (type 5)
        this.startMapBoss = 0;
        for (int i = 0; i < 5; i++) {
            this.startMapBoss += ROOM_QUANTITY[i];
        }

        rooms = new Room[totalRooms];
        byte index = 0;

        for (byte type = 0; type < ROOM_QUANTITY.length; type++) {
            int minXu = ROOM_MIN_XU[type];
            int maxXu = ROOM_MAX_XU[type];
            byte minMap = ROOM_MIN_MAP[type];
            byte maxMap = ROOM_MAX_MAP[type];

            for (byte roomCount = 0; roomCount < ROOM_QUANTITY[type]; roomCount++) {
                byte[] mapCanSelected = null;
                boolean isContinuous = false;
                if (type == 5) {
                    mapCanSelected = BOSS_ROOM_MAP_LIMIT[roomCount];
                    if (roomCount == 9) {
                        isContinuous = true;
                    }
                }

                rooms[index] = new Room(index, type, minXu, maxXu, minMap, maxMap, mapCanSelected, isContinuous, NUM_AREA, MAX_PLAYER_FIGHT, NUM_PLAYER_INIT_ROOM, ROOM_ICON_TYPE);
                index++;
            }
        }
    }
}
