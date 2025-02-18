package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.ICountdownTimer;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.IFightWait;
import com.teamobi.mobiarmy2.model.Room;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.MapManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IClanService;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author tuyen
 */
public class FightWait implements IFightWait {
    public static final byte MAX_ITEMS_SLOT = 8;
    public static final int KICK_BOSS_TIME = 90;
    public static final byte[] CONTINUOUS_MAPS = {30, 31, 32, 33, 34, 35, 36, 37, 38, 39};

    private final IFightManager fightManager;
    private final Room room;
    private final byte id;
    private final ICountdownTimer countdownTimer;
    private User[] users;
    private boolean[] readies;
    private byte[][] items;
    private boolean started;
    private int numReady;
    private int maxSetPlayers;
    private byte numPlayers;
    private boolean isPassSet;
    private String password;
    private int money;
    private String name;
    private byte mapId;
    private int bossIndex;
    private byte continuousLevel;
    private long endTime;
    private long lastPlayerJoinTime;

    public FightWait(Room room, byte id) {
        this.room = room;
        this.id = id;

        byte maxPlayers = room.getMaxPlayerFight();

        this.fightManager = new FightManager(this, ApplicationContext.getInstance().getBean(IClanService.class));
        this.users = new User[maxPlayers];
        this.items = new byte[maxPlayers][MAX_ITEMS_SLOT];
        this.readies = new boolean[maxPlayers];

        this.name = "";
        this.password = "";
        this.isPassSet = false;
        this.started = false;
        this.numReady = 0;
        this.bossIndex = -1;
        this.numPlayers = 0;
        this.endTime = 0L;
        this.continuousLevel = 0;

        this.mapId = room.getMapId();
        this.money = room.getMinXu();

        this.maxSetPlayers = room.getNumPlayerInitRoom();
        this.countdownTimer = new CountdownTimer(KICK_BOSS_TIME, this::onTimeUp);
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
        items = new byte[maxPlayers][MAX_ITEMS_SLOT];
        readies = new boolean[maxPlayers];
        if (isContinuous()) {
            continuousLevel = 0;
            mapId = CONTINUOUS_MAPS[continuousLevel];
        }
        countdownTimer.stop();
    }

    private User getRoomOwner() {
        return users[bossIndex];
    }

    private byte findEmptyUserSlot() {
        for (byte i = 0; i < users.length; i++) {
            if (users[i] == null) {
                return i;
            }
        }
        return -1;
    }

    private int getUserIndexByUserId(int userId) {
        for (byte i = 0; i < users.length; i++) {
            User user = this.users[i];
            if (user == null) {
                continue;
            }
            if (user.getUserId() == userId) {
                return i;
            }
        }
        return -1;
    }

    private void onTimeUp() {
        if (bossIndex < 0 || bossIndex >= users.length) {
            return;
        }

        User user = getRoomOwner();
        sendMessageKick(bossIndex, "Không start ván");
        handleUserRemoval(bossIndex);
        if (numPlayers <= 0) {
            refreshFightWait();
        } else {
            findNewBoss();
            notifyPlayerLeave(user.getUserId());
        }
    }

    private void changeBoss(int index) {
        bossIndex = index;
        countdownTimer.reset();
    }

