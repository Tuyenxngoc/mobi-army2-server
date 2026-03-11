package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.entity.Room;
import lombok.Getter;

public class RoomManager {
    @Getter
    private Room[] rooms;
    @Getter
    private int startMapBoss;

    public void init() {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
        byte[] roomQuantities = GameConstants.ROOM_QUANTITY;
        int totalRooms = 0;

        for (int quantity : roomQuantities) {
            totalRooms += quantity;
        }

        // Calculate startMapBoss: index where boss rooms start (type 5)
        this.startMapBoss = 0;
        for (int i = 0; i < 5; i++) {
            this.startMapBoss += roomQuantities[i];
        }

        rooms = new Room[totalRooms];
        byte index = 0;

        for (byte type = 0; type < roomQuantities.length; type++) {
            int minXu = GameConstants.ROOM_MIN_XU[type];
            int maxXu = GameConstants.ROOM_MAX_XU[type];
            byte minMap = GameConstants.ROOM_MIN_MAP[type];
            byte maxMap = GameConstants.ROOM_MAX_MAP[type];
            byte numArea = GameConstants.NUM_AREA;
            byte maxPlayerFight = GameConstants.MAX_PLAYER_FIGHT;
            byte numPlayerInitRoom = GameConstants.NUM_PLAYER_INIT_ROOM;
            byte roomIconType = GameConstants.ROOM_ICON_TYPE;

            for (byte roomCount = 0; roomCount < roomQuantities[type]; roomCount++) {
                byte[] mapCanSelected = null;
                boolean isContinuous = false;
                if (type == 5) {
                    mapCanSelected = GameConstants.BOSS_ROOM_MAP_LIMIT[roomCount];
                    if (roomCount == 9) {
                        isContinuous = true;
                    }
                }

                rooms[index] = new Room(index, type, minXu, maxXu, minMap, maxMap, mapCanSelected, isContinuous, numArea, maxPlayerFight, numPlayerInitRoom, roomIconType);
                index++;
            }
        }
    }
}
