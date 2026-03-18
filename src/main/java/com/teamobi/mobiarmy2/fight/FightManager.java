package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.MatchResult;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.Equipment;
import com.teamobi.mobiarmy2.entity.SpecialItemChest;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.fight.boss.*;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.server.ClanItemManager;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.service.ClanService;
import com.teamobi.mobiarmy2.util.RandomUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.Predicate;

@Slf4j
public class FightManager {
    private static final int MAX_ELEMENT_FIGHT = 100;
    private static final int MAX_USER_FIGHT = 8;
    private static final int MAX_PLAY_TIME = 30;
    private static final byte[][] BOSS_COUNTS = {
            {4, 6, 6, 8, 8, 8, 10, 10},
            {4, 6, 6, 6, 8, 8, 10, 10},
            {4, 6, 6, 8, 8, 8, 10, 10},
            {2, 2, 3, 3, 4, 4, 5, 5},
            {4, 5, 5, 6, 6, 7, 7, 8},
            {4, 5, 5, 6, 8, 8, 9, 9},
            null,
            {4, 5, 5, 6, 8, 8, 9, 9},
            {4, 5, 5, 6, 8, 8, 9, 9},
            {4, 5, 5, 6, 8, 8, 9, 9},
    };

    //Danh sách id boss không có lượt chơi
    private static final Set<Byte> INVALID_CHARACTER_IDS = new HashSet<>(Set.of((byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 23, (byte) 24));
    private static final Set<Byte> UNAUTHORIZED_ITEMS = Set.of((byte) 9, (byte) 28, (byte) 30, (byte) 31);
    public static final int MAX_SKIP_TURNS = 3;

    private final FightWait fightWait;

    @Getter
    private final FightMapManager fightMapManager;
    private final BulletManager bulletManager;
    private final CountdownTimer countdownTimer;
    private final ScheduledExecutorService fightLoop = Executors.newSingleThreadScheduledExecutor();
    private final List<Boss> pendingBosses = new ArrayList<>();
    private final ClanService clanService;

    @Getter
    private Player[] players;
    @Getter
    private int totalPlayers;
    @Getter
    private int turnCount;
    private boolean isBossTurn;
    private int playerTurn = -1;
    private int bossTurn = -1;
    @Getter
    private byte windX;
    @Getter
    private byte windY;
    private long startTime;
    private int nextBossId = -10;

    private ScheduledFuture<?> autoNextTurnTask;
    private long lastShootTime;

    private int teamBlueSkippedTurns = 0;
    private int teamRedSkippedTurns = 0;
    private boolean hasActionInTurn = false;

    public FightManager(FightWait fightWait, ClanService clanService) {
        this.fightWait = fightWait;
        this.clanService = clanService;
        this.players = new Player[MAX_ELEMENT_FIGHT];
        this.fightMapManager = new FightMapManager(this);
        this.bulletManager = new BulletManager(this);
        this.countdownTimer = new CountdownTimer(MAX_PLAY_TIME + 10, this::nextTurn);
    }

    /**
     * Khởi tạo và bắt đầu một ván chơi mới.
     */
    public void startGame() {
        fightLoop.submit(wrap(() -> {
            // Tính toán điểm đồng đội
            int totalTeamPointsBlue = 0;
            int totalTeamPointsRed = 0;
            byte roomType = fightWait.getRoomType();
            byte numPlayers = fightWait.getNumPlayers();

            for (byte i = 0; i < MAX_USER_FIGHT; i++) {
                User user = fightWait.getUsers()[i];
                if (user == null) {
                    continue;
                }

                //Lấy danh sách items của clan
                boolean[] clanItems;
                if (user.hasClan()) {
                    clanItems = clanService.getClanItems(user.getClanId());
                } else {
                    clanItems = new boolean[ClanItemManager.CLAN_ITEM_MAP.size()];
                }

                byte teamPointsPercentBonus = 0; // Phần trăm bonus điểm đồng đội từ clan items, nếu có
                if (clanItems[2]) {
                    teamPointsPercentBonus += 5;
                }
                if (clanItems[9]) {
                    teamPointsPercentBonus += 10;
                }

                if (roomType == 5) {
                    if (numPlayers > 1) {
                        totalTeamPointsBlue += user.calculateTeamPoints(teamPointsPercentBonus);
                    }
                } else {
                    if (i % 2 == 0) {
                        if (numPlayers > 3) {
                            totalTeamPointsBlue += user.calculateTeamPoints(teamPointsPercentBonus);
                        }
                    } else {
                        if (numPlayers > 3) {
                            totalTeamPointsRed += user.calculateTeamPoints(teamPointsPercentBonus);
                        }
                    }
                }
            }

            short teamPointsBlue = (short) (totalTeamPointsBlue / 200);
            short teamPointsRed = (short) (totalTeamPointsRed / 200);

            log.info("Team points - Blue: {}, Red: {}", teamPointsBlue, teamPointsRed);

            //Tải dữ liệu bản đồ
            fightMapManager.loadMapId(fightWait.getMapId());

            //Tải dữ liệu vị trí
            List<short[]> randomPositions = fightMapManager.getRandomPlayerPositions(MAX_USER_FIGHT);

            for (byte i = 0; i < MAX_USER_FIGHT; i++) {
                User user = fightWait.getUsers()[i];
                if (user == null) {
                    continue;
                }

                //Lấy ra vị trí
                short x = randomPositions.get(i)[0];
                short y = randomPositions.get(i)[1];

                //Lấy điểm đồng đội
                short teamPoints;
                boolean isTeamBlue = false;
                if (fightWait.getRoomType() == 5 || i % 2 == 0) {
                    teamPoints = teamPointsBlue;
                    isTeamBlue = true;
                } else {
                    teamPoints = teamPointsRed;
                }

                //Lấy ra chỉ số
                int[] abilities = user.calculateCharacterAbilities(teamPoints);

                //Lấy danh sách items của clan
                boolean[] clanItems;
                if (user.hasClan()) {
                    clanItems = clanService.getClanItems(user.getClanId());
                    } else {
                    clanItems = new boolean[ClanItemManager.CLAN_ITEM_MAP.size()];
                }

                //Xóa túi đựng item nếu sử dụng
                byte[] items = fightWait.getItems(i);
                if (items != null) {
                    for (int j = 4; j < items.length; j++) {
                        if (items[j] > 0) {
                            user.updateFightItems((byte) (12 + j - 4), (byte) -1);
                        }
                    }
                }

                //Trừ xu cược
                user.updateXu(-fightWait.getMoney());

                //Cập nhật trạng thái người chơi
                user.setState(UserState.FIGHTING);

                players[i] = new Player(this, user, i, isTeamBlue, x, y, items, abilities, clanItems);
                log.info("Player [{}]: Hp={}/{}, DamePt={}, Def={}, Luck={}", user.getUsername(), players[i].getHp(), players[i].getMaxHp(), players[i].getDamagePercent(), players[i].getDefense(), players[i].getLuck());
            }

            //Cập nhật trang thái game
            startTime = System.currentTimeMillis();
            totalPlayers = MAX_USER_FIGHT;

            if (fightWait.getMoney() > 0) {
                updateMoneyPlayers(-fightWait.getMoney());
            }

            for (int i = 0; i < MAX_USER_FIGHT; i++) {
                Player player = players[i];
                if (player == null || player.getUser() == null) {
                    continue;
                }
                sendFightInfo(player, (player.isTeamBlue() ? teamPointsBlue : teamPointsRed));
            }

            // Tạo boss nếu là chế độ đấu trùm
            if (fightWait.getRoomType() == 5) {
                initMapBosses();
            }

            // Bắt đầu lượt chơi đầu tiên
            doNextTurn();
        }));
    }

    public Future<?> leaveGame(int userId) {
        return fightLoop.submit(wrap(() -> {
            int index = getPlayerIndexByUserId(userId);
            if (index == -1) {
                return;
            }

            //Cập nhật thông tin người chơi
            Player player = players[index];
            player.die();
            player.getUser().updateCup(-5);
            player.setUser(null);

            //Gửi thông báo đến ván chơi
            fightWait.chatMessage(userId, GameString.ESCAPED_GAME);

            //Chuyển lượt nếu người chơi đang trong lượt
            if (index == getCurrentTurn()) {
                doNextTurn();
            } else {
                sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);
            }
        }));
    }

