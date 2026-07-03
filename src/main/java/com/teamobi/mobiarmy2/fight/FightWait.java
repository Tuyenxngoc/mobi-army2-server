package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.Room;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.MapManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.ClanService;
import com.teamobi.mobiarmy2.service.GiftBoxService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class FightWait {
    public static final byte MAX_ITEMS_SLOT = 8;
    public static final int KICK_BOSS_TIME = 90;
    public static final byte[] CONTINUOUS_MAPS = {30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
    public static final int GAME_END_WAIT_TIME_MS = 3000;
    public static final int PLAYER_JOIN_WAIT_TIME_MS = 3000;

    @Getter
    private final IFightManager fightManager;
    @Getter
    private final Room room;
    @Getter
    private final byte id;
    private final CountdownTimer countdownTimer;
    @Getter
    private User[] users;//Chẵn - đội xanh, lẻ - đội đỏ
    private boolean[] readies;
    private byte[][] items;
    @Getter
    private boolean started;
    private int numReady;
    @Getter
    private int maxSetPlayers;
    @Getter
    private byte numPlayers;
    @Getter
    private boolean isPassSet;
    @Getter
    private String password;
    @Getter
    private int money;
    @Getter
    private String name;
    @Getter
    private byte mapId;
    private int bossIndex;
    private byte continuousLevel;
    private long endTime;
    private long lastPlayerJoinTime;

    private final GiftBoxService giftBoxService;
    private final MessageSender messageSender;

    public FightWait(Room room, byte id) {
        this.room = room;
        this.id = id;

        byte maxPlayers = room.getMaxPlayerFight();

        this.messageSender = ApplicationContext.getInstance().getBean(MessageSender.class);
        this.fightManager = new FightManager(this, ApplicationContext.getInstance().getBean(ClanService.class), messageSender);
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

        this.giftBoxService = new GiftBoxService();
    }

    private synchronized void refreshFightWait() {
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

    private synchronized void onTimeUp() {
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

    private synchronized void changeBoss(int index) {
        bossIndex = index;
        countdownTimer.reset();
    }

    private void sendUpdateItemSlot(User us) {
        try {
            Message ms = new Message(Cmd.ITEM_SLOT);
            DataOutputStream ds = ms.writer();
            for (byte i = 0; i < 4; i++) {
                ds.writeByte(us.getItemFightQuantity(12 + i));
            }
            ds.flush();
            messageSender.sendTo(us, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUpdateMap(User us) {
        try {
            Message ms = new Message(Cmd.MAP_SELECT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(mapId);
            ds.flush();
            messageSender.sendTo(us, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageKick(int index, String s) {
        try {
            User user = users[index];
            if (user == null) {
                return;
            }
            Message ms = new Message(Cmd.KICK);
            DataOutputStream ds = ms.writer();
            ds.writeShort(index);
            ds.writeInt(user.getUserId());
            ds.writeUTF(s);
            ds.flush();
            messageSender.sendTo(user, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resetReadies() {
        readies = new boolean[room.getMaxPlayerFight()];
        numReady = 0;
    }

    private synchronized void findNewBoss() {
        for (byte i = 0; i < users.length; i++) {
            if (users[i] != null) {
                changeBoss(i);
                break;
            }
        }
    }

    private synchronized void handleUserRemoval(int index) {
        //Xóa người chơi và cập nhật trạng thái người chơi
        users[index].setState(UserState.WAITING);
        users[index].setFightWait(null);
        users[index] = null;
        numPlayers--;

        //Xóa trang bị và trạng thái sẵn sàng
        items[index] = new byte[MAX_ITEMS_SLOT];
        if (readies[index]) {
            readies[index] = false;
            numReady--;
        }
    }

    private void notifyPlayerLeave(int userId) {
        try {
            User owner = getRoomOwner();
            Message ms = new Message(Cmd.SOMEONE_LEAVEBOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeInt(owner.getUserId());
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isContinuous() {
        return room.isContinuous();
    }

    public boolean isFightWaitInvalid() {
        return numPlayers == maxSetPlayers || started || (isContinuous() && continuousLevel > 0);
    }

    public byte getRoomType() {
        return room.getType();
    }

    public User getUserByUserId(int userId) {
        int index = getUserIndexByUserId(userId);
        if (index == -1) {
            return null;
        }
        return users[index];
    }

    public byte[] getItems(byte i) {
        return items[i];
    }

    public synchronized void fightComplete() {
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

    public synchronized void startGame(int userId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        //Kiểm tra thời gian kết thúc ván gần nhất
        long remainingTime = GAME_END_WAIT_TIME_MS - (System.currentTimeMillis() - endTime);
        if (remainingTime > 0) {
            roomOwner.sendServerMessage(GameString.createWaitClickMessage(remainingTime / 1000 + 1));
            return;
        }

        //Kiểm tra thời gian người chơi vào phòng gần nhất
        remainingTime = PLAYER_JOIN_WAIT_TIME_MS - (System.currentTimeMillis() - lastPlayerJoinTime);
        if (remainingTime > 0) {
            roomOwner.sendMoneyErrorMessage(GameString.createWaitClickMessage(remainingTime / 1000 + 1));
            return;
        }

        if (numReady == 0 && room.getType() != 5) {
            roomOwner.sendServerMessage(GameString.TEAM_NOT_READY);
            return;
        }

        // Kiểm tra điều kiện trong chế độ Đấu đội
        if (room.getType() == 6) {
            for (byte i = 0; i < users.length; i++) {
                if (users[i] == null) {
                    continue;
                }
                for (byte j = (byte) (i + 1); j < users.length; j++) {
                    if (users[j] == null) {
                        continue;
                    }
                    // Nếu i và j cùng phe (cùng chẵn hoặc cùng lẻ)
                    if (i % 2 == j % 2) {
                        // Các thành viên cùng phe phải thuộc cùng một clan
                        if (users[i].getClanId() != users[j].getClanId()) {
                            roomOwner.sendServerMessage(GameString.CLAN_MUST_BE_SAME);
                            return;
                        }
                    } else {
                        // Nếu i và j khác phe, hai phe phải thuộc hai clan khác nhau
                        if (users[i].getClanId() == users[j].getClanId()) {
                            roomOwner.sendServerMessage(GameString.CLAN_MUST_BE_DIFFERENT);
                            return;
                        }
                    }
                }
            }
        }

        // Khởi tạo biến đếm số lượng người chơi và tổng điểm đồng đội mỗi phe
        byte numTeamRed = 0;
        byte numTeamBlue = 0;

        // Vòng lặp kiểm tra từng người chơi trong phòng
        for (byte i = 0; i < users.length; i++) {
            User user = users[i];
            if (user == null) {
                continue;
            }

            // Kiểm tra nếu người chơi đang trong quá trình mở hộp quà
            if (giftBoxService.isOpeningGift(user.getUserId())) {
                roomOwner.sendServerMessage(GameString.createOpeningGiftMessage(user.getUsername()));
                return;
            }

            // Kiểm tra trạng thái sẵn sàng (Chủ phòng không cần check ready)
            if (bossIndex != i && !readies[i]) {
                roomOwner.sendServerMessage(GameString.createGameStartErrorMessageUserNotReady(user.getUsername()));
                return;
            }

            // Kiểm tra số dư tài khoản có đủ để tham gia ván cược không
            if (user.getXu() < money) {
                roomOwner.sendServerMessage(GameString.createGameStartErrorMessageInsufficientFunds(user.getUsername()));
                return;
            }

            byte[] userItems = items[i];
            byte[] itemUsageMap = new byte[FightItemManager.FIGHT_ITEMS.size()];

            // Kiểm tra hợp lệ cho các Item mang theo
            // Bước A: Thống kê số lượng từng loại item người chơi đã chọn vào các slot
            for (byte itemIndex : userItems) {
                if (itemIndex < 0 || itemIndex >= itemUsageMap.length) {
                    continue;
                }
                itemUsageMap[itemIndex]++;
            }

            // Bước B: Đối chiếu số lượng đã chọn với giới hạn cho phép và số lượng thực tế trong kho
            for (int j = 0; j < userItems.length; j++) {
                byte itemIndex = userItems[j];
                if (itemIndex < 0 || itemIndex >= itemUsageMap.length) {
                    continue;
                }

                if (itemUsageMap[itemIndex] > FightItemManager.FIGHT_ITEMS.get(itemIndex).getCarriedItemCount() || // Vượt quá giới hạn mang theo của item
                        itemUsageMap[itemIndex] > user.getItemFightQuantity(itemIndex) || // Vượt quá số lượng item đang sở hữu
                        (j >= 4 && user.getItemFightQuantity(12 + j - 4) == 0) // Các slot từ 4-7 yêu cầu phải có Item túi đựng (từ id 12-15) còn số lượng
                ) {
                    try {
                        Message ms = new Message(Cmd.SERVER_MESSAGE);
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

            // Phân loại phe để đếm số lượng người chơi hỗ trợ kiểm tra cân bằng đội hình
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

        // Kiểm tra cân bằng số lượng người chơi giữa 2 phe (trừ chế độ Đấu Trùm)
        if (room.getType() != 5 && numTeamBlue != numTeamRed) {
            roomOwner.sendServerMessage(GameString.TEAM_SIZE_MISMATCH);
            return;
        }

        started = true;
        fightManager.startGame();

        resetReadies();
        countdownTimer.stop();
    }

    public void sendToTeam(Message ms) {
        for (User user : users) {
            if (user != null) {
                messageSender.sendTo(user, ms);
            }
        }
    }

    public synchronized void leaveTeam(int userId) {
        if (started) {
            try {
                fightManager.leaveGame(userId).get();
            } catch (Exception e) {
                log.error("Leave game failed", e);
            }
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

    public void chatMessage(int userId, String message) {
        int index = getUserIndexByUserId(userId);
        if (index == -1) {
            return;
        }

        try {
            Message ms = new Message(Cmd.CHAT_TO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeUTF(message);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        if (giftBoxService.isOpeningGift(user.getUserId())) {
            roomOwner.sendServerMessage(GameString.createOpeningGiftMessage(user.getUsername()));
            return;
        }

        sendMessageKick(index, GameString.KICKED_BY_HOST);
        handleUserRemoval(index);
        notifyPlayerLeave(targetUserId);
    }

    public synchronized void handleKickPlayer(int targetUserId, int index, String message) {
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

    public synchronized void nextContinuousLevel() {
        continuousLevel = (byte) ((continuousLevel + 1) % CONTINUOUS_MAPS.length);
        mapId = CONTINUOUS_MAPS[continuousLevel];
    }

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
            Message ms = new Message(Cmd.READY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(userId);
            ds.writeBoolean(ready);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public synchronized void setMoney(int newMoney, int userId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        if (newMoney < room.getMinXu() || newMoney > room.getMaxXu()) {
            roomOwner.sendServerMessage(GameString.createBettingRangeErrorMessage(room.getMinXu(), room.getMaxXu()));
            return;
        }

        if (roomOwner.getXu() < newMoney) {
            roomOwner.sendServerMessage(GameString.INSUFFICIENT_FUNDS);
            return;
        }

        //Đặt lại bộ đếm thời gian kick
        countdownTimer.reset();

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

    public synchronized void setRoomName(int userId, String name) {
        if (started) {
            return;
        }

        if (getRoomOwner().getUserId() != userId) {
            return;
        }

        this.name = name;
    }

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

    public synchronized void changeTeam(User user) {
        if (started) {
            return;
        }

        int index = getUserIndexByUserId(user.getUserId());
        if (index == -1) {
            return;
        }

        // Ngoài chủ phòng ra, người chơi đang sẵn sàng không được đổi phe
        if (bossIndex != index && readies[index]) {
            return;
        }

        byte newIndex = (byte) ((index % 2 == 0) ? 1 : 0);
        boolean teamChanged = false;

        for (; newIndex < users.length; newIndex += 2) {
            if (users[newIndex] == null) {
                // Cập nhật các giá trị của người chơi ở vị trí mới
                users[newIndex] = user;
                items[newIndex] = items[index];
                readies[newIndex] = false;

                // Xóa thông tin người chơi ở vị trí cũ
                users[index] = null;
                items[index] = new byte[MAX_ITEMS_SLOT];
                readies[index] = false;

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
            ds.writeInt(user.getUserId());
            ds.writeByte(newIndex);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void setMap(int userId, byte mapIdSet) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        if (isContinuous()) {
            roomOwner.sendServerMessage(GameString.MAP_SELECTION_ERROR);
            return;
        }

        for (User user : users) {
            if (user == null) {
                continue;
            }
            if (giftBoxService.isOpeningGift(user.getUserId())) {
                user.sendServerMessage(GameString.createOpeningGiftMessage(user.getUsername()));
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
                roomOwner.sendServerMessage(GameString.createMapSelectionErrorMessage(MapManager.getMapNames(room.getMapCanSelected())));
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
                roomOwner.sendServerMessage(msg);
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

        //Đặt lại bộ đếm thời gian kick
        countdownTimer.reset();

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

    public synchronized void findPlayer(int userId) {
        if (started) {
            return;
        }

        User roomOwner = getRoomOwner();
        if (roomOwner.getUserId() != userId) {
            return;
        }

        List<User> userList = ApplicationContext.getInstance()
                .getBean(ServerManager.class).findWaitPlayers(userId);

        try {
            Message ms = new Message(Cmd.FIND_PLAYER);
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
            messageSender.sendTo(roomOwner, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void inviteToRoom(int userId) {
        User roomOwner = getRoomOwner();

        User user = ApplicationContext.getInstance()
                .getBean(ServerManager.class).getUserByUserId(userId);
        if (user == null) {
            roomOwner.sendServerMessage(GameString.INVITE_OFFLINE);
            return;
        }

        if (user.isNotWaiting()) {
            roomOwner.sendServerMessage(GameString.INVITE_ALREADY_IN_GAME);
            return;
        }

        if (user.isInvitationLocked()) {
            roomOwner.sendServerMessage(GameString.INVITE_DISABLED);
            return;
        }

        try {
            Message ms = new Message(Cmd.FIND_PLAYER);
            DataOutputStream ds = ms.writer();
            ds.writeBoolean(false);
            ds.writeUTF(GameString.createInviteMessage(roomOwner.getUsername()));
            ds.writeByte(room.getIndex());
            ds.writeByte(id);
            ds.writeUTF(password);
            ds.flush();
            messageSender.sendTo(user, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInfo(User user) {
        try {
            Message ms = new Message(Cmd.AUTO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeByte(room.getIndex());
            ds.writeByte(id);
            ds.writeUTF(name);
            ds.writeByte(room.getType());
            ds.flush();
            messageSender.sendTo(user, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void addUser(User us) throws IOException {
        if (room.getType() == 6 && !us.hasClan()) {
            us.sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
            return;
        }

        if (started || (isContinuous() && continuousLevel > 0)) {
            us.sendServerMessage(GameString.AREA_JOIN_IN_PROGRESS);
            return;
        }

        if (money > us.getXu()) {
            us.sendServerMessage(GameString.AREA_INSUFFICIENT_FUNDS);
            return;
        }

        if (numPlayers >= maxSetPlayers) {
            us.sendServerMessage(GameString.AREA_FULL);
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
            ds.writeInt(us.getUserId());
            ds.writeShort(us.getClanId());
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
                ds.writeShort(user.getClanId());
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
        messageSender.sendTo(us, ms);

        sendUpdateMap(us);
        sendUpdateItemSlot(us);
    }

    public void startGiftBoxOpening(boolean isBlueWin) {
        List<User> winners = new ArrayList<>();

        int startIndex = isBlueWin ? 0 : 1;

        for (int i = startIndex; i < users.length; i += 2) {
            User us = users[i];
            if (us != null) {
                winners.add(us);
            }
        }

        giftBoxService.startGiftBoxOpening(winners, 2);
    }

    public void openGiftBoxAfterFight(int userId, byte boxIndex) {
        giftBoxService.openGiftBoxAfterFight(userId, boxIndex);
    }
}