    private void sendUpdateItemSlot(User us) {
        try {
            IMessage ms = new Message(Cmd.ITEM_SLOT);
            DataOutputStream ds = ms.writer();
            for (byte i = 0; i < 4; i++) {
                ds.writeByte(us.getItemFightQuantity(12 + i));
            }
            ds.flush();
            us.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUpdateMap(User us) {
        try {
            IMessage ms = new Message(Cmd.MAP_SELECT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(mapId);
            ds.flush();
            us.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageKick(int index, String s) {
        try {
            User user = users[index];
            IMessage ms = new Message(Cmd.KICK);
            DataOutputStream ds = ms.writer();
            ds.writeShort(index);
            ds.writeInt(user.getUserId());
            ds.writeUTF(s);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetReadies() {
        readies = new boolean[room.getMaxPlayerFight()];
        numReady = 0;
    }

    private void findNewBoss() {
        for (byte i = 0; i < users.length; i++) {
            if (users[i] != null) {
                changeBoss(i);
                break;
            }
        }
    }

    private void handleUserRemoval(int index) {
        //Xóa người chơi và cập nhật trạng thái người chơi
        users[index].setState(UserState.WAITING);
        users[index].setFightWait(null);
        users[index] = null;
        numPlayers--;

        //Xóa trạng thái sẵn sàng
        if (readies[index]) {
            readies[index] = false;
            numReady--;
        }
    }

    private void notifyPlayerLeave(int userId) {
        try {
            IMessage ms = new Message(Cmd.SOMEONE_LEAVEBOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeInt(getRoomOwner().getUserId());
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getMaxSetPlayers() {
        return maxSetPlayers;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isContinuous() {
        return room.isContinuous();
    }

    @Override
    public boolean isPassSet() {
        return isPassSet;
    }

    @Override
    public boolean isFightWaitInvalid() {
        return numPlayers == maxSetPlayers || started || (isContinuous() && continuousLevel > 0);
    }

    @Override
    public byte getNumPlayers() {
        return numPlayers;
    }

    @Override
    public byte getId() {
        return id;
    }

    @Override
    public byte getMapId() {
        return mapId;
    }

    @Override
    public byte getRoomType() {
        return room.getType();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public int getMoney() {
        return money;
    }

    @Override
    public Room getRoom() {
        return room;
    }

    @Override
    public User getUserByUserId(int userId) {
        int index = getUserIndexByUserId(userId);
        if (index == -1) {
            return null;
        }
        return users[index];
    }

    @Override
    public byte[] getItems(byte i) {
        return items[i];
    }

    @Override
    public User[] getUsers() {
        return users;
    }

    @Override
    public IFightManager getFightManager() {
        return fightManager;
    }

    @Override
    public void fightComplete() {
        resetReadies();
        countdownTimer.reset();

        for (User user : users) {
            if (user == null) {
                continue;
            }
            user.setState(UserState.WAIT_FIGHT);

            sendUpdateItemSlot(user);
            sendUpdateMap(user);
        }

        endTime = System.currentTimeMillis();
        started = false;
    }

    @Override
    public synchronized void startGame(int userId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        //Kiểm tra thời gian kết thúc ván gần nhất
        long remainingTime = 5000 - (System.currentTimeMillis() - endTime);
        if (remainingTime > 0) {
            roomOwner.getUserService().sendServerMessage(GameString.createWaitClickMessage(remainingTime / 1000 + 1));
            return;
        }

        //Kiểm tra thời gian người chơi vào phòng gần nhất
        remainingTime = 5000 - (System.currentTimeMillis() - lastPlayerJoinTime);
        if (remainingTime > 0) {
            roomOwner.getUserService().sendMoneyErrorMessage(GameString.createWaitClickMessage(remainingTime / 1000 + 1));
            return;
        }

        if (numReady == 0 && room.getType() != 5) {
            roomOwner.getUserService().sendServerMessage(GameString.TEAM_NOT_READY);
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
                    if (Objects.equals(users[j].getClanId(), users[i].getClanId())) {
                        roomOwner.getUserService().sendServerMessage(GameString.TEAM_MUST_BE_SAME_FACTION);
                        return;
                    }
                }
            }
        }

        byte numTeamRed = 0;
        byte numTeamBlue = 0;
        short teamPointsBlue = 0;
        short teamPointsRed = 0;

        for (byte i = 0; i < users.length; i++) {
            User user = users[i];
            if (user == null) {
                continue;
            }

            if (user.isOpeningGift()) {
                roomOwner.getUserService().sendServerMessage(GameString.createOpeningGiftMessage(user.getUsername()));
                return;
            }

            if (bossIndex != i && !readies[i]) {
                roomOwner.getUserService().sendServerMessage(GameString.createGameStartErrorMessageUserNotReady(user.getUsername()));
                return;
            }

            if (user.getXu() < money) {
                roomOwner.getUserService().sendServerMessage(GameString.createGameStartErrorMessageInsufficientFunds(user.getUsername()));
                return;
            }

            byte[] userItems = items[i];
            byte[] itemUsageMap = new byte[FightItemManager.FIGHT_ITEMS.size()];

            //Đếm số lượng item mà người dùng đang có
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

                //Kiểm tra điều kiện số lượng item
                if (itemUsageMap[itemIndex] > FightItemManager.FIGHT_ITEMS.get(itemIndex).getCarriedItemCount() || //Số lượng vượt quá số lượng cho phép
                        itemUsageMap[itemIndex] > user.getItemFightQuantity(itemIndex) || //Số lượng vượt quá số lượng đang có
                        (j >= 4 && user.getItemFightQuantity(12 + j - 4) == 0) //Item chứa đã hết
                ) {
                    try {
                        IMessage ms = new Message(Cmd.SERVER_MESSAGE);
                        DataOutputStream ds = ms.writer();
                        ds.writeUTF(GameString.createGameStartErrorMessageInvalidSlot(user.getUsername(), j));
                        ds.flush();
                        sendToTeam(ms);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
            }

            //Lấy ra bonus item clan
            byte bonusPercent = 0;

            //Đếm số người chơi và tính điểm đồng đội (chỉ áp dụng điểm đồng đội nếu có người chơi cùng)
            if (room.getType() == 5) {
                numTeamBlue++;
                if (numPlayers > 1) {
                    teamPointsBlue += user.calculateTeamPoints(bonusPercent);
                }
            } else {
                if (i % 2 == 0) {
                    numTeamBlue++;
                    if (numPlayers > 3) {
                        teamPointsBlue += user.calculateTeamPoints(bonusPercent);
                    }
                } else {
                    numTeamRed++;
                    if (numPlayers > 3) {
                        teamPointsRed += user.calculateTeamPoints(bonusPercent);
                    }
                }
            }
        }

        if (room.getType() != 5 && numTeamBlue != numTeamRed) {
            roomOwner.getUserService().sendServerMessage(GameString.TEAM_SIZE_MISMATCH);
        }

        //Cập nhật lại điểm đồng đội
        teamPointsBlue = (short) (teamPointsBlue / 20);
        teamPointsRed = (short) (teamPointsRed / 20);

        started = true;
        fightManager.startGame(teamPointsBlue, teamPointsRed);

        resetReadies();
        countdownTimer.stop();
    }

    @Override
    public void sendToTeam(IMessage ms) {
        for (User user : users) {
            if (user != null) {
                user.sendMessage(ms);
            }
        }
    }

    @Override
    public synchronized void leaveTeam(int userId) {
        if (started) {
            fightManager.leave(userId);
        }

        int index = getUserIndexByUserId(userId);
        if (index == -1) {
            return;
        }

        handleUserRemoval(index);

        if (numPlayers <= 0) {
            refreshFightWait();
        } else {
            if (bossIndex == index) {
                findNewBoss();
            }
            notifyPlayerLeave(userId);
        }
    }

    @Override
    public void chatMessage(int userId, String message) {
        int index = getUserIndexByUserId(userId);
        if (index == -1) {
            return;
        }

        try {
            IMessage ms = new Message(Cmd.CHAT_TO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeUTF(message);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void kickPlayer(int userId, int targetUserId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        int index = getUserIndexByUserId(targetUserId);
        if (index == -1) {
            return;
        }

        if (readies[index]) {
            return;
        }

        User user = users[index];
        if (user.isOpeningGift()) {
            roomOwner.getUserService().sendServerMessage(GameString.createOpeningGiftMessage(users[index].getUsername()));
            return;
        }

        sendMessageKick(index, GameString.KICKED_BY_HOST);
        handleUserRemoval(index);
        notifyPlayerLeave(targetUserId);
    }

    @Override
    public void handleKickPlayer(int targetUserId, int index, String message) {
        sendMessageKick(index, message);
        handleUserRemoval(index);
        if (numPlayers <= 0) {
            refreshFightWait();
        } else {
            if (bossIndex == index) {
                findNewBoss();
            }
            notifyPlayerLeave(targetUserId);
        }
    }

    @Override
    public void decreaseContinuousLevel() {
        continuousLevel = (byte) ((continuousLevel + 1) % CONTINUOUS_MAPS.length);
        mapId = CONTINUOUS_MAPS[continuousLevel];
    }

    @Override
    public synchronized void setReady(boolean ready, int userId) {
        if (started) {
            return;
        }

        if (getRoomOwner().getUserId() == userId) {
            return;
        }

        int index = getUserIndexByUserId(userId);
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
            IMessage ms = new Message(Cmd.READY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeBoolean(ready);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void setPassRoom(String password, int userId) {
        if (started) {
            return;
        }
        if (getRoomOwner().getUserId() != userId) {
            return;
        }

        isPassSet = true;
        this.password = password;
    }

    @Override
    public synchronized void setMoney(int newMoney, int userId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        if (newMoney < room.getMinXu() || newMoney > room.getMaxXu()) {
            roomOwner.getUserService().sendServerMessage(GameString.createBettingRangeErrorMessage(room.getMinXu(), room.getMaxXu()));
            return;
        }

        if (roomOwner.getXu() < newMoney) {
            roomOwner.getUserService().sendServerMessage(GameString.INSUFFICIENT_FUNDS);
            return;
        }

        //Đặt lại bộ đếm thời gian kick
        countdownTimer.reset();

        resetReadies();
        money = newMoney;

        try {
            IMessage ms = new Message(Cmd.SET_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeShort(0);
            ds.writeInt(newMoney);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void setRoomName(int userId, String name) {
        if (started) {
            return;
        }

        if (getRoomOwner().getUserId() != userId) {
            return;
        }

        this.name = name;
    }

    @Override
    public synchronized void setMaxPlayers(int userId, byte maxPlayers) {
        if (started) {
            return;
        }

        if (getRoomOwner().getUserId() != userId) {
            return;
        }

        if (maxPlayers > 0 && maxPlayers < 9 && maxPlayers % 2 == 0 && numPlayers < maxPlayers) {
            maxSetPlayers = maxPlayers;
        }
    }

    @Override
    public synchronized void setItems(int userId, byte[] newItems) {
        if (started) {
            return;
        }

        int index = getUserIndexByUserId(userId);
        if (index == -1) {
            return;
        }

        items[index] = newItems;
    }

    @Override
    public synchronized void changeTeam(User user) {
        if (started) {
            return;
        }

        int index = getUserIndexByUserId(user.getUserId());
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
            IMessage ms = new Message(Cmd.CHANGE_TEAM);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getUserId());
            ds.writeByte(newIndex);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void setMap(int userId, byte mapIdSet) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        if (isContinuous()) {
            roomOwner.getUserService().sendServerMessage(GameString.MAP_SELECTION_ERROR);
            return;
        }

        for (User user : users) {
            if (user == null) {
                continue;
            }
            if (user.isOpeningGift()) {
                user.getUserService().sendServerMessage(GameString.createOpeningGiftMessage(user.getUsername()));
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
                roomOwner.getUserService().sendServerMessage(GameString.createMapSelectionErrorMessage(MapManager.getMapNames(room.getMapCanSelected())));
                return;
            }
        } else {
            byte minMap = room.getMinMap();
            byte maxMap = room.getMaxMap();
            if (mapIdSet < minMap || mapIdSet > maxMap) {
                String msg;
                if (minMap == maxMap) {
                    msg = GameString.createMapSelectionErrorMessage(MapManager.getMapNames(minMap));
                } else if (minMap == maxMap - 1) {
                    msg = GameString.createMapSelectionErrorMessage(MapManager.getMapNames(minMap, maxMap));
                } else {
                    msg = GameString.MAP_SELECTION_ERROR;
                }
                roomOwner.getUserService().sendServerMessage(msg);
                return;
            }
        }

        mapId = mapIdSet;
        if (mapId == 27) {
            byte mapRandom = MapManager.randomMap(Set.of(
                    (byte) 27, (byte) 30, (byte) 31, (byte) 32, (byte) 33,
                    (byte) 34, (byte) 35, (byte) 36, (byte) 37, (byte) 38, (byte) 39
            ));
            try {
                IMessage ms = new Message(Cmd.TRAINING_MAP);
                DataOutputStream ds = ms.writer();
                ds.writeByte(mapRandom);
                ds.flush();
                sendToTeam(ms);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        resetReadies();

        //Đặt lại bộ đếm thời gian kick
        countdownTimer.reset();

        try {
            IMessage ms = new Message(Cmd.MAP_SELECT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(mapId);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void findPlayer(int userId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        List<User> userList = ServerManager.getInstance().findWaitPlayers(userId);

        try {
            IMessage ms = new Message(Cmd.FIND_PLAYER);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(true);
            ds.writeByte(userList.size());
            for (User u : userList) {
                ds.writeUTF(u.getUsername());
                ds.writeInt(u.getUserId());
                ds.writeByte(u.getActiveCharacterId());
                ds.writeInt(u.getXu());
                ds.writeByte(u.getCurrentLevel());
                ds.writeByte(u.getCurrentLevelPercent());
                short[] equips = u.getEquips();
                for (short id : equips) {
                    ds.writeShort(id);
                }
            }
            ds.flush();
            roomOwner.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void inviteToRoom(int userId) {
        User roomOwner = getRoomOwner();

        User user = ServerManager.getInstance().getUserByUserId(userId);
        if (user == null) {
            roomOwner.getUserService().sendServerMessage(GameString.INVITE_OFFLINE);
            return;
        }

        if (user.isNotWaiting()) {
            roomOwner.getUserService().sendServerMessage(GameString.INVITE_ALREADY_IN_GAME);
            return;
        }

        if (user.isInvitationLocked()) {
            roomOwner.getUserService().sendServerMessage(GameString.INVITE_DISABLED);
            return;
        }

        try {
            IMessage ms = new Message(Cmd.FIND_PLAYER);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(false);
            ds.writeUTF(GameString.createInviteMessage(roomOwner.getUsername()));
            ds.writeByte(room.getIndex());
            ds.writeByte(id);
            ds.writeUTF(password);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendInfo(User user) {
        try {
            Message ms = new Message(Cmd.AUTO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeByte(room.getIndex());
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.writeByte(room.getType());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void addUser(User us) throws IOException {
        if (room.getType() == 6 && us.getClanId() == null) {
            us.getUserService().sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
            return;
        }

        if (started || (isContinuous() && continuousLevel > 0)) {
            us.getUserService().sendServerMessage(GameString.AREA_JOIN_IN_PROGRESS);
            return;
        }

        if (money > us.getXu()) {
            us.getUserService().sendServerMessage(GameString.AREA_INSUFFICIENT_FUNDS);
            return;
        }

        if (numPlayers >= maxSetPlayers) {
            us.getUserService().sendServerMessage(GameString.AREA_FULL);
            return;
        }

        byte bestLocation = findEmptyUserSlot();
        if (bestLocation == -1) {
            return;
        }

        IMessage ms;
        DataOutputStream ds;
        if (numPlayers != 0) {
            ms = new Message(Cmd.SOMEONE_JOINBOARD);
            ds = ms.writer();
            ds.writeByte(bestLocation);
            ds.writeInt(us.getUserId());
            ds.writeShort(us.getClanId() != null ? us.getClanId() : 0);
            ds.writeUTF(us.getUsername());
            ds.writeByte(us.getCurrentLevel());
            ds.writeByte(us.getActiveCharacterId());
            short[] equips = us.getEquips();
            for (short id : equips) {
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

        //Lưu thời gian gần nhất vào phòng của người chơi
        lastPlayerJoinTime = System.currentTimeMillis();

        ms = new Message(Cmd.JOIN_BOARD);
        ds = ms.writer();
        ds.writeInt(getRoomOwner().getUserId());
        ds.writeInt(money);
        ds.writeByte(mapId);
        ds.writeByte(0);//GameMode
        for (byte i = 0; i < users.length; i++) {
            User user = users[i];
            if (user != null) {
                ds.writeInt(user.getUserId());
                ds.writeShort(user.getClanId() != null ? user.getClanId() : 0);
                ds.writeUTF(user.getUsername());
                ds.writeInt(user.getXu());
                ds.writeByte(user.getCurrentLevel());
                ds.writeByte(user.getActiveCharacterId());
                short[] equips = user.getEquips();
                for (short id : equips) {
                    ds.writeShort(id);
                }
                ds.writeBoolean(readies[i]);
            } else {
                ds.writeInt(-1);
            }
        }
        ds.flush();
        us.sendMessage(ms);

        sendUpdateMap(us);
        sendUpdateItemSlot(us);
    }
}
