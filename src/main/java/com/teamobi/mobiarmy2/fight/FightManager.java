package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.MatchResult;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.entity.*;
import com.teamobi.mobiarmy2.entity.boss.*;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.server.ClanItemManager;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.service.ClanService;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

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
    private static final Set<Byte> UNAUTHORIZED_ITEMS = Set.of((byte) 9, (byte) 23, (byte) 26, (byte) 28, (byte) 30, (byte) 31);

    private final FightWait fightWait;
    private final FightMapManager mapManager;
    private final BulletManager bulletManager;
    private final CountdownTimer countdownTimer;
    private final ExecutorService fightLoop = Executors.newSingleThreadExecutor();
    @Getter
    private Player[] players;
    @Getter
    private int totalPlayers;
    @Getter
    private int turnCount;
    private boolean isBossTurn;
    private int playerTurn;
    private int bossTurn;
    @Getter
    private byte windX;
    @Getter
    private byte windY;
    private long startTime;

    private final ClanService clanService;

    public FightManager(FightWait fightWait, ClanService clanService) {
        this.fightWait = fightWait;
        this.clanService = clanService;
        this.players = new Player[MAX_ELEMENT_FIGHT];
        this.mapManager = new FightMapManager(this);
        this.bulletManager = new BulletManager(this);
        this.countdownTimer = new CountdownTimer(MAX_PLAY_TIME + 10, this::onTimeUp);
        this.playerTurn = -1;
    }

    private void refreshFightManager() {
        players = new Player[MAX_ELEMENT_FIGHT];
        totalPlayers = MAX_USER_FIGHT;
        turnCount = 0;
        isBossTurn = false;
        playerTurn = 0;
        bossTurn = MAX_USER_FIGHT;
        windX = 0;
        windY = 0;
        countdownTimer.stop();
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

    private void sendEyeSmokeUpdate(byte index) {
        try {
            Message ms = new Message(Cmd.EYE_SMOKE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(index);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFreezeUpdate(byte index) {
        try {
            Message ms = new Message(Cmd.FREEZE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(index);
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

    private void sendMssAddBosses(Boss[] bosses) {
        try {
            Message ms = new Message(Cmd.GET_BOSS);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bosses.length);
            for (Boss boss : bosses) {
                ds.writeInt(-1);
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

    private void sendNextTurnMessage(int turn) {
        try {
            Message ms = new Message(Cmd.NEXT_TURN_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(turn);
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

    private void sendFightInfo(Player player) {
        try {
            Message ms = new Message(Cmd.START_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(fightWait.getMapId());
            ds.writeByte(MAX_PLAY_TIME);
            ds.writeShort(player.getTeamPoints());
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
            if (player == null || player.getUser() == null) {
                continue;
            }
            player.nextLuck();
        }
    }

    private void updateLuckyPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null || !player.isLucky()) {
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
            if (player.getFreezeCount() > 0) {
                sendFreezeUpdate(i);
            }
            if (player.getEyeSmokeCount() > 0) {
                sendEyeSmokeUpdate(i);
            }
            if (player.isPoisoned()) {
                sendPoisonUpdate(i);
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

    private synchronized void nextWind() {
        Player player = players[getCurrentTurn()];
        if (player != null && player.getWindStopCount() > 0) {
            player.decreaseWindStopCount();

            windX = 0;
            windY = 0;
        } else {
            if (Utils.nextInt(0, 100) > 25) {
                windX = (byte) Utils.nextInt(-70, 70);
                windY = (byte) Utils.nextInt(-70, 70);
            }
        }

        sendWindUpdate();
    }

    private int getCurrentTurn() {
        if (isBossTurn) {
            return bossTurn;
        }
        return playerTurn;
    }

    private void spawnBosses() {
        byte playerCount = fightWait.getNumPlayers();

        switch (fightWait.getMapId()) {
            case 30 -> {//Bom 1
                byte bossCount = BOSS_COUNTS[0][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short bossX = (short) ((i % 2 == 0) ? Utils.nextInt(95, 315) : Utils.nextInt(890, 1070));
                    short bossY = (short) (50 + 40 * Utils.nextInt(3));
                    short bossHealth = 1000;
                    players[totalPlayers] = new BigBoom(this, (byte) totalPlayers, bossX, bossY, bossHealth);
                    totalPlayers++;
                }
            }

            case 31 -> {//Bom 2
                byte bossCount = BOSS_COUNTS[1][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short bossX = (short) (Utils.nextInt(445, 800) + i * 50);
                    short bossY = 180;
                    short bossHealth = 1500;
                    players[totalPlayers] = new BigBoom(this, (byte) totalPlayers, bossX, bossY, bossHealth);
                    totalPlayers++;
                }
            }

            case 32 -> {//Nhện máy
                short[] tempX = new short[]{505, 1010, 743, 425, 1068};
                short[] tempY = new short[]{221, 221, 198, 369, 369, 369};
                byte bossCount = BOSS_COUNTS[2][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    players[totalPlayers] = new RobotSpider(this, (byte) totalPlayers, tempX[i], tempY[i], (short) 1500);
                    totalPlayers++;
                }
            }

            case 33 -> {//Thành phố máy
                short[] tempX = new short[]{420, 580, 720, 240, 55, 900};
                byte bossCount = BOSS_COUNTS[3][playerCount - 1];
                for (int i = 0; i < bossCount; i++) {
                    short X = tempX[i];
                    short Y = 200;
                    players[totalPlayers] = new Robot(this, (byte) totalPlayers, X, Y, (short) 3700);
                    totalPlayers++;
                }
            }

            case 34 -> {// T. rex máy
                short X = 880;
                short Y = 400;
                players[totalPlayers] = new TRex(this, (byte) totalPlayers, X, Y, (short) 15000);
                totalPlayers++;

                byte bossCount = BOSS_COUNTS[4][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    X = (short) (Utils.nextInt(470, 755));
                    players[totalPlayers] = new BigBoom(this, (byte) totalPlayers, X, Y, (short) 1500);
                    totalPlayers++;
                }
            }

            case 35 -> {//Khu vực cấm
                byte bossCount = BOSS_COUNTS[5][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) (Utils.nextInt(300, 800));
                    short Y = (short) Utils.nextInt(-350, 100);
                    players[totalPlayers] = new UFO(this, (byte) totalPlayers, X, Y, (short) 4500);
                    totalPlayers++;
                }
            }

            case 36 -> {//Đỉnh hi mã lạp sơn
                short X = (short) (Utils.nextInt(300, 800));
                short Y = (short) Utils.nextInt(-350, 100);

                Balloon balloon = new Balloon(this, (byte) totalPlayers, X, Y);
                balloon.getBodyParts()[0] = balloon;
                players[totalPlayers] = balloon;
                totalPlayers++;

                BalloonGun balloonGun = new BalloonGun(this, (byte) totalPlayers, (short) (X + 51), (short) (Y + 19), (short) 2000);
                balloon.getBodyParts()[1] = balloonGun;
                players[totalPlayers] = balloonGun;
                totalPlayers++;

                BalloonGunBig balloonGunBig = new BalloonGunBig(this, (byte) totalPlayers, (short) (X - 5), (short) (Y + 30), (short) 2500);
                balloon.getBodyParts()[2] = balloonGunBig;
                players[totalPlayers] = balloonGunBig;
                totalPlayers++;

                BalloonFanBack balloonFanBack = new BalloonFanBack(this, (byte) totalPlayers, (short) (X - 67), (short) (Y - 6), (short) 1000);
                balloon.getBodyParts()[3] = balloonFanBack;
                players[totalPlayers] = balloonFanBack;
                totalPlayers++;
            }

            case 37 -> {//Nhện độc
                byte bossCount = BOSS_COUNTS[7][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) Utils.nextInt(20, mapManager.getWidth() - 20);
                    short Y = (short) 250;
                    players[totalPlayers] = new VenomousSpider(this, (byte) totalPlayers, X, Y, (short) 3800);
                    totalPlayers++;
                }
            }

            case 38 -> {//Nghĩa trang 1
                byte bossCount = BOSS_COUNTS[8][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) ((short) 700 - i * 80);
                    short Y = (short) (Utils.nextInt(30));
                    players[totalPlayers] = new Ghost(this, (byte) totalPlayers, X, Y, (short) 1800);
                    totalPlayers++;
                }
            }

            case 39 -> {//Nghĩa trang 2
                byte bossCount = BOSS_COUNTS[9][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short X = (short) (700 - i * 80);
                    short Y = (short) Utils.nextInt(30);
                    players[totalPlayers] = new Ghost2(this, (byte) totalPlayers, X, Y, (short) 1800);
                    totalPlayers++;
                }
            }
        }

        //Gửi thông tin thêm các boss đã tạo đến các team
        Boss[] bosses = new Boss[totalPlayers - MAX_USER_FIGHT];
        for (int i = 0; i < bosses.length; i++) {
            bosses[i] = (Boss) players[i + MAX_USER_FIGHT];
        }
        sendMssAddBosses(bosses);
    }

    public short[] getForceArgXY(int idGun, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100) {
        return null;
    }

    public void nextTurn() {
        fightLoop.submit(this::doNextTurn);
    }

    private void doNextTurn() {
        MatchResult result = getMatchResult();
        if (result != null) {
            fightComplete(result);
        }

        turnCount++;

        //Cập nhật vị trí y của các player
        for (Player player : players) {
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
        }

        // Random gió
        nextWind();

        // Gửi thông báo lượt chơi tiếp theo
        sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);

        //Khởi động lại đồng hồ đếm ngược
        countdownTimer.reset();

        // Thực hiện hành động của boss trong lượt
        if (isBossTurn) {
            ((Boss) players[bossTurn]).turnAction();
        }
    }

    private void initFirstTurn() {
        byte roomType = fightWait.getRoomType();
        while (true) {
            int next;
            if (roomType == 5) {
                next = Utils.nextInt(MAX_USER_FIGHT, totalPlayers);
            } else {
                next = Utils.nextInt(MAX_USER_FIGHT);
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

    public void addBoss(Boss boss) {
        if (totalPlayers >= FightManager.MAX_ELEMENT_FIGHT) {
            return;
        }
        players[totalPlayers] = boss;
        totalPlayers++;

        sendMssAddBosses(new Boss[]{boss});
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

    public void leave(int userId) {
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
            nextTurn();
        } else {
            sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);
        }
    }

    /**
     * Kiểm tra kết quả trận đấu
     *
     * @return MatchResult nếu trận đấu đã kết thúc (DRAW/BLUE_WIN/RED_WIN),
     * null nếu trận đấu đang tiếp tục hoặc chưa bắt đầu
     */
    public MatchResult getMatchResult() {
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
                Boss boss = (Boss) this.players[i];
                if (boss != null && !boss.isDead()) {
                    bossAliveCount++;
                }
                i++;
            }
            if (playerAliveCount == 0 || bossAliveCount == 0) {
                if (playerAliveCount == bossAliveCount) {
                    if (isBossTurn) {
                        return MatchResult.RED_WIN;
                    } else {
                        return MatchResult.BLUE_WIN;
                    }
                } else if (playerAliveCount == 0) {
                    return MatchResult.RED_WIN;
                } else {
                    return MatchResult.BLUE_WIN;
                }
            } else {
                return null;
            }
        } else {
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

    private void fightComplete(MatchResult result) {
        //Cập nhật trạng thái người chơi
        updatePlayerStatuses();

        //Cập nhật số xp nhận được
        updateXpPlayers();

        //Cập nhật số cup nhận được
        updateCupPlayers();

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

                        int chance = Utils.nextInt(100);
                        if (chance < 30) {//30% nhận nguyên liệu
                            short quantity = (short) Utils.nextInt(1, 5);
                            byte id = getRewardMaterialId();

                            SpecialItemChest newItem = new SpecialItemChest(quantity, SpecialItemManager.getSpecialItemById(id));
                            user.updateInventory(null, null, List.of(newItem), null);

                            String reward = String.format("Phần thưởng diệt trùm của bạn là %dx %s", newItem.getQuantity(), newItem.getItem().getName());
                            user.sendServerMessage(reward);
                        } else {
                            StringBuilder reward = new StringBuilder("Phần thưởng diệt trùm của bạn là ");
                            int count = Utils.nextInt(2, 3);
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
                    if (user.getClanId() != null) {
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
                fightWait.decreaseContinuousLevel();

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

        new Thread(() -> {
            try {
                Thread.sleep(8000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            fightWait.fightComplete();

            //Cập nhật mở quà
            if (turnCount > 5 && fightWait.getRoomType() != 5) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                boolean isBlueWin = result == MatchResult.BLUE_WIN;
                fightWait.startGiftBoxOpening(isBlueWin);
            }

            refreshFightManager();
        }).start();
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

    public void startGame(short teamPointsBlue, short teamPointsRed) {
        //Tải dữ liệu bản đồ
        mapManager.loadMapId(fightWait.getMapId());

        //Tải dữ liệu vị trí
        List<short[]> randomPositions = mapManager.getRandomPlayerPositions(MAX_USER_FIGHT);

        //Sử dụng cache để lưu trữ kết quả clan items
        Map<Short, boolean[]> clanItemsCache = new HashMap<>();
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
            short[] abilities = user.calculateCharacterAbilities(teamPoints);

            //Lấy danh sách items của clan
            boolean[] clanItems = new boolean[ClanItemManager.CLAN_ITEM_MAP.size()];
            if (user.getClanId() != null) {
                if (clanItemsCache.containsKey(user.getClanId())) {
                    clanItems = clanItemsCache.get(user.getClanId());
                } else {
                    clanItems = clanService.getClanItems(user.getClanId());
                    clanItemsCache.put(user.getClanId(), clanItems);
                }
            }

            //Xóa túi đựng item nếu sử dụng
            byte[] items = fightWait.getItems(i);
            for (int j = 4; j < items.length; j++) {
                if (items[i] > 0) {
                    user.updateFightItems((byte) (12 + j - 4), (byte) -1);
                }
            }

            //Trừ xu cược
            user.updateXu(-fightWait.getMoney());

            //Cập nhật trạng thái người chơi
            user.setState(UserState.FIGHTING);

            players[i] = new Player(this, user, i, isTeamBlue, x, y, items, abilities, teamPoints, clanItems);
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
            sendFightInfo(player);
        }

        // Tạo boss nếu là chế độ đấu trùm
        if (fightWait.getRoomType() == 5) {
            spawnBosses();
        }

        // Bắt đầu lượt chơi đầu tiên
        nextTurn();
    }

    public synchronized void addShoot(int userId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
        int index = getPlayerIndexByUserId(userId);
        if (index == -1 || index != playerTurn || isBossTurn || !fightWait.isStarted()) {
            return;
        }
        Player player = players[index];
        player.updateXY(x, y);

        newShoot(index, bullId, angle, force, force2, numShoot);
    }

    public void newShoot(int index, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        newShoot(index, bullId, angle, force, force2, numShoot, true);
    }

    public void newShoot(int index, byte bullId, short angle, byte force, byte force2, byte numShoot, boolean isNextTurn) {
        Player player = players[index];
        if (player.isDoubleShoot()) {
            player.setDoubleShoot(false);
        } else {
            numShoot = 1;
        }

        //Tính toán người chơi nào rơi sao
        handleLuckUpdates();

        bulletManager.addShoot(player, bullId, angle, force, force2, numShoot);
        bulletManager.updateBullets();

        //Gửi ms những người chơi may mắn
        updateLuckyPlayers();

        List<Bullet> bullets = bulletManager.getBullets();
        byte typeShoot = bulletManager.getTypeShoot();
        try {
            Message ms = new Message(Cmd.FIRE_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeShoot);
            ds.writeByte(player.isUsePow() ? 1 : 0);
            ds.writeByte(index);
            ds.writeByte(bullId);
            ds.writeShort(player.getX());
            ds.writeShort(player.getY());
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

        //Xóa các đạn đã bắn
        bulletManager.resetBullets();

        //Chuyển lượt mới
        if (isNextTurn) {
            nextTurn();
        }
    }

    public void changeLocation(int userId, short x, short y) {
        int index = getPlayerIndexByUserId(userId);
        if (index == -1) {
            return;
        }

        Player player = players[index];

        //Lưu lại vị trí ban đầu
        int preX = player.getX();
        int preY = player.getY();

        //Cập nhật vị trí mới
        player.updateXY(x, y);

        //Gửi thông báo nếu vị trí thay đổi
        if (preX != player.getX() || preY != player.getY()) {
            sendMessageUpdateXY(index);
        }
    }

    public synchronized void skipTurn(int userId) {
        int index = getPlayerIndexByUserId(userId);
        if (index == -1 || index != playerTurn || isBossTurn) {
            return;
        }
        Player player = players[playerTurn];
        if (player.getSkippedTurns() < 5) {
            nextTurn();
            player.incrementSkippedTurns();
        }
    }

    public synchronized void useItem(int userId, byte itemIndex) {
        int index = getPlayerIndexByUserId(userId);
        if (index == -1 || index != playerTurn) {
            return;
        }

        Player player = players[index];
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

        sendUseItemMessage(itemIndex, index);

        //Xử lý khi dùng item
        handleItem(player, index, itemIndex);
    }

    private void handleItem(Player player, int playerIndex, byte itemIndex) {
        switch (itemIndex) {
            //Hồi máu
            case 0 -> {
                player.updateHP((short) 350);
                sendHpUpdate((byte) playerIndex);
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
                nextWind();
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
            case 24 -> newShoot(playerIndex, (byte) 50, (short) 0, (byte) 0, (byte) 0, (byte) 1);

            //Ufo Todo
            case 27 -> {
                System.out.println("tobe continue...");
            }

            //Hồi máu 50%
            case 32 -> {
                short hpUp = (short) (player.getMaxHp() / 2);
                player.updateHP(hpUp);
                sendHpUpdate((byte) playerIndex);
            }

            //Hồi máu 100%
            case 33 -> {
                player.updateHP(player.getMaxHp());
                sendHpUpdate((byte) playerIndex);
            }

            //Vô hình
            case 34 -> player.setInvisibleCount((byte) 10);

            //Hút máu
            case 35 -> player.setVampireCount((byte) 2);
        }
    }

    public FightMapManager getMapManger() {
        return mapManager;
    }

    public void onTimeUp() {
        nextTurn();
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

        return validPlayers.get(Utils.nextInt(validPlayers.size()));
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

    public void updateCantMove(Player pl) {
        pl.setFreezeCount((byte) 5);
    }

    public void updateCantSee(Player pl) {
    }

    public void collisionPlayers(short x, short y, Bullet bullet) {
        for (int i = 0; i < totalPlayers; i++) {
            Player pl = players[i];
            if (pl != null && pl.getCharacterId() != 17) {
                pl.collision(x, y, bullet);
            }
        }
    }
}
