package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.entity.Room;
import lombok.Getter;

public class RoomManager {
    @Getter
    private Room[] rooms;

    public void init() {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
        byte[] roomQuantities = serverConfig.getRoomQuantity();
        int totalRooms = 0;

        for (int quantity : roomQuantities) {
            totalRooms += quantity;
        }

        rooms = new Room[totalRooms];
        byte index = 0;

        for (byte type = 0; type < roomQuantities.length; type++) {
            int minXu = serverConfig.getRoomMinXu()[type];
            int maxXu = serverConfig.getRoomMaxXu()[type];
            byte minMap = serverConfig.getRoomMinMap()[type];
            byte maxMap = serverConfig.getRoomMaxMap()[type];
            byte numArea = serverConfig.getNumArea();
            byte maxPlayerFight = serverConfig.getMaxPlayerFight();
            byte numPlayerInitRoom = serverConfig.getNumPlayerInitRoom();
            byte roomIconType = serverConfig.getRoomIconType();

            for (byte roomCount = 0; roomCount < roomQuantities[type]; roomCount++) {
                byte[] mapCanSelected = null;
                boolean isContinuous = false;
                if (type == 5) {
                    mapCanSelected = serverConfig.getBossRoomMapLimit()[roomCount];
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
