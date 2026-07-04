package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.Room;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.RoomManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RoomMessageHandler extends BaseMessageHandler {
    private static final int REJOIN_COOLDOWN_MS = 3000;
    private long timeSinceLeftRoom;

    private final RoomManager roomManager;

    public RoomMessageHandler(Session session, RoomManager roomManager) {
        super(session);
        this.roomManager = roomManager;
    }

    public void sendRoomName() throws IOException {
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
        Room[] rooms = roomManager.getRooms();
        byte roomNumber = ms.reader().readByte();
        if (roomNumber < 0 || roomNumber >= rooms.length) {
            return;
        }
        Room room = rooms[roomNumber];
        if (room.getType() == 6 && !us().hasClan()) {
            messageSender.sendServerMessage(us(), GameString.NO_CLAN_MEMBERSHIP);
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
            messageSender.sendServerMessage(us(), GameString.createJoinAreaErrorMessage((int) (timeRemaining / 1000) + 1));
            return;
        }

        Room[] rooms = roomManager.getRooms();
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
            messageSender.sendServerMessage(us(), GameString.AREA_INCORRECT_PASSWORD);
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

        int type = ms.reader().readByte();

        FightWait fightWait = roomManager.findRandomFightWait(type, us().getXu());

        if (fightWait == null) {
            messageSender.sendMoneyErrorMessage(us(), GameString.AREA_NOT_FOUND);
        } else {
            fightWait.sendInfo(us());
            fightWait.addUser(us());
        }
    }
}
