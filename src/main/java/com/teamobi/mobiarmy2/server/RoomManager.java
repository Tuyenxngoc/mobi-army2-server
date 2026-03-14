package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.RoomConfig;
import com.teamobi.mobiarmy2.entity.Room;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.util.RandomUtil;
import lombok.Getter;

public class RoomManager {
    // Các hằng số cấu hình cho phòng đấu trùm
    public static final String[] BOSS_ROOM_NAME = {"Bom", "Nhện máy", "Người máy", "T-rex máy", "UFO", "Khí cầu", "Nhện độc", "Ma", "Trùm tự chọn", "Đấu trùm liên hoàn"};
    public static final byte[][] BOSS_ROOM_MAP_LIMIT = {{30, 31}, {32}, {33}, {34}, {35}, {36}, {37}, {38, 39}, null, null};
    public static final byte[] BOSS_ROOM_BOSS_ID = {12, 12, 13, 14, 15, 16, 17, 22, 25, 26};
    public static final byte[] BOSS_ROOM_MAP_ID = {30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
    public static final byte ROOM_BOSS_TYPE = 5;

    public static final byte NUM_AREA = 20;
    public static final byte MAX_PLAYER_FIGHT = 8;
    public static final byte MAX_ELEMENT_FIGHT = 100;
    public static final byte NUM_PLAYER_PER_ROOM = 12;
    public static final byte NUM_PLAYER_INIT_ROOM = 8;
    public static final byte TRAINING_MAP_ID = 0;
    public static final byte ROOM_ICON_TYPE = 0;

    public static final RoomConfig[] ROOM_CONFIGS = {
            new RoomConfig("PHÒNG SƠ CẤP", "NEWBIE ROOM", 25, 0, 1000, (byte) 0, (byte) 29),
            new RoomConfig("PHÒNG TRUNG CẤP", "INTERMEDIATE ROOM", 3, 1000, 5000, (byte) 0, (byte) 29),
            new RoomConfig("PHÒNG VIP", "VIP ROOM", 1, 5000, 10000, (byte) 0, (byte) 29),
            new RoomConfig("PHÒNG ĐẤU TRƯỜNG", "ARENA", 3, 2000, 5000, (byte) 0, (byte) 29),
            new RoomConfig("PHÒNG TỰ DO", "FREEDOM ROOM", 1, 0, 10000, (byte) 0, (byte) 29),
            new RoomConfig("PHÒNG ĐẤU TRÙM", "BOSS ROOM", 10, 100, 100, (byte) 30, (byte) 39),
            new RoomConfig("PHÒNG ĐẤU ĐỘI", "CLAN ROOM", 2, 0, 100000000, (byte) 0, (byte) 29)
    };

    @Getter
    private Room[] rooms;

    @Getter
    private int startMapBoss;

    public void init() {

        int totalRooms = 0;

        for (RoomConfig config : ROOM_CONFIGS) {
            totalRooms += config.getQuantity();
        }

        // Tính index bắt đầu boss room
        startMapBoss = 0;
        for (int i = 0; i < ROOM_BOSS_TYPE; i++) {
            startMapBoss += ROOM_CONFIGS[i].getQuantity();
        }

        rooms = new Room[totalRooms];

        byte index = 0;

        for (byte type = 0; type < ROOM_CONFIGS.length; type++) {

            RoomConfig config = ROOM_CONFIGS[type];

            for (byte roomCount = 0; roomCount < config.getQuantity(); roomCount++) {

                byte[] mapCanSelected = null;
                boolean isContinuous = false;// Đánh dấu map liên hoàn

                if (type == ROOM_BOSS_TYPE) {
                    mapCanSelected = BOSS_ROOM_MAP_LIMIT[roomCount];

                    if (roomCount == 9) {
                        isContinuous = true;
                    }
                }

                rooms[index] = new Room(
                        index,
                        type,
                        config.getMinXu(),
                        config.getMaxXu(),
                        config.getMinMap(),
                        config.getMaxMap(),
                        mapCanSelected,
                        isContinuous,
                        NUM_AREA,
                        MAX_PLAYER_FIGHT,
                        NUM_PLAYER_INIT_ROOM,
                        ROOM_ICON_TYPE
                );

                index++;
            }
        }
    }

    public FightWait findRandomFightWait(int type, int playerXu) {

        FightWait fightWait = null;

        switch (type) {

            case 5 -> {
                int start = startMapBoss;
                int end = start + ROOM_CONFIGS[5].getQuantity();

                outerLoop:
                for (int i = start; i < end; i++) {
                    Room room = rooms[i];

                    for (FightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted()
                                && !fight.isPassSet()
                                && !fight.isContinuous()
                                && fight.getNumPlayers() < fight.getMaxSetPlayers()
                                && fight.getMoney() <= playerXu) {

                            fightWait = fight;
                            break outerLoop;
                        }
                    }
                }
            }

            case 4, 3, 2, 1 -> {
                int end = startMapBoss;
                int index = RandomUtil.nextInt(0, end - 1);

                Room room = rooms[index];

                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted()
                            && !fight.isPassSet()
                            && fight.getNumPlayers() < fight.getMaxSetPlayers()
                            && fight.getMoney() <= playerXu
                            && fight.getMaxSetPlayers() == type * 2) {

                        fightWait = fight;
                        break;
                    }
                }
            }

            case 0 -> {
                int end = startMapBoss;
                int index = RandomUtil.nextInt(0, end - 1);

                Room room = rooms[index];

                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted()
                            && !fight.isPassSet()
                            && fight.getMoney() <= playerXu
                            && fight.getNumPlayers() == 0) {

                        fightWait = fight;
                        break;
                    }
                }
            }

            case -1 -> {
                int end = startMapBoss;
                int index = RandomUtil.nextInt(0, end - 1);

                Room room = rooms[index];

                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted()
                            && !fight.isPassSet()
                            && fight.getNumPlayers() < fight.getMaxSetPlayers()
                            && fight.getMoney() <= playerXu) {

                        fightWait = fight;
                        break;
                    }
                }
            }
        }

        return fightWait;
    }
}