    public void handlePlayerShoot(int userId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
        fightLoop.submit(wrap(() -> {
            Player player = getPlayerTurn();
            if (player.getUser() == null || player.getUser().getUserId() != userId) {
                return;
            }

            // Cập nhật vị trí người chơi
            player.updateXY(x, y);

            // Tạo đạn bắn
            createShoot(player, bullId, angle, force, force2, numShoot);
        }));
    }

    public void changeLocation(int userId, short x, short y) {
        fightLoop.submit(wrap(() -> {
            Player player = getPlayerTurn();
            if (player.getUser() == null || player.getUser().getUserId() != userId) {
                return;
            }

            //Lưu lại vị trí ban đầu
            int preX = player.getX();
            int preY = player.getY();

            //Cập nhật vị trí mới
            player.updateXY(x, y);

            //Gửi thông báo nếu vị trí thay đổi
            if (preX != player.getX() || preY != player.getY()) {
                sendMessageUpdateXY(player.getIndex());
            }
        }));
    }

    public void skipTurn(int userId) {
        fightLoop.submit(wrap(() -> {
            Player player = getPlayerTurn();
            if (player.getUser() == null || player.getUser().getUserId() != userId) {
                return;
            }

            if (player.getSkippedTurns() < MAX_SKIP_TURNS) {
                player.incrementSkippedTurns();
                hasActionInTurn = true;
                doNextTurn();
            }
        }));
    }

    public void updatePlayerCoordinates(int userId, short x, short y) {
        Player player = getPlayerTurn();
        if (player == null || player.getUser() == null || player.getUser().getUserId() != userId) {
            return;
        }
        //Todo update player coordinates if needed
        System.out.println("Player " + player.getUser().getUsername() + " updated coordinates to (" + x + ", " + y + ")");
    }

    public void useItem(int userId, byte itemIndex) {
        fightLoop.submit(wrap(() -> {
            Player player = getPlayerTurn();
            if (player.getUser() == null || player.getUser().getUserId() != userId) {
                return;
            }

            if (player.isItemUsed() || player.isUsePow()) {
                return;
            }

            //Khi đấu boss thì cấm dùng 1 số item
            if (fightWait.getRoomType() == 5 && UNAUTHORIZED_ITEMS.contains(itemIndex)) {
                player.getUser().sendMoneyErrorMessage(GameString.ITEM_UNAUTHORIZED);
                return;
            }

            if (itemIndex == 100) {//Nếu là pow thì kiểm tra angry
                if (player.getAngry() < 100) {
                    return;
                } else {
                    player.setAngry((byte) 0);
                    player.setUsePow(true);
                }
            } else { //Kiểm tra người chơi có mang theo item hay không
                int slot = -1;
                byte[] items = player.getItems();
                for (byte i = 0; i < items.length; i++) {
                    if (items[i] == itemIndex) {
                        slot = i;
                    }
                }
                if (slot == -1) {
                    return;
                }

                player.usedItem(slot);
                player.getUser().updateFightItems(itemIndex, (byte) -1);
            }

            sendUseItemMessage(itemIndex, player.getIndex());

            //Xử lý khi dùng item
            handleItem(player, itemIndex);
            hasActionInTurn = true;
        }));
    }

    public void addPendingBoss(Boss player) {
        pendingBosses.add(player);
    }

    private void refreshFightManager() {
        players = new Player[MAX_ELEMENT_FIGHT];
        totalPlayers = MAX_USER_FIGHT;
        turnCount = 0;
        isBossTurn = false;
        playerTurn = -1;
        bossTurn = -1;
        windX = 0;
        windY = 0;
        startTime = 0;
        nextBossId = -10;
        teamBlueSkippedTurns = 0;
        teamRedSkippedTurns = 0;
        hasActionInTurn = false;
        countdownTimer.stop();
        bulletManager.resetBullets();
    }

