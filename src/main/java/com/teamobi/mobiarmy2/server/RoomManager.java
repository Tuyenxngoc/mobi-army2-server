package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.model.Room;

public class RoomManager {
    private Room[] rooms;

    private static class SingletonHelper {
        private static final RoomManager INSTANCE = new RoomManager();
    }

    public static RoomManager getInstance() {
        return RoomManager.SingletonHelper.INSTANCE;
    }

    public Room[] getRooms() {
        return rooms;
    }

    public void init() {
        IServerConfig config = ServerManager.getInstance().getConfig();
        byte[] roomQuantities = config.getRoomQuantity();
        int totalRooms = 0;

        for (int quantity : roomQuantities) {
            totalRooms += quantity;
        }

        rooms = new Room[totalRooms];
        byte index = 0;

        for (byte type = 0; type < roomQuantities.length; type++) {
            int minXu = config.getRoomMinXu()[type];
            int maxXu = config.getRoomMaxXu()[type];
            byte minMap = config.getRoomMinMap()[type];
            byte maxMap = config.getRoomMaxMap()[type];
            byte numArea = config.getNumArea();
            byte maxPlayerFight = config.getMaxPlayerFight();
            byte numPlayerInitRoom = config.getNumPlayerInitRoom();
            byte roomIconType = config.getRoomIconType();

            for (byte roomCount = 0; roomCount < roomQuantities[type]; roomCount++) {
                byte[] mapCanSelected = null;
                boolean isContinuous = false;
                if (type == 5) {
                    mapCanSelected = config.getBossRoomMapLimit()[roomCount];
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
