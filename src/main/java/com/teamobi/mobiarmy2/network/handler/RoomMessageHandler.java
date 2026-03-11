package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.Room;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.RoomManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RoomMessageHandler extends BaseMessageHandler {
    private static final int REJOIN_COOLDOWN_MS = 3000;
    private long timeSinceLeftRoom;

    public RoomMessageHandler(Session session) {
        super(session);
    }

    public void sendRoomName() throws IOException {
        RoomManager roomManager = ApplicationContext.getInstance()
                .getBean(RoomManager.class);
        String[] names = RoomManager.BOSS_ROOM_NAME;
        int startMapBoss = roomManager.getStartMapBoss();
        Message ms = new Message(Cmd.CHANGE_ROOM_NAME);
        DataOutputStream ds = ms.writer();
        ds.writeByte(names.length);
        for (int i = 0; i < names.length; i++) {
            ds.writeByte(startMapBoss + i);
            ds.writeUTF(String.format("Phòng %d: %s", startMapBoss + i, names[i]));
            ds.writeByte(5);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleSendRoomList() throws IOException {
        if (us().isNotWaiting()) {
            return;
        }
        RoomManager roomManager = ApplicationContext.getInstance()
                .getBean(RoomManager.class);
        Message ms = new Message(Cmd.ROOM_LIST);
        DataOutputStream ds = ms.writer();
        for (Room room : roomManager.getRooms()) {
            ds.writeByte(room.getIndex());
            ds.writeByte(room.getStatus());
            ds.writeByte(room.getFightWaitsAvailable());
            ds.writeByte(room.getType());
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleEnteringRoom(Message ms) throws IOException {
        if (us().isNotWaiting()) {
            return;
        }
        Room[] rooms = ApplicationContext.getInstance()
                .getBean(RoomManager.class).getRooms();
        byte roomNumber = ms.reader().readByte();
        if (roomNumber < 0 || roomNumber >= rooms.length) {
            return;
        }
        Room room = rooms[roomNumber];
        if (room.getType() == 6 && !us().hasClan()) {
            us().sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
            return;
        }
        ms = new Message(Cmd.BOARD_LIST);
        DataOutputStream ds = ms.writer();
        ds.writeByte(roomNumber);
        for (FightWait fightWait : room.getFightWaits()) {
            if (fightWait.isFightWaitInvalid()) {
                continue;
            }
            ds.writeByte(fightWait.getId());
            ds.writeByte(fightWait.getNumPlayers());
            ds.writeByte(fightWait.getMaxSetPlayers());
            ds.writeBoolean(fightWait.isPassSet());
            ds.writeInt(fightWait.getMoney());
            ds.writeBoolean(fightWait.isStarted());
            ds.writeUTF(fightWait.getName());
            ds.writeByte(fightWait.getRoom().getIconType());
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleJoinBoard(Message ms) throws IOException {
        if (us().isNotWaiting()) {
            return;
        }

        long timeRemaining = REJOIN_COOLDOWN_MS - (System.currentTimeMillis() - timeSinceLeftRoom);
        if (timeRemaining > 0) {
            us().sendServerMessage(GameString.createJoinAreaErrorMessage((int) (timeRemaining / 1000) + 1));
            return;
        }

        Room[] rooms = ApplicationContext.getInstance()
                .getBean(RoomManager.class).getRooms();
        DataInputStream dis = ms.reader();
        byte roomNumber = dis.readByte();
        byte areaNumber = dis.readByte();
        String password = dis.readUTF().trim();
        if (roomNumber < 0 || roomNumber >= rooms.length) {
            return;
        }
        FightWait[] fightWaits = rooms[roomNumber].getFightWaits();
        if (areaNumber < 0 || areaNumber >= fightWaits.length) {
            return;
        }
        FightWait fightWait = fightWaits[areaNumber];
        if (fightWait.isPassSet() && !fightWait.getPassword().equals(password)) {
            us().sendServerMessage(GameString.AREA_INCORRECT_PASSWORD);
            return;
        }
        fightWait.addUser(us());
    }

    public void handleLeaveBoard() {
        if (us().getState() != UserState.WAIT_FIGHT && us().getState() != UserState.FIGHTING) {
            return;
        }
        fw().leaveTeam(us().getUserId());

        timeSinceLeftRoom = System.currentTimeMillis();
    }

    public void handleJoinAnyBoard(Message ms) throws IOException {
        RoomManager roomManager = ApplicationContext.getInstance()
                .getBean(RoomManager.class);
        Room[] rooms = roomManager.getRooms();

        FightWait fightWait = null;
        int type = ms.reader().readByte();
        switch (type) {
            // Đấu trùm
            case 5 -> {
                int start = roomManager.getStartMapBoss();
                int end = start + RoomManager.ROOM_QUANTITY[5];

                outerLoop:
                for (int i = start; i < end; i++) {
                    Room room = rooms[i];
                    for (FightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted() &&
                                !fight.isPassSet() &&
                                !fight.isContinuous() &&
                                fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                                fight.getMoney() <= us().getXu()
                        ) {
                            fightWait = fight;
                            break outerLoop;
                        }
                    }
                }
            }

            //4vs4->1vs1
            case 4, 3, 2, 1 -> {
                int end = roomManager.getStartMapBoss();
                int index = Utils.nextInt(0, end - 1);
                Room room = rooms[index];
                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted() &&
                            !fight.isPassSet() &&
                            fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                            fight.getMoney() <= us().getXu() &&
                            fight.getMaxSetPlayers() == type * 2
                    ) {
                        fightWait = fight;
                        break;
                    }
                }
            }

            //Khu vực trống
            case 0 -> {
                int end = roomManager.getStartMapBoss();
                int index = Utils.nextInt(0, end - 1);
                Room room = rooms[index];
                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted() &&
                            !fight.isPassSet() &&
                            fight.getMoney() <= us().getXu() &&
                            fight.getNumPlayers() == 0
                    ) {
                        fightWait = fight;
                        break;
                    }
                }
            }

            //Ngẫu nhiên
            case -1 -> {
                int end = roomManager.getStartMapBoss();
                int index = Utils.nextInt(0, end - 1);
                Room room = rooms[index];
                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted() &&
                            !fight.isPassSet() &&
                            fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                            fight.getMoney() <= us().getXu()
                    ) {
                        fightWait = fight;
                        break;
                    }
                }
            }
        }

        if (fightWait == null) {
            us().sendMoneyErrorMessage(GameString.AREA_NOT_FOUND);
        } else {
            fightWait.sendInfo(us());
            fightWait.addUser(us());
        }
    }
}