    private void sendLuckyUpdate(byte index) {
        try {
            Message ms = new Message(Cmd.LUCKY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPoisonUpdate(byte index) {
        try {
            Message ms = new Message(Cmd.POISON);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEyeSmokeUpdate(byte type, byte index) {
        try {
            Message ms = new Message(Cmd.EYE_SMOKE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);// 0: add, 1: remove
            ds.writeByte(index);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFreezeUpdate(byte type, byte index) {
        try {
            Message ms = new Message(Cmd.FREEZE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);// 0: add, 1: remove
            ds.writeByte(index);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEndInvisible(byte whoEnd) {
        try {
            Message ms = new Message(Cmd.END_INVISIBLE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(whoEnd);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHpUpdate(byte index) {
        try {
            Player player = players[index];
            Message ms = new Message(Cmd.UPDATE_HP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeShort(player.getHp());
            ds.writeByte(player.getPixel());
            ds.flush();
            fightWait.sendToTeam(ms);
            player.setUpdateHP(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateCoordinates(byte index) {
        try {
            Player player = players[index];
            Message ms = new Message(Cmd.UPDATE_XY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeShort(player.getX());
            ds.writeShort(player.getY());
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAngryUpdate(byte index) {
        try {
            Player player = players[index];
            Message ms = new Message(Cmd.ANGRY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeByte(player.getAngry());
            ds.flush();
            fightWait.sendToTeam(ms);
            player.setUpdateAngry(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMoneyUpdate(Player player, int money) {
        try {
            User user = player.getUser();
            Message ms = new Message(Cmd.BONUS_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getUserId());
            ds.writeInt(money);
            ds.writeInt(user.getXu());
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMssAddBosses(List<Boss> bosses) {
        try {
            Message ms = new Message(Cmd.GET_BOSS);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bosses.size());
            for (Boss boss : bosses) {
                ds.writeInt(nextBossId--);
                ds.writeUTF(boss.getName());
                ds.writeInt(boss.getMaxHp());
                ds.writeByte(boss.getCharacterId());
                ds.writeShort(boss.getX());
                ds.writeShort(boss.getY());
            }
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendNextTurnMessage(int turn) {
        try {
            Message ms = new Message(Cmd.NEXT_TURN);
            DataOutputStream ds = ms.writer();
            ds.writeByte(turn);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCapture(byte index, byte toIndex) {
        try {
            Message ms = new Message(Cmd.CAPTURE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeByte(toIndex);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBulletHit(byte index, byte toIndex) {
        try {
            Message ms = new Message(Cmd.BIT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeByte(toIndex);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRewardMessage(Player player, Reward reward) {
        try {
            Message ms = new Message(Cmd.GIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);//index gift
            ds.writeByte(player.getIndex());//player index
            ds.writeByte(reward.getType());//gift type
            switch (reward.getType()) {
                //xu
                case 0 -> ds.writeShort(reward.getXu());

                //item fight
                case 1 -> {
                    ds.writeByte(reward.getItemIndex());
                    ds.writeByte(reward.getQuantity());
                }

                //equip
                case 2 -> {
                    Equipment equip = reward.getEquip().getEquipment();
                    ds.writeByte(equip.getCharacterId());
                    ds.writeByte(equip.getEquipType());
                    ds.writeShort(equip.getEquipIndex());
                    ds.writeUTF(equip.getName());
                }

                //xp
                case 3 -> ds.writeByte(reward.getXp());

                //notification
                case 4 -> {
                    SpecialItemChest specialItem = reward.getSpecialItem();
                    ds.writeUTF(specialItem.getItem().getName());
                }
            }
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPlayerFlyPosition(byte index) {
        Player player = players[index];
        try {
            Message ms = new Message(Cmd.FLY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeShort(player.getX());
            ds.writeShort(player.getY());
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendGhostAttackInfo(byte index, byte toIndex) {
        try {
            Message ms = new Message(Cmd.GHOST_BIT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeByte(toIndex);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessageUpdateXY(int index) {
        try {
            Player player = players[index];
            Message ms = new Message(Cmd.MOVE_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeShort(player.getX());
            ds.writeShort(player.getY());
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendWindUpdate() {
        try {
            Message ms = new Message(Cmd.WIND);
            DataOutputStream ds = ms.writer();
            ds.writeByte(windX);
            ds.writeByte(windY);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendUseItemMessage(byte itemIndex, int index) {
        try {
            Message ms = new Message(Cmd.USE_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeByte(itemIndex);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFightInfo(Player player, int teamPoints) {
        try {
            Message ms = new Message(Cmd.START_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(fightWait.getMapId());
            ds.writeByte(MAX_PLAY_TIME);
            ds.writeShort(teamPoints);
            for (int j = 0; j < MAX_USER_FIGHT; j++) {
                Player pl = players[j];
                if (pl == null) {
                    ds.writeShort(-1);
                    continue;
                }
                ds.writeShort(pl.getX());
                ds.writeShort(pl.getY());
                ds.writeShort(pl.getMaxHp());
            }

            ds.flush();
            player.getUser().sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLuckUpdates() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null || player.isDead()) {
                continue;
            }
            player.nextLuck();
        }
    }

    private void updateLuckyPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null || player.isDead() || !player.isLucky()) {
                continue;
            }
            sendLuckyUpdate(i);
            player.setLucky(false);
        }
    }

    private void updatePlayerStatuses() {
        for (byte i = 0; i < totalPlayers; i++) {
            Player player = players[i];
            if (player == null) {
                continue;
            }
            if (player.isUpdateHP()) {
                sendHpUpdate(i);
            }
            if (player.isUpdateAngry()) {
                sendAngryUpdate(i);
            }
        }
    }

    private void updateMoneyPlayers(int money) {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null) {
                continue;
            }
            sendMoneyUpdate(player, money);
        }
    }

    private void updateXpPlayers() {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null || !player.isUpdateXP()) {
                continue;
            }
            player.getUser().updateXp(player.getXpUp(), true);

            player.setXpUp(0);
            player.setUpdateXP(false);
        }
    }

    private void updateCupPlayers() {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null || !player.isUpdateCup()) {
                continue;
            }
            player.getUser().updateCup(player.getCupUp());

            player.setCupUp(0);
            player.setUpdateCup(false);
        }
    }

    private int getPlayerIndexByUserId(int userId) {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null
                    && players[i].getUser() != null
                    && players[i].getUser().getUserId() == userId) {
                return i;
            }
        }
        return -1;
    }

    private void updateWind() {
        Player player = getPlayerTurn();
        if (player.getWindStopCount() > 0) {
            player.decreaseWindStopCount();

            windX = 0;
            windY = 0;
        } else {
            int[] range = getWindRange(player);

            if (RandomUtil.nextInt(0, 100) > 25) {
                windX = (byte) RandomUtil.nextInt(-range[0], range[0]);
                windY = (byte) RandomUtil.nextInt(-range[1], range[1]);
            }
        }

        sendWindUpdate();
    }

    /**
     * Lấy phạm vi gió dựa trên ID nhân vật của người chơi.
     *
     * @param player
     * @return mảng chứa phạm vi gió theo trục X và Y.
     */
    private int[] getWindRange(Player player) {
        if (player.getCharacterId() == 9) {
            return new int[]{60, 25};
        }
        return new int[]{70, 70};
    }

    private int getCurrentTurn() {
        if (isBossTurn) {
            return bossTurn;
        }
        return playerTurn;
    }

    /**
     * Khởi tạo ngẫu nhiên vị trí, số lượng và các loại chi tiết của Boss dựa theo ID của bản đồ.
     * Số lượng Boss sẽ tự động co can (scale) theo số người chơi tham gia trong phòng (sử dụng mảng BOSS_COUNTS).
     */
    private void initMapBosses() {
        byte playerCount = fightWait.getNumPlayers();
        List<Boss> spawnBosses = new ArrayList<>();

        switch (fightWait.getMapId()) {
            case 30 -> {//Bom 1
                byte bossCount = BOSS_COUNTS[0][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short bossX = (short) ((i % 2 == 0) ? RandomUtil.nextInt(95, 315) : RandomUtil.nextInt(890, 1070));
                    short bossY = (short) (50 + 40 * RandomUtil.nextInt(3));
                    short bossHealth = 1000;
                    Boss boss = new BigBoom(this, bossX, bossY, bossHealth);
                    spawnBosses.add(boss);
                }
            }

            case 31 -> {//Bom 2
                byte bossCount = BOSS_COUNTS[1][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short bossX = (short) (RandomUtil.nextInt(445, 800) + i * 50);
                    short bossY = 180;
                    short bossHealth = 1500;
                    Boss boss = new BigBoom(this, bossX, bossY, bossHealth);
                    spawnBosses.add(boss);
                }
            }

            case 32 -> {//Nhện máy
                short[] tempX = new short[]{505, 1010, 743, 425, 1068};
                short[] tempY = new short[]{221, 221, 198, 369, 369, 369};
                byte bossCount = BOSS_COUNTS[2][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    Boss boss = new RobotSpider(this, tempX[i], tempY[i], (short) 1500);
                    spawnBosses.add(boss);
                }
            }

            case 33 -> {//Thành phố máy
                short[] tempX = new short[]{420, 580, 720, 240, 55, 900};
                byte bossCount = BOSS_COUNTS[3][playerCount - 1];
                for (int i = 0; i < bossCount; i++) {
                    short X = tempX[i];
                    short Y = 200;
                    Boss boss = new Robot(this, X, Y, (short) 3700);
                    spawnBosses.add(boss);
                }
            }

            case 34 -> {// T. rex máy
                short X = 886;
                short Y = 428;
                Boss tRex = new TRex(this, X, Y, (short) 15000);
                spawnBosses.add(tRex);

                byte bossCount = BOSS_COUNTS[4][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    X = (short) (RandomUtil.nextInt(470, 755));
                    Boss bigBoom = new BigBoom(this, X, Y, (short) 1500);
                    spawnBosses.add(bigBoom);
                }
            }

            case 35 -> {//Khu vực cấm
                byte bossCount = BOSS_COUNTS[5][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) (RandomUtil.nextInt(300, 800));
                    short Y = (short) RandomUtil.nextInt(-350, 100);
                    Boss boss = new UFO(this, X, Y, (short) 4500);
                    spawnBosses.add(boss);
                }
            }

            case 36 -> {//Đỉnh hi mã lạp sơn
                short X = (short) (RandomUtil.nextInt(300, 800));
                short Y = (short) RandomUtil.nextInt(-350, 100);

                Balloon balloon = new Balloon(this, X, Y);
                balloon.getBodyParts()[0] = balloon;
                spawnBosses.add(balloon);

                BalloonGun balloonGun = new BalloonGun(this, (short) (X + 51), (short) (Y + 19), (short) 2000);
                balloon.getBodyParts()[1] = balloonGun;
                spawnBosses.add(balloonGun);

                BalloonGunBig balloonGunBig = new BalloonGunBig(this, (short) (X - 5), (short) (Y + 30), (short) 2500);
                balloon.getBodyParts()[2] = balloonGunBig;
                spawnBosses.add(balloonGunBig);

                BalloonFanBack balloonFanBack = new BalloonFanBack(this, (short) (X - 67), (short) (Y - 6), (short) 1000);
                balloon.getBodyParts()[3] = balloonFanBack;
                spawnBosses.add(balloonFanBack);
            }

            case 37 -> {//Nhện độc
                byte bossCount = BOSS_COUNTS[7][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) RandomUtil.nextInt(20, fightMapManager.getWidth() - 20);
                    short Y = (short) 250;
                    Boss boss = new VenomousSpider(this, X, Y, (short) 3800);
                    spawnBosses.add(boss);
                }
            }

            case 38 -> {//Nghĩa trang 1
                byte bossCount = BOSS_COUNTS[8][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) ((short) 700 - i * 80);
                    short Y = (short) (RandomUtil.nextInt(30));
                    Boss boss = new Ghost(this, X, Y, (short) 1800);
                    spawnBosses.add(boss);
                }
            }

            case 39 -> {//Nghĩa trang 2
                byte bossCount = BOSS_COUNTS[9][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) (700 - i * 80);
                    short Y = (short) RandomUtil.nextInt(30);
                    Boss boss = new Ghost2(this, X, Y, (short) 1800);
                    spawnBosses.add(boss);
                }
            }
        }

        //Thêm boss vào danh sách
        spawnBosses(spawnBosses);
    }

    public short[] getForceArgXY(int idGun, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100) {
        return null;
    }

    public void nextTurn() {
        fightLoop.submit(wrap(this::doNextTurn));
    }

    /**
     * Xử lý logic chuyển đổi sang lượt tiếp theo của trận đấu.
     */
    public void doNextTurn() {
        log.info("Turn {} complete. Processing next turn...", turnCount);

        if (turnCount > 0) {
            if (!isBossTurn && playerTurn != -1 && players[playerTurn] != null) {
                Player p = players[playerTurn];
                if (!hasActionInTurn) {
                    if (fightWait.getRoomType() == 5) {
                        p.setInactiveTurns((byte) (p.getInactiveTurns() + 1));
                        log.info("Player {} inactive for {} turns", p.getUser().getUsername(), p.getInactiveTurns());
                        if (p.getInactiveTurns() >= 2) {
                            log.info("Player {} died due to inactivity", p.getUser().getUsername());
                            p.die();
                        }
                    } else {
                        if (p.isTeamBlue()) {
                            teamBlueSkippedTurns++;
                            log.info("Team Blue skipped {} consecutive turns", teamBlueSkippedTurns);
                        } else {
                            teamRedSkippedTurns++;
                            log.info("Team Red skipped {} consecutive turns", teamRedSkippedTurns);
                        }
                    }
                } else {
                    if (fightWait.getRoomType() == 5) {
                        p.setInactiveTurns((byte) 0);
                    } else {
                        if (p.isTeamBlue()) {
                            teamBlueSkippedTurns = 0;
                        } else {
                            teamRedSkippedTurns = 0;
                        }
                    }
                }
            }
            hasActionInTurn = false;
        }

        //Cập nhật vị trí y của các player
        for (int i = 0; i < totalPlayers; i++) {
            Player player = players[i];
            if (player == null) {
                continue;
            }
            player.updateYPosition();
        }

        //Cập nhật trạng thái người chơi
        updatePlayerStatuses();

        //Cập nhật số xp nhận được
        updateXpPlayers();

        //Cập nhật số cup nhận được
        updateCupPlayers();

        //Kiểm tra kết quả trận đấu
        MatchResult result = getMatchResult();
        if (result != null) {
            fightComplete(result);
            return;
        }

        turnCount++;

        // Tính lượt chơi tiếp theo
        if (playerTurn == -1) {
            initFirstTurn();
        } else {
            calculateNextTurn();
        }

        //Đặt lại giá trị của người chơi trong lượt mới như thể lực, ..., vv
        if (isBossTurn) {
            Boss boss = (Boss) players[bossTurn];
            boss.resetValueInNewTurn();
        } else {
            Player player = players[playerTurn];
            player.resetValueInNewTurn();
            player.updateAngry((byte) 10);

            //Giảm số lần hút máu
            if (player.getVampireCount() > 0) {
                player.setVampireCount((byte) (player.getVampireCount() - 1));
            }

            //Giảm số lần vô hình
            if (player.getInvisibleCount() > 0) {
                player.setInvisibleCount((byte) (player.getInvisibleCount() - 1));
                if (player.getInvisibleCount() == 0) {
                    sendEndInvisible(player.getIndex());
                }
            }

            //Giảm số lần tàn hình
            if (player.getVanishCount() > 0) {
                player.setVanishCount((byte) (player.getVanishCount() - 1));
                if (player.getVanishCount() == 0) {
                    sendEndInvisible(player.getIndex());
                }
            }

            //Giảm số lần bom mù
            if (player.getEyeSmokeCount() > 0) {
                player.setEyeSmokeCount((byte) (player.getEyeSmokeCount() - 1));
                if (player.getEyeSmokeCount() == 0) {
                    sendEyeSmokeUpdate((byte) 1, player.getIndex());
                }
            }

            //Giảm số lần đóng băng
            if (player.getFreezeCount() > 0) {
                player.setFreezeCount((byte) (player.getFreezeCount() - 1));
                if (player.getFreezeCount() == 0) {
                    sendFreezeUpdate((byte) 1, player.getIndex());
                }
            }
        }

        //Spawn boss nếu có
        spawnBosses(pendingBosses);
        pendingBosses.clear();

        // Random gió
        updateWind();

        // Gửi thông báo lượt chơi tiếp theo
        sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);

        //Cài đồng hồ đếm ngược
        if (isBossTurn) {
            countdownTimer.stop();// Boss không có giới hạn thời gian
        } else {
            countdownTimer.reset();// Người chơi có giới hạn thời gian
        }

        // Thực hiện hành động của boss trong lượt
        if (isBossTurn) {
            Boss boss = (Boss) players[bossTurn];

            // Đợi 2 giây trước khi boss hành động
            fightLoop.schedule(wrap(() -> {
                if (turnCount == 1) {
                    doNextTurn();
                } else {
                    boss.turnAction();
                }
            }), 2, TimeUnit.SECONDS);
        }
    }

    /**
     * Chọn ngẫu nhiên một người chơi (hoặc Boss nếu là chế độ Boss) đáp ứng đủ yêu cầu để trở thành
     * người khai hỏa đi lượt đầu tiên của màn chơi.
     */
    private void initFirstTurn() {
        byte roomType = fightWait.getRoomType();
        while (true) {
            int next;
            if (roomType == 5) {
                next = RandomUtil.nextInt(MAX_USER_FIGHT, totalPlayers);
            } else {
                next = RandomUtil.nextInt(MAX_USER_FIGHT);
            }
            if (players[next] != null && !INVALID_CHARACTER_IDS.contains(players[next].getCharacterId())) {
                if (next < MAX_USER_FIGHT) {
                    playerTurn = next;
                    bossTurn = MAX_USER_FIGHT;
                    isBossTurn = false;
                } else {
                    playerTurn = 0;
                    bossTurn = next;
                    isBossTurn = true;
                }
                break;
            }
        }
    }

    private void calculateNextTurn() {
        byte roomType = fightWait.getRoomType();
        if (roomType == 5) {
            if (isBossTurn) {
                playerTurn = getNextValidTurn(playerTurn, 0, MAX_USER_FIGHT);
            } else {
                bossTurn = getNextValidTurn(bossTurn, MAX_USER_FIGHT, totalPlayers);
            }
            isBossTurn = !isBossTurn;
        } else {
            playerTurn = getNextValidTurn(playerTurn, 0, MAX_USER_FIGHT);
        }
    }

    private void spawnBosses(List<Boss> bosses) {
        List<Boss> addedBosses = new ArrayList<>();
        for (Boss boss : bosses) {
            if (totalPlayers >= FightManager.MAX_ELEMENT_FIGHT) {
                break;
            }

            players[totalPlayers] = boss;
            boss.index = (byte) totalPlayers;
            totalPlayers++;

            addedBosses.add(boss);
        }

        if (addedBosses.isEmpty()) {
            return;
        }

        sendMssAddBosses(addedBosses);
    }

    public void giveXpToTeammates(boolean isTeamBlue, int addXP, Player sharer) {
        int i = isTeamBlue ? 0 : 1;
        int step = fightWait.getRoomType() == 5 ? 1 : 2;

        for (; i < MAX_USER_FIGHT; i += step) {
            Player player = players[i];
            if (player != sharer
                    && player != null
                    && player.getUser() != null
                    && !player.isDead()
            ) {
                player.updateXp(addXP, false);
            }
        }
    }

    private int getNextValidTurn(int currentTurn, int min, int limit) {
        int turn = currentTurn + 1;
        while (turn != currentTurn) {
            if (turn == limit) {
                turn = min;
            }
            Player player = players[turn];
            if (player != null && !player.isDead() && !INVALID_CHARACTER_IDS.contains(player.getCharacterId())) {
                return turn;
            }
            turn++;
        }
        return currentTurn;
    }

    /**
     * Kiểm tra kết quả trận đấu
     *
     * @return MatchResult nếu trận đấu đã kết thúc (DRAW/BLUE_WIN/RED_WIN),
     * null nếu trận đấu đang tiếp tục hoặc chưa bắt đầu
     */
    private MatchResult getMatchResult() {
        if (!fightWait.isStarted()) {
            return null;
        }

        if (fightWait.getRoomType() == 5) {
            int playerAliveCount = 0, bossAliveCount = 0, i = 0;
            while (i < MAX_USER_FIGHT) {
                Player player = players[i];
                if (player != null && !player.isDead()) {
                    playerAliveCount++;
                }
                i++;
            }
            while (i < totalPlayers) {
                Boss boss = (Boss) players[i];
                if (boss != null && !boss.isDead()) {
                    bossAliveCount++;
                }
                i++;
            }
            if (playerAliveCount == 0 || bossAliveCount == 0) {
                if (playerAliveCount == 0) {
                    return MatchResult.RED_WIN;
                } else {
                    return MatchResult.BLUE_WIN;
                }
            } else {
                return null;
            }
        } else {
            if (teamBlueSkippedTurns >= 2 || teamRedSkippedTurns >= 2) {
                return MatchResult.DRAW;
            }
            int redAliveCount = 0, blueAliveCount = 0;
            for (byte i = 0; i < MAX_USER_FIGHT; i++) {
                Player player = players[i];
                if (player == null) {
                    continue;
                }
                if (!player.isDead()) {
                    if (player.isTeamBlue()) {
                        blueAliveCount++;
                    } else {
                        redAliveCount++;
                    }
                }
            }
            if (redAliveCount == 0 || blueAliveCount == 0) {
                if (redAliveCount == blueAliveCount) {
                    return MatchResult.DRAW;
                } else if (redAliveCount == 0) {
                    return MatchResult.BLUE_WIN;
                } else {
                    return MatchResult.RED_WIN;
                }
            } else {
                return null;
            }
        }
    }

    /**
     * Xử lý tất toán các thủ tục khi trận đấu kết thúc (có phe thắng/thua hoặc hòa).
     */
    private void fightComplete(MatchResult result) {
        long duration = System.currentTimeMillis() - startTime;
        boolean fightInValid = false;
        if (duration < 5000) {
            fightInValid = true;
            for (byte i = 0; i < MAX_USER_FIGHT; i++) {
                Player player = players[i];
                if (player == null || player.getUser() == null) {
                    continue;
                }
                player.getUser().sendMoneyErrorMessage(GameString.MATCH_NOT_COUNTED);
            }
        }

        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null) {
                continue;
            }
            User user = player.getUser();

            byte winStatus = 0;//Hòa
            if ((player.isTeamBlue() && result == MatchResult.BLUE_WIN) ||
                    (!player.isTeamBlue() && result == MatchResult.RED_WIN)) {
                winStatus = 1;//THắng
            } else if ((!player.isTeamBlue() && result == MatchResult.BLUE_WIN) ||
                    (player.isTeamBlue() && result == MatchResult.RED_WIN)) {
                winStatus = -1;//Thua
            }

            try {
                //Gửi thông báo kết thúc ván chơi
                Message ms = new Message(Cmd.STOP_GAME);
                DataOutputStream ds = ms.writer();
                ds.writeByte(winStatus);
                ds.writeByte(0);//exBonus
                if (winStatus == 1 || winStatus == 0) {
                    ds.writeInt(fightWait.getMoney());
                } else {
                    ds.writeInt(-fightWait.getMoney());
                }
                ds.flush();
                user.sendMessage(ms);

                //Gửi thông báo số xp và cup nhận được
                user.sendUpdateXp(player.getAllXpUp(), false);
                user.sendUpdateCup(Math.min(player.getAllCupUp(), Byte.MAX_VALUE));

                //Cộng thêm quà nếu trận đấu là hợp lệ
                if (!fightInValid) {
                    //Nếu chiến thắng trong đấu boss thì cộng thêm 10xp
                    if (winStatus == 1 && fightWait.getRoomType() == 5) {
                        user.updateXp(10, true);

                        int chance = RandomUtil.nextInt(100);
                        if (chance < 30) {//30% nhận nguyên liệu
                            short quantity = (short) RandomUtil.nextInt(1, 5);
                            byte id = getRewardMaterialId();

                            SpecialItemChest newItem = new SpecialItemChest(quantity, SpecialItemManager.getSpecialItemById(id));
                            user.updateInventory(null, null, List.of(newItem), null);

                            String reward = String.format("Phần thưởng diệt trùm của bạn là %dx %s", newItem.getQuantity(), newItem.getItem().getName());
                            user.sendServerMessage(reward);
                        } else {
                            StringBuilder reward = new StringBuilder("Phần thưởng diệt trùm của bạn là ");
                            int count = RandomUtil.nextInt(2, 3);
                            for (int k = 0; k < count; k++) {
                                byte indexItem = FightItemManager.getRandomItem();
                                byte quantity = 1;
                                user.updateFightItems(indexItem, quantity);
                                reward.append(quantity).append("x ");
                                reward.append(FightItemManager.FIGHT_ITEMS.get(indexItem).getName()).append(", ");
                            }
                            reward.deleteCharAt(reward.length() - 2);
                            user.sendServerMessage(reward.toString());
                        }
                    }

                    //Cộng xp và cup cho clan
                    if (user.hasClan()) {
                        clanService.updateXp(user.getClanId(), user.getUserId(), player.getAllXpUp() / 100);
                        clanService.updateCup(user.getClanId(), user.getUserId(), player.getAllCupUp());
                    }
                }

                //Cập nhật xu cuối trận
                int xuUp = fightWait.getMoney();
                if (xuUp > 0) {
                    switch (winStatus) {
                        //Thắng
                        case 1 -> {
                            xuUp = xuUp * 2;
                            user.updateXu(xuUp);
                            sendMoneyUpdate(player, xuUp);
                        }

                        //Hòa
                        case 0 -> {
                            user.updateXu(xuUp);
                            sendMoneyUpdate(player, xuUp);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (fightWait.isContinuous()) {
            if (result == MatchResult.BLUE_WIN) {//Nếu là đấu liên hoàn và thắng thì đổi map
                fightWait.nextContinuousLevel();

                //Xóa người chơi bị hạ khỏi phòng
                for (byte i = 0; i < MAX_USER_FIGHT; i++) {
                    Player player = players[i];
                    if (player == null || !player.isDead() || player.getUser() == null) {
                        continue;
                    }
                    User user = player.getUser();
                    fightWait.handleKickPlayer(user.getUserId(), i, GameString.PLAYER_ELIMINATED);
                }
            } else if (result == MatchResult.RED_WIN) { //Nếu thua thì đuổi toàn bộ người chơi
                for (byte i = 0; i < MAX_USER_FIGHT; i++) {
                    Player player = players[i];
                    if (player == null || player.getUser() == null) {
                        continue;
                    }
                    User user = player.getUser();
                    fightWait.handleKickPlayer(user.getUserId(), i, GameString.PLAYER_ELIMINATED);
                }
            }
        }

        //Kết thúc ván đấu sau 8 giây
        fightLoop.schedule(wrap(() -> {
            fightWait.fightComplete();

            //Cập nhật mở quà
            if (turnCount > 5 && fightWait.getRoomType() != 5) {
                //Đợi thêm 2 giây trước khi mở quà (tổng 10s sau khi đấu xong)
                fightLoop.schedule(wrap(() -> {
                    boolean isBlueWin = result == MatchResult.BLUE_WIN;
                    fightWait.startGiftBoxOpening(isBlueWin);
                }), 2, TimeUnit.SECONDS);
            }

            refreshFightManager();
        }), 8, TimeUnit.SECONDS);
    }

    private byte getRewardMaterialId() {
        switch (fightWait.getMapId()) {
            //Bom: Nhôm
            case 30, 31 -> {
                return 62;
            }

            //Nhện máy: Sắt
            case 32 -> {
                return 63;
            }

            //Người máy: Đồng
            case 33 -> {
                return 64;
            }

            //T-rex: Sắt
            case 34 -> {
                return 63;
            }

            //UFO: Gỗ
            case 35 -> {
                return 68;
            }

            //Khí cầu: Vàng
            case 36 -> {
                return 66;
            }

            //Nhện độc: Lông vũ
            case 37 -> {
                return 67;
            }

            //Nghĩa trang: Bạc
            default -> {
                return 65;
            }
        }
    }

    public void createShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        createShoot(player, bullId, angle, force, force2, numShoot, true);
    }

    public void createShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot, boolean isNextTurn) {
        hasActionInTurn = true;
        if (player.isDoubleShoot()) {
            player.setDoubleShoot(false);
        } else {
            numShoot = 1;
        }

        //Tính toán người chơi nào rơi sao
        handleLuckUpdates();

        int xS = player.getX();
        int yS = player.getY();

        bulletManager.addShoot(player, bullId, angle, force, force2, numShoot);
        bulletManager.updateBullets();

        //Gửi ms những người chơi may mắn
        updateLuckyPlayers();

        //Gủi ms bắn đạn
        sendFireArmyPacket(bullId, xS, yS, angle, force2, numShoot, player);

        //Xóa các đạn đã bắn
        bulletManager.resetBullets();

        //Chuyển lượt mới
        if (isNextTurn) {
            lastShootTime = System.currentTimeMillis();

            // Hủy bộ đếm thời gian tự động chuyển lượt nếu đang tồn tại
            if (autoNextTurnTask != null && !autoNextTurnTask.isDone()) {
                autoNextTurnTask.cancel(false);
            }

            // Nếu là Boss bắn (isBossTurn), mặc định 2s tự đổi lượt
            // Nếu là Player bắn, 5s timeout nếu không nhận được SHOOT_RESULT
            int timeout = isBossTurn ? 2 : 5;
            autoNextTurnTask = fightLoop.schedule(wrap(this::doNextTurn), timeout, TimeUnit.SECONDS);
        }
    }

    public void handlePlayerShootResult(int userId) {
        fightLoop.submit(wrap(() -> {
            Player player = getPlayerTurn();
            // Nếu không phải trong trạng thái chờ thì bỏ qua
            if (autoNextTurnTask == null || autoNextTurnTask.isDone() || player == null) {
                return;
            }

            // Chỉ chấp nhận báo cáo từ chính người chơi vừa bắn
            if (player.getUser() == null || player.getUser().getUserId() != userId) {
                return;
            }

            // Hủy bộ đếm thời gian Timeout
            autoNextTurnTask.cancel(false);

            // Kiểm tra xem đã đủ 2s từ lúc bắn chưa
            long elapsedDelay = System.currentTimeMillis() - lastShootTime;

            if (elapsedDelay < 2000) {
                // Nếu chưa tới 2s thì chờ cho đủ 2s
                fightLoop.schedule(wrap(this::doNextTurn), 2000 - elapsedDelay, TimeUnit.MILLISECONDS);
            } else {
                // Nếu thời gian chờ đã hơn 2s thì gọi chuyển lượt luôn
                doNextTurn();
            }
        }));
    }

    private void sendFireArmyPacket(byte bullId, int xS, int yS, short angle, byte force2, byte numShoot, Player player) {
        List<Bullet> bullets = bulletManager.getBullets();
        byte typeShoot = bulletManager.getTypeShoot();
        try {
            Message ms = new Message(Cmd.FIRE_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeShoot);
            ds.writeByte(player.isUsePow() ? 1 : 0);
            ds.writeByte(player.getIndex());
            ds.writeByte(bullId);
            ds.writeShort(xS);
            ds.writeShort(yS);
            ds.writeShort(angle);
            if (bullId == 17 || bullId == 19) {
                ds.writeByte(force2);
            }
            if (bullId == 14 || bullId == 40) {
                ds.writeByte(0);//angle
                ds.writeByte(0);//force
            }
            if (bullId == 44 || bullId == 45 || bullId == 47) {
                ds.writeByte(0);//angle
            }
            ds.writeByte(numShoot);
            ds.writeByte(bullets.size());
            for (Bullet bullet : bullets) {
                List<Point> trajectory = bullet.getTrajectory();
                int size = trajectory.size();
                ds.writeShort(size);// Ghi độ dài quỹ đạo

//                if (log.isDebugEnabled()) {
//                    log.debug("Bullet ID: {}, Trajectory Size: {}", bullId, size);
//                    for (Point p : trajectory) {
//                        log.debug("Bullet Trajectory Point: x={}, y={}", p.getX(), p.getY());
//                    }
//                }

                if (typeShoot == 0) {// Ghi tọa độ theo dạng delta (chênh lệch)
                    for (int i = 0; i < size; i++) {
                        Point point = bullet.getTrajectory().get(i);

                        if (i == 0) {
                            // Điểm đầu tiên: ghi tọa độ tuyệt đối
                            ds.writeShort(point.getX());
                            ds.writeShort(point.getY());
                        } else {
                            if ((i == size - 1) && bullId == 49) {// Điểm cuối của laser Magenta
                                ds.writeShort(point.getX());
                                ds.writeShort(point.getY());
                                ds.writeByte(bullet.getDXLaser());
                                ds.writeByte(bullet.getDYLaser());
                            } else {
                                Point prevPoint = bullet.getTrajectory().get(i - 1);
                                ds.writeByte((byte) (point.getX() - prevPoint.getX()));
                                ds.writeByte((byte) (point.getY() - prevPoint.getY()));
                            }
                        }
                    }
                } else if (typeShoot == 1) { // Ghi tọa độ tuyệt đối cho mỗi điểm
                    for (Point point : bullet.getTrajectory()) {
                        ds.writeShort(point.getX());
                        ds.writeShort(point.getY());
                    }
                }

                if (bullId == 48) {
                    ds.writeByte(bullet.getHitPoints().size());
                    for (Point hit : bullet.getHitPoints()) {
                        ds.writeShort(hit.getX());
                        ds.writeShort(hit.getY());
                    }
                }
            }

            // Ghi thông tin nếu đạn siêu cao
            byte superType = bulletManager.getSuperType();
            ds.writeByte(superType);
            if (superType == 1 || superType == 2) {
                ds.writeShort(bulletManager.getSuperX());
                ds.writeShort(bulletManager.getSuperY());
            }
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleItem(Player player, byte itemIndex) {
        switch (itemIndex) {
            //Hồi máu
            case 0 -> {
                player.updateHP((short) 350);
                sendHpUpdate(player.getIndex());
            }

            //Bắn x2
            case 2 -> player.setDoubleShoot(true);

            //Đi x2
            case 3 -> player.setDoubleSpeed(true);

            //Tàng hình
            case 4 -> player.setVanishCount((byte) 5);

            //Ngưng gió
            case 5 -> {
                player.setWindStopCount((byte) 5);
                updateWind();
            }

            //Hồi máu đồng đội
            case 10 -> {
                byte n = (byte) (fightWait.getRoomType() == 5 ? 1 : 2);
                byte i = (byte) (player.isTeamBlue() ? 0 : 1);
                for (; i < MAX_USER_FIGHT; i += n) {
                    Player pl = players[i];
                    if (pl == null || pl.isDead()) {
                        continue;
                    }
                    short hpUp = (short) (pl.getMaxHp() * 0.3);
                    pl.updateHP(hpUp);
                    sendHpUpdate(i);
                }
            }

            //Tự sát
            case 24 -> createShoot(player, (byte) 50, (short) 0, (byte) 0, (byte) 0, (byte) 1);

            //Ufo
            case 27 -> addPendingBoss(new UFOPet(this, (short) 140, (short) 0, (short) 550));

            //Hồi máu 50%
            case 32 -> {
                short hpUp = (short) (player.getMaxHp() / 2);
                player.updateHP(hpUp);
                sendHpUpdate(player.getIndex());
            }

            //Hồi máu 100%
            case 33 -> {
                player.updateHP(player.getMaxHp());
                sendHpUpdate(player.getIndex());
            }

            //Vô hình
            case 34 -> player.setInvisibleCount((byte) 10);

            //Hút máu
            case 35 -> player.setVampireCount((byte) 2);
        }
    }

    public Player getPlayerTurn() {
        return players[getCurrentTurn()];
    }

    public Player getRandomPlayer(Predicate<Player> condition) {
        List<Player> validPlayers = new ArrayList<>(MAX_USER_FIGHT);

        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];

            boolean isValid = player != null && player.getUser() != null && !player.isDead();

            if (isValid && (condition == null || condition.test(player))) {
                validPlayers.add(player);
            }
        }

        if (validPlayers.isEmpty()) {
            return null;
        }

        return validPlayers.get(RandomUtil.nextInt(validPlayers.size()));
    }

    public Player findClosestPlayer(short targetX, short targetY) {
        Player closestPlayer = null;
        int closestDistanceSquared = Integer.MAX_VALUE;

        for (byte index = 0; index < MAX_USER_FIGHT; index++) {
            Player player = this.players[index];
            if (player == null || player.getUser() == null || player.isDead()) {
                continue;
            }

            int deltaX = player.getX() - targetX;
            int deltaY = player.getY() - targetY;
            int distanceSquared = deltaX * deltaX + deltaY * deltaY;

            if (distanceSquared < closestDistanceSquared) {
                closestDistanceSquared = distanceSquared;
                closestPlayer = player;
            }
        }

        return closestPlayer;
    }

    public Player getRandomPlayer() {
        List<Player> validPlayers = new ArrayList<>();

        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null || player.isDead()) {
                continue;
            }
            validPlayers.add(player);
        }

        if (validPlayers.isEmpty()) {
            return null;
        }

        int randomIndex = RandomUtil.nextInt(0, validPlayers.size() - 1);
        return validPlayers.get(randomIndex);
    }

    public void collisionPlayers(short x, short y, Bullet bullet) {
        for (int i = 0; i < totalPlayers; i++) {
            Player player = players[i];
            if (player == null) {
                continue;
            }

            player.collision(x, y, bullet);
        }
    }

    public void onBulletExplode(short bx, short by, Bullet bullet) {
        int impactRadius = Bullet.getImpactRadiusByBullId(bullet.bullId);

        for (Player player : players) {
            if (player == null || player.isDead() || player.getInvisibleCount() > 0) {
                continue;
            }

            if (isInRange(player, bx, by, impactRadius)) {
                applyBulletEffect(bullet, player);
            }
        }
    }

    private void applyBulletEffect(Bullet bullet, Player player) {
        switch (bullet.getBullId()) {
            case 51 -> {//Bom mù
                if (player.getEyeSmokeCount() <= 0) {
                    sendEyeSmokeUpdate((byte) 0, player.getIndex());
                }
                player.setEyeSmokeCount((byte) 5);
            }

            case 54 -> {//Đóng băng
                if (player.getFreezeCount() <= 0) {
                    sendFreezeUpdate((byte) 0, player.getIndex());
                }
                player.setFreezeCount((byte) 5);
            }

            case 55 -> {//Khói độc
                if (!player.isPoisoned()) {
                    player.setPoisoned(true);
                    sendPoisonUpdate(player.getIndex());
                }
            }
        }
    }

    private boolean isInRange(Player pl, int bx, int by, int radius) {
        int dx = pl.getX() - bx;
        int dy = (pl.getY() - pl.height / 2) - by;

        int distanceSq = dx * dx + dy * dy;
        int hitRadius = radius + pl.width / 2;

        return distanceSq <= hitRadius * hitRadius;
    }

    private Runnable wrap(Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (Exception e) {
                log.error("Exception in fightLoop: ", e);
            }
        };
    }
}
