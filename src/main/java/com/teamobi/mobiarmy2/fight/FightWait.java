package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.model.FightItemData;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.Room;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ServerManager;
import lombok.Getter;
import lombok.Setter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FightWait {
    private final FightManager fightManager;
    private final Room room;
    private final byte id;
    private User[] users;
    private boolean[] readies;
    private byte[][] items;
    private boolean started;
    private byte numReady;
    private byte maxSetPlayers;
    private byte numPlayers;
    private boolean isPassSet;
    private String password;
    public int money;
    private String name;
    public byte mapId;
    private byte bossIndex;
    private Thread bossKickThread;
    private long startTime;
    public byte continuousLevel;

    public FightWait(Room room, byte id) {
        this.room = room;
        this.id = id;

        byte maxPlayers = room.getMaxPlayerFight();

        this.fightManager = new FightManager(this);
        this.users = new User[maxPlayers];
        this.items = new byte[maxPlayers][8];
        this.readies = new boolean[maxPlayers];

        this.name = "";
        this.password = "";
        this.isPassSet = false;
        this.started = false;
        this.numReady = 0;
        this.numPlayers = 0;
        this.startTime = 0L;
        this.continuousLevel = (byte) (room.isContinuous() ? 0 : -1);

        this.mapId = room.getMapId();
        this.money = room.getMinXu();

        this.maxSetPlayers = room.getNumPlayerInitRoom();
    }

    private User getRoomOwner() {
        return users[bossIndex];
    }

    public boolean isFightWaitInvalid() {
        return numPlayers == maxSetPlayers || started || (room.isContinuous() && continuousLevel > 0);
    }

    public synchronized void enterFireOval(User us) throws IOException {
        if (room.getType() == 6 && us.getClanId() == 0) {
            us.getUserService().sendServerMessage(GameString.notClan());
            return;
        }

        if (started || (room.isContinuous() && continuousLevel > 0)) {
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

        byte bestLocation = findEmptyUserSlot();
        if (bestLocation == -1) {
            return;
        }

        Message ms;
        DataOutputStream ds;
        if (numPlayers != 0) {
            ms = new Message(Cmd.SOMEONE_JOINBOARD);
            ds = ms.writer();
            ds.writeByte(bestLocation);
            ds.writeInt(us.getPlayerId());
            ds.writeShort(us.getClanId());
            ds.writeUTF(us.getUsername());
            ds.writeByte(us.getCurrentLevel());
            ds.writeByte(us.getActiveCharacterId());
            for (short id : us.getEquip()) {
                ds.writeShort(id);
            }
            ds.flush();
            sendToTeam(ms);
        } else {
            changeBoss(bestLocation);
        }

        us.setFightWait(this);
        us.setState(UserState.WAIT_FIGHT);

        users[bestLocation] = us;
        readies[bestLocation] = false;
        numPlayers++;

        ms = new Message(Cmd.JOIN_BOARD);
        ds = ms.writer();
        ds.writeInt(getRoomOwner().getPlayerId());
        ds.writeInt(money);
        ds.writeByte(mapId);
        ds.writeByte(0);//todo find value
        for (byte i = 0; i < users.length; i++) {
            User user = users[i];
            if (user != null) {
                ds.writeInt(user.getPlayerId());
                ds.writeShort(user.getClanId());
                ds.writeUTF(user.getUsername());
                ds.writeInt(0);
                ds.writeByte(user.getCurrentLevel());
                ds.writeByte(user.getActiveCharacterId());
                for (short id : user.getEquip()) {
                    ds.writeShort(id);
                }
                ds.writeBoolean(readies[i]);
            } else {
                ds.writeInt(-1);
            }
        }
        ds.flush();
        us.sendMessage(ms);

        ms = new Message(Cmd.MAP_SELECT);
        ds = ms.writer();
        ds.writeByte(mapId);
        ds.flush();
        us.sendMessage(ms);

        ms = new Message(Cmd.ITEM_SLOT);
        ds = ms.writer();
        for (byte i = 0; i < 4; i++) {
            ds.writeByte(us.getItemFightQuantity(12 + i));
        }
        ds.flush();
        us.sendMessage(ms);
    }

    private synchronized void changeBoss(byte index) {
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

    private byte findEmptyUserSlot() {
        for (byte i = 0; i < users.length; i++) {
            if (users[i] == null) {
                return i;
            }
        }
        return -1;
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

    public synchronized void setPassRoom(String password, int playerId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }

        isPassSet = true;
        this.password = password;
    }

    public synchronized void setMoney(int newMoney, int playerId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getPlayerId() != playerId) {
            return;
        }

        if (newMoney < room.getMinXu() || newMoney > room.getMaxXu()) {
            roomOwner.getUserService().sendServerMessage(GameString.datCuocError1(room.getMinXu(), room.getMaxXu()));
            return;
        }

        if (roomOwner.getXu() < newMoney) {
            roomOwner.getUserService().sendServerMessage(GameString.xuNotEnought());
            return;
        }

        resetReadies();
        money = newMoney;

        try {
            Message ms = new Message(Cmd.SET_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeShort(0);
            ds.writeInt(newMoney);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetReadies() {
        Arrays.fill(readies, false);
        numReady = 0;
        if (bossIndex >= 0 && bossIndex < readies.length) {
            readies[bossIndex] = true;
        }
    }

    public synchronized void setReady(boolean ready, int playerId) {
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

        if (readies[index] != ready) {
            readies[index] = ready;
            if (ready) {
                numReady++;
            } else {
                numReady--;
            }
        }

        try {
            Message ms = new Message(Cmd.READY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeBoolean(ready);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void kickPlayer(int playerId, int targetPlayerId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getPlayerId() != playerId) {
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
            roomOwner.getUserService().sendServerMessage(GameString.openingGift(users[index].getUsername()));
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

        leaveTeam(targetPlayerId);
    }

    public synchronized void leaveTeam(int targetPlayerId) {
        if (started) {
            fightManager.leave(targetPlayerId);
        }

        int index = getUserIndexByPlayerId(targetPlayerId);
        if (index == -1) {
            return;
        }

        users[index] = null;
        numPlayers--;

        if (numPlayers <= 0) {
            refreshFightWait();
        } else {
            if (bossIndex == index) {
                findNewBoss();
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
        byte maxPlayers = room.getMaxPlayerFight();

        money = room.getMinXu();
        name = "";
        password = "";
        isPassSet = false;
        started = false;
        bossIndex = -1;
        numPlayers = 0;
        numReady = 0;
        users = new User[maxPlayers];
        items = new byte[maxPlayers][8];
        readies = new boolean[maxPlayers];
    }

    public void chatMessage(int playerId, String message) {
        if (started) {
            fightManager.chatMessage(playerId, message);
            return;
        }

        int index = getUserIndexByPlayerId(playerId);
        if (index == -1) {
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

    public synchronized void startGame(int playerId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getPlayerId() != playerId) {
            return;
        }

        if (System.currentTimeMillis() - startTime < 5000) {
            roomOwner.getUserService().sendServerMessage(GameString.waitClick(startTime));
            return;
        }

        if (numReady == 0 && room.getType() != 5) {
            roomOwner.getUserService().sendServerMessage(GameString.startGameError1());
            return;
        }

        //Kiểm tra phe còn lại có cùng clan hay không
        if (room.getType() == 6) {
            for (byte i = 0; i < users.length; i++) {
                if (users[i] == null) {
                    continue;
                }
                for (byte j = 0; j < users.length; j++) {
                    if (j == i || users[j] == null || (j % 2 == 0 && i % 2 == 0) || (j % 2 != 0 && i % 2 != 0)) {
                        continue;
                    }
                    if (users[j].getClanId() == users[i].getClanId()) {
                        roomOwner.getUserService().sendServerMessage(GameString.startGameError());
                        return;
                    }
                }
            }
        }

        int numTeamRed = 0;
        int numTeamBlue = 0;

        for (byte i = 0; i < users.length; i++) {
            User user = users[i];
            if (user == null) {
                continue;
            }

            if (user.isOpeningGift()) {
                roomOwner.getUserService().sendServerMessage(GameString.openingGift(user.getUsername()));
                return;
            }

            if (bossIndex != i && !readies[i]) {
                roomOwner.getUserService().sendServerMessage(GameString.startGameError2(user.getUsername()));
                return;
            }

            if (user.getXu() < money) {
                roomOwner.getUserService().sendServerMessage(GameString.startGameError3(user.getUsername()));
                return;
            }

            byte[] userItems = items[i];
            byte[] itemUsageMap = new byte[FightItemData.FIGHT_ITEM_ENTRIES.size()];

            // Đếm số lượng item mà người dùng đang có
            for (byte itemIndex : userItems) {
                if (itemIndex < 0 || itemIndex >= itemUsageMap.length) {
                    continue;
                }
                itemUsageMap[itemIndex]++;
            }

            for (int j = 0; j < userItems.length; j++) {
                byte itemIndex = userItems[j];
                if (itemIndex < 0 || itemIndex >= itemUsageMap.length) {
                    continue;
                }

                // Kiểm tra điều kiện số lượng item
                if (itemUsageMap[itemIndex] > FightItemData.FIGHT_ITEM_ENTRIES.get(itemIndex).getCarriedItemCount() && // Số lượng vượt quá số lượng cho phép
                        itemUsageMap[itemIndex] > user.getItemFightQuantity(itemIndex) && // Số lượng vượt quá số lượng đang có
                        (j >= 4 && user.getItemFightQuantity(12 + j - 4) == 0) // Item chứa đã hết
                ) {
                    try {
                        Message ms = new Message(Cmd.SERVER_MESSAGE);
                        DataOutputStream ds = ms.writer();
                        ds.writeUTF(GameString.startGameError4(user.getUsername(), j));
                        ds.flush();
                        sendToTeam(ms);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            if (room.getType() == 5) {
                numTeamBlue++;
            } else {
                if (i % 2 == 0) {
                    numTeamBlue++;
                } else {
                    numTeamRed++;
                }
            }
        }

        if (room.getType() != 5 && numTeamBlue != numTeamRed) {
            roomOwner.getUserService().sendServerMessage(GameString.startGameError5());
        }

        started = true;
        try {
            fightManager.startGame(0, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setRoomName(int playerId, String name) {
        if (started) {
            return;
        }

        if (getRoomOwner().getPlayerId() != playerId) {
            return;
        }

        this.name = name;
    }

    public synchronized void setMaxPlayers(int playerId, byte maxPlayers) {
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

    public synchronized void setMap(int playerId, byte mapIdSet) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getPlayerId() != playerId) {
            return;
        }

        if (room.isContinuous()) {
            roomOwner.getUserService().sendServerMessage(GameString.selectMapError1_3());
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

        if (room.getMapCanSelected() != null) {
            boolean mapIdFound = false;
            for (byte id : room.getMapCanSelected()) {
                if (id == mapIdSet) {
                    mapIdFound = true;
                    break;
                }
            }

            if (!mapIdFound) {
                roomOwner.getUserService().sendServerMessage(GameString.selectMapError1_1(MapData.getMapNames(room.getMapCanSelected())));
                return;
            }
        } else {
            byte minMap = room.getMinMap();
            byte maxMap = room.getMaxMap();
            if (mapIdSet < minMap || mapIdSet > maxMap) {
                String msg;
                if (minMap == maxMap) {
                    msg = GameString.selectMapError1_1(MapData.getMapNames(minMap));
                } else if (minMap == maxMap - 1) {
                    msg = GameString.selectMapError1_1(MapData.getMapNames(minMap, maxMap));
                } else {
                    msg = GameString.selectMapError1_3();
                }
                roomOwner.getUserService().sendServerMessage(msg);
                return;
            }
        }

        mapId = mapIdSet;
        if (mapId == 27) {
            byte mapRandom = MapData.randomMap(27);
            try {
                Message ms = new Message(Cmd.TRAINING_MAP);
                DataOutputStream ds = ms.writer();
                ds.writeByte(mapRandom);
                ds.flush();
                sendToTeam(ms);
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        User roomOwner = getRoomOwner();
        if (roomOwner.getPlayerId() != playerId) {
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
                ds.writeByte(u.getActiveCharacterId());
                ds.writeInt(u.getXu());
                ds.writeByte(u.getCurrentLevel());
                ds.writeByte(u.getCurrentLevelPercent());
                for (short id : u.getEquip()) {
                    ds.writeShort(id);
                }
            }
            ds.flush();
            roomOwner.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void inviteToRoom(int playerId) {
        User user = ServerManager.getInstance().getUserByPlayerId(playerId);

        User roomOwner = getRoomOwner();
        if (user == null) {
            roomOwner.getUserService().sendServerMessage(GameString.inviteError1());
            return;
        }

        if (user.isNotWaiting()) {
            roomOwner.getUserService().sendServerMessage(GameString.inviteError2());
            return;
        }

        try {
            Message ms = new Message(Cmd.FIND_PLAYER);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(false);
            ds.writeUTF(GameString.inviteMessage(user.getUsername()));
            ds.writeByte(room.getIndex());
            ds.writeByte(id);
            ds.writeUTF(password);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void changeTeam(User user) {
        if (started) {
            return;
        }

        int index = getUserIndexByPlayerId(user.getPlayerId());
        if (index == -1) {
            return;
        }

        byte newIndex = (byte) ((index % 2 == 0) ? 1 : 0);
        boolean teamChanged = false;

        for (; newIndex < users.length; newIndex += 2) {
            if (users[newIndex] == null) {
                users[newIndex] = user;
                users[index] = null;
                if (bossIndex == index) {
                    bossIndex = newIndex;
                }
                teamChanged = true;
                break;
            }
        }

        if (!teamChanged) {
            return;
        }

        try {
            Message ms = new Message(Cmd.CHANGE_TEAM);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
            ds.writeByte(newIndex);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setItems(int playerId, byte[] newItems) {
        if (started) {
            return;
        }

        int index = getUserIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }

        items[index] = newItems;
    }

    public void fightComplete() throws IOException {
        // Chien xong, refresh fight wait
        Message ms;
        DataOutputStream ds;
        for (byte i = 0; i < this.users.length; i++) {
            this.readies[i] = false;
            User us = this.users[i];
            if (us == null) {
                continue;
            }
            ms = new Message(112);
            ds = ms.writer();
            for (byte j = 0; j < 4; j++) {
                ds.writeByte(us.getItemFightQuantity(12 + j));
            }
            ds.flush();
            us.sendMessage(ms);
            us.setFightWait(this);
        }
        this.numReady = 0;
        if (this.bossIndex != -1) {
            changeBoss(this.bossIndex);
        }
        // Send map
        ms = new Message(75);
        ds = ms.writer();
        ds.writeByte(this.mapId);
        ds.flush();
        this.sendToTeam(ms);

        startTime = System.currentTimeMillis();
    }
}