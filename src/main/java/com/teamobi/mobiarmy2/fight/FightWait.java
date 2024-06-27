package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.model.Room;
import com.teamobi.mobiarmy2.server.ServerManager;
import lombok.Getter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
public class FightWait {
    public static final byte[] CONTINUOUS_MAPS = new byte[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39};

    public final User[] users;
    public final boolean[] readies;
    public FightManager fightManager;
    public final Room parentRoom;
    public final byte id;
    public final int[][] items;
    public boolean started;
    public int numReadyPlayers;
    public int maxSetPlayers;
    public int maxInitialPlayers;
    public int maxPlayer;
    public int numPlayers;
    public boolean isPassSet;
    public String password;
    public int money;
    public String name;
    public byte type;
    public byte teaFree;
    public byte mapId;
    public int bossIndex;
    public Thread bossKickThread;
    public long startTime;
    public boolean isContinuous;
    public byte continuousLevel;

    public FightWait(Room parentRoom, byte type, byte id, byte maxPlayers, byte maxInitialPlayers, byte mapId, byte teaFree, boolean isContinuous, boolean isSieuBoss) {
        this.parentRoom = parentRoom;
        this.type = type;
        this.id = id;
        this.maxPlayer = maxPlayers;
        this.maxInitialPlayers = maxInitialPlayers;
        this.maxSetPlayers = maxInitialPlayers;
        this.numPlayers = 0;
        this.numReadyPlayers = 0;
        this.users = new User[maxPlayers];
        this.readies = new boolean[maxPlayers];
        this.items = new int[maxPlayers][8];
        this.teaFree = teaFree;
        this.money = this.parentRoom.minXu;
        this.name = "";
        this.password = "";
        this.isContinuous = isContinuous;
        this.continuousLevel = (byte) (isContinuous ? 0 : -1);
        this.mapId = isContinuous ? CONTINUOUS_MAPS[continuousLevel] : mapId;
        this.fightManager = new FightManager();
        this.started = false;
        this.bossIndex = -1;
        this.startTime = 0L;
    }

    private User getRoomOwner() {
        return users[bossIndex];
    }

    public boolean isFightWaitInvalid() {
        return numPlayers == maxSetPlayers || started || (isContinuous && continuousLevel > 0);
    }

    public void enterFireOval(User us) throws IOException {
        if (type == 6 && us.getClanId() == 0) {
            us.getUserService().sendServerMessage(GameString.notClan());
            return;
        }
        if (started || (isContinuous && continuousLevel > 0)) {
            us.getUserService().sendServerMessage(GameString.joinKVError0());
            return;
        }
        if (money > us.getXu()) {
            us.getUserService().sendServerMessage(GameString.joinKVError2());
            return;
        }
        if (numPlayers >= maxSetPlayers) {
            us.getUserService().sendServerMessage(GameString.joinKVError3());
            return;
        }

        Message ms;
        DataOutputStream ds;
        synchronized (users) {
            int bestLocation = -1;
            for (byte i = 0; i < users.length; i++) {
                if (users[i] == null) {
                    bestLocation = i;
                    break;
                }
            }
            if (bestLocation == -1) {
                return;
            }
            us.setFightWait(this);
            if (numPlayers == 0) {
                changeBoss(bestLocation);
            }
            ms = new Message(Cmd.SOMEONE_JOINBOARD);
            ds = ms.writer();
            ds.writeByte(bestLocation);
            ds.writeInt(us.getPlayerId());
            ds.writeShort(us.getClanId());
            ds.writeUTF(us.getUsername());
            ds.writeByte(us.getCurrentLevel());
            ds.writeByte(us.getNvUsed());
            for (short i : us.getEquip()) {
                ds.writeShort(i);
            }
            ds.flush();
            sendToTeam(ms);

            users[bestLocation] = us;
            readies[bestLocation] = false;
            numPlayers++;

            ms = new Message(Cmd.JOIN_BOARD);
            ds = ms.writer();
            ds.writeInt(getRoomOwner().getPlayerId());
            ds.writeInt(money);
            ds.writeByte(mapId);
            ds.writeByte(0);
            for (byte i = 0; i < users.length; i++) {
                User user = users[i];
                if (user != null) {
                    ds.writeInt(user.getPlayerId());
                    ds.writeShort(user.getClanId());
                    ds.writeUTF(user.getUsername());
                    ds.writeInt(0);
                    ds.writeByte(user.getCurrentLevel());
                    ds.writeByte(user.getNvUsed());
                    for (short k : user.getEquip()) {
                        ds.writeShort(k);
                    }
                    ds.writeBoolean(readies[i]);
                } else {
                    ds.writeInt(-1);
                }
            }
            ds.flush();
            us.sendMessage(ms);

            // Update khu vuc
            ms = new Message(76);
            ds = ms.writer();
            ds.writeByte(parentRoom.id);
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.writeByte(parentRoom.type);
            ds.flush();
            us.sendMessage(ms);

            // Send map
            ms = new Message(75);
            ds = ms.writer();
            ds.writeByte(mapId);
            ds.flush();
            us.sendMessage(ms);
        }
    }

    private void changeBoss(int index) {
        bossIndex = index;
        if (bossKickThread != null) {
            bossKickThread.interrupt();
        }
        bossKickThread = new Thread(() -> {
            try {
                int second = 300;
                while (!started && bossIndex != -1) {
                    Thread.sleep(1000L);
                    second--;
                    if (second == 0) {
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        });
        bossKickThread.start();
    }

    public void sendToTeam(Message ms) {
        for (User user : users) {
            if (user != null) {
                user.sendMessage(ms);
            }
        }
    }

    private int getUserIndexByPlayerId(int playerId) {
        for (byte i = 0; i < users.length; i++) {
            User user = this.users[i];
            if (user == null) {
                continue;
            }
            if (user.getPlayerId() == playerId) {
                return i;
            }
        }
        return -1;
    }

    public void setPassRoom(String password, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        //Set new password
        isPassSet = true;
        this.password = password;
    }

    public void setMoney(int xu, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        if (xu < parentRoom.minXu || xu > parentRoom.maxXu) {
            getRoomOwner().getUserService().sendServerMessage(GameString.datCuocError1(parentRoom.minXu, parentRoom.maxXu));
            return;
        }
        if (getRoomOwner().getXu() < xu) {
            getRoomOwner().getUserService().sendServerMessage(GameString.xuNotEnought());
            return;
        }
        // Reset ready
        resetReadies();
        // Set new money
        money = xu;
        try {
            Message ms = new Message(19);
            DataOutputStream ds = ms.writer();
            ds.writeShort(0);
            ds.writeInt(xu);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetReadies() {
        Arrays.fill(readies, false);
        readies[bossIndex] = true;
        numReadyPlayers = 0;
    }

    public void setReady(boolean ready, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() == playerId) {
            return;
        }

        int index = getUserIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }
        // Set new player ready
        if (readies[index] != ready) {
            readies[index] = ready;
            if (ready) {
                numReadyPlayers++;
            } else {
                numReadyPlayers--;
            }
        }
        try {
            Message ms = new Message(16);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeBoolean(ready);
            ds.flush();
            this.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void kickPlayer(int playerId, int targetPlayerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        int index = getUserIndexByPlayerId(targetPlayerId);
        if (index == -1) {
            return;
        }
        if (readies[index]) {
            return;
        }
        if (users[index].isOpeningGift()) {
            getRoomOwner().getUserService().sendServerMessage(GameString.openingGift(users[index].getUsername()));
            return;
        }

        try {
            Message ms = new Message(Cmd.KICK);
            DataOutputStream ds = ms.writer();
            ds.writeShort(index);
            ds.writeInt(targetPlayerId);
            ds.writeUTF(GameString.kickString());
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // todo leave
        leaveTeam(targetPlayerId);
    }

    private void leaveTeam(int targetPlayerId) {
        if (started) {
            fightManager.leaveFight(targetPlayerId);
        }
        removeUser(targetPlayerId);
        if (numPlayers == 0) {
            return;
        }
        try {
            Message ms = new Message(Cmd.SOMEONE_LEAVEBOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(targetPlayerId);
            ds.writeInt(getRoomOwner().getPlayerId());
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeUser(int playerId) {
        synchronized (users) {
            for (int i = 0; i < users.length; i++) {
                if (users[i] != null && users[i].getPlayerId() == playerId) {
                    users[i] = null;
                    numPlayers--;
                    if (numPlayers == 0) {
                        refreshFightWait();
                    } else {
                        if (bossIndex == i) {
                            findNewBoss();
                        }
                    }
                    break;
                }
            }
        }
    }

    private void findNewBoss() {
        for (byte i = 0; i < users.length; i++) {
            if (users[i] != null) {
                changeBoss(i);
                break;
            }
        }
    }

    private void refreshFightWait() {
        money = parentRoom.minXu;
        name = "";
        password = "";
        isPassSet = false;
        bossIndex = -1;
    }

    public void chatMessage(int playerId, String message) {
        int index = getUserIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }
        if (started) {
            fightManager.chatMessage(playerId, message);
            return;
        }
        try {
            Message ms = new Message(Cmd.CHAT_TO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeUTF(message);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startGame(int playerId) {

        if (started || getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        started = true;
        //fightManager.startGame();
    }

    public void setRoomName(int playerId, String name) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        this.name = name;
    }

    public void setMaxPlayers(int playerId, byte maxPlayers) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        if (maxPlayers > 0 && maxPlayers < 9 && maxPlayers % 2 == 0 && numPlayers < maxPlayers) {
            maxSetPlayers = maxPlayers;
        }
    }

    public void setMap(int playerId, byte mapIdSet) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        if (isContinuous) {
            getRoomOwner().getUserService().sendServerMessage(GameString.selectMapError1_3());
            return;
        }
        for (User user : users) {
            if (user == null) {
                continue;
            }
            if (user.isOpeningGift()) {
                user.getUserService().sendServerMessage(GameString.openingGift(user.getUsername()));
                return;
            }
        }

        //todo check map can select

        mapId = mapIdSet;
        if (mapId == 27) {
            mapId = MapData.randomMap(27);
        }
        resetReadies();
        try {
            Message ms = new Message(Cmd.MAP_SELECT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(mapId);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void findPlayer(int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }
        List<User> userList = ServerManager.getInstance().findWaitPlayers(playerId);
        try {
            Message ms = new Message(Cmd.FIND_PLAYER);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(true);
            ds.writeByte(userList.size());
            for (User u : userList) {
                ds.writeUTF(u.getUsername());
                ds.writeInt(u.getPlayerId());
                ds.writeByte(u.getNvUsed());
                ds.writeInt(u.getXu());
                ds.writeByte(u.getCurrentLevel());
                ds.writeByte(u.getCurrentLevelPercent());
                for (short j : u.getEquip()) {
                    ds.writeShort(j);
                }
            }
            ds.flush();
            getRoomOwner().sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inviteToRoom(int playerId) {
        User user = ServerManager.getInstance().getUserByPlayerId(playerId);
        if (user == null) {
            getRoomOwner().getUserService().sendServerMessage(GameString.inviteError1());
            return;
        }
        if (user.isNotWaiting()) {
            getRoomOwner().getUserService().sendServerMessage(GameString.inviteError2());
            return;
        }
        try {
            Message ms = new Message(Cmd.FIND_PLAYER);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(false);
            ds.writeUTF(GameString.inviteMessage(user.getUsername()));
            ds.writeByte(this.parentRoom.id);
            ds.writeByte(this.id);
            ds.writeUTF(this.password);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}