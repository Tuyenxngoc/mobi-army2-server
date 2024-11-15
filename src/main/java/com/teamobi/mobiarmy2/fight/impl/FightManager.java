package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.MatchResult;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.fight.boss.*;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.repository.ClanItemRepository;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * @author tuyen
 */
public class FightManager implements IFightManager {

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
    private static final Set<Byte> invalidCharacterIds = new HashSet<>(Set.of((byte) 18, (byte) 19, (byte) 20, (byte) 21, (byte) 23, (byte) 24));

    private final IFightWait fightWait;
    private Player[] players;
    private int totalPlayers;
    private int turnCount;
    private boolean isBossTurn;
    private int playerTurn;
    private int bossTurn;
    private byte windX;
    private byte windY;
    private long startTime;
    private final IMapManager mapManager;
    private final IBulletManager bulletManager;
    private final ICountdownTimer countdownTimer;
    private final ExecutorService executorNextTurn;

    public FightManager(IFightWait fightWait) {
        this.fightWait = fightWait;
        this.players = new Player[MAX_ELEMENT_FIGHT];
        this.mapManager = new MapManager(this);
        this.bulletManager = new BulletManager(this);
        this.countdownTimer = new CountdownTimer(MAX_PLAY_TIME + 10, this::onTimeUp);
        this.executorNextTurn = Executors.newSingleThreadExecutor();
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
            Player player = players[index];
            IMessage ms = new Message(Cmd.LUCKY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.flush();
            fightWait.sendToTeam(ms);
            player.setLucky(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendPoisonUpdate(byte index) {
        try {
            IMessage ms = new Message(Cmd.POISON);
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
            IMessage ms = new Message(Cmd.EYE_SMOKE);
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
            IMessage ms = new Message(Cmd.FREEZE);
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
            IMessage ms = new Message(Cmd.UPDATE_HP);
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
            IMessage ms = new Message(Cmd.ANGRY);
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
            IMessage ms = new Message(Cmd.BONUS_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
            ds.writeInt(money);
            ds.writeInt(user.getXu());
            ds.flush();
            fightWait.sendToTeam(ms);
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

    private int getPlayerIndexByPlayerId(int playerId) {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null
                    && players[i].getUser() != null
                    && players[i].getUser().getPlayerId() == playerId) {
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

        try {
            IMessage ms = new Message(Cmd.WIND);
            DataOutputStream ds = ms.writer();
            ds.writeByte(windX);
            ds.writeByte(windY);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentTurn() {
        if (isBossTurn) {
            return bossTurn;
        }
        return playerTurn;
    }

    private void nextBosses() {
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

    @Override
    public short[] getForceArgXY(int idGun, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100) {
        byte i = (byte) (Utils.nextInt(2) == 0 ? -1 : 1);
        short argS = (short) (i == 1 ? arg : 180 - arg);
        byte forceS = (byte) force;
        do {
            short x, y, vx, vy;
            x = (short) (X + (20 * Utils.cos(argS) >> 10));
            y = (short) (Y - 12 - (20 * Utils.sin(argS) >> 10));
            vx = (short) (forceS * Utils.cos(argS) >> 10);
            vy = (short) -(forceS * Utils.sin(argS) >> 10);
            short ax100 = (short) (windX * msg / 100);
            short ay100 = (short) (windY * msg / 100);
            short vxTemp = 0, vyTemp = 0, vyTemp2 = 0;

            if (idGun == 13) {
                y -= 25;
            }
            while (true) {
                if ((x < -200) || (x > mapManager.getWidth() + 200) || (y > mapManager.getHeight() + 200)) {
                    break;
                }
                short preX = x, preY = y;
                x += vx;
                y += vy;
                byte collision = getCollisionPoint(preX, preY, x, y, toX, toY, Mx, My, isXuyenMap);
                if (collision == 1) {
                    return new short[]{argS, forceS};
                } else if (collision == 2) {
                    break;
                }
                vxTemp += Math.abs(ax100);
                vyTemp += Math.abs(ay100);
                vyTemp2 += g100;
                if (Math.abs(vxTemp) >= 100) {
                    if (ax100 > 0) {
                        vx += vxTemp / 100;
                    } else {
                        vx -= vxTemp / 100;
                    }
                    vxTemp %= 100;
                }
                if (Math.abs(vyTemp) >= 100) {
                    if (ay100 > 0) {
                        vy += vyTemp / 100;
                    } else {
                        vy -= vyTemp / 100;
                    }
                    vyTemp %= 100;
                }
                if (Math.abs(vyTemp2) >= 100) {
                    vy += vyTemp2 / 100;
                    vyTemp2 %= 100;
                }
            }
            forceS++;
            if (forceS > 30) {
                argS += i;
                forceS = (byte) force;
                argS = (short) Utils.toArg0_360(argS);
                if (argS == arg) {
                    break;
                }
            }
        } while (true);

        return null;
    }

    private byte getCollisionPoint(short X1, short Y1, short X2, short Y2, short toX, short toY, short Mx, short My, boolean isXuyenMap) {
        int Dx = X2 - X1;
        int Dy = Y2 - Y1;
        byte x_unit = 0;
        byte y_unit = 0;
        byte x_unit2 = 0;
        byte y_unit2 = 0;
        if (Dx < 0) {
            x_unit = x_unit2 = -1;
        } else if (Dx > 0) {
            x_unit = x_unit2 = 1;
        }
        if (Dy < 0) {
            y_unit = y_unit2 = -1;
        } else if (Dy > 0) {
            y_unit = y_unit2 = 1;
        }
        int k1 = Math.abs(Dx);
        int k2 = Math.abs(Dy);
        if (k1 > k2) {
            y_unit2 = 0;
        } else {
            k1 = Math.abs(Dy);
            k2 = Math.abs(Dx);
            x_unit2 = 0;
        }
        int k = k1 >> 1;
        short X = X1, Y = Y1;
        for (int i = 0; i <= k1; i++) {
            if (Math.abs(X - toX) <= Mx && Math.abs(Y - toY) <= My) {
                return 1;
            }
            if (!isXuyenMap) {
                if (mapManager.isCollision(X, Y)) {
                    return 2;
                }
            }
            k += k2;
            if (k >= k1) {
                k -= k1;
                X += x_unit;
                Y += y_unit;
            } else {
                X += x_unit2;
                Y += y_unit2;
            }
        }
        return 0;
    }

    @Override
    public synchronized void nextTurn() {
        turnCount++;
        byte roomType = fightWait.getRoomType();

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

        //Lần đầu radom lượt chơi
        if (playerTurn == -1) {
            while (true) {
                int next;
                if (roomType == 5) {
                    next = Utils.nextInt(MAX_USER_FIGHT, totalPlayers);
                } else {
                    next = Utils.nextInt(MAX_USER_FIGHT);
                }
                if (players[next] != null && !invalidCharacterIds.contains(players[next].getCharacterId())) {
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
        } else {
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

        //Đặt lại giá trị của người chơi trong lượt mới như thể lực, ..., vv
        if (isBossTurn) {
            Boss boss = (Boss) players[bossTurn];
            boss.resetValueInNewTurn();
        } else {
            Player player = players[playerTurn];
            player.resetValueInNewTurn();
            player.updateAngry((byte) 10);
        }

        List<Boss> addBosses = bulletManager.getAddBosses();
        if (!addBosses.isEmpty()) {
            for (Boss bos : addBosses) {
                addBoss(bos);
            }
            addBosses.clear();
        }

        executorNextTurn.submit(() -> {
            if (turnCount > 1) {
                try {
                    Thread.sleep(2000L);
                } catch (InterruptedException ignored) {

                }
            }
            nextWind();
            sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);
            countdownTimer.reset();

            if (isBossTurn) {
                ((Boss) players[bossTurn]).turnAction();
            }
        });
    }

    @Override
    public void addBoss(Boss boss) {
        if (totalPlayers >= FightManager.MAX_ELEMENT_FIGHT) {
            return;
        }
        players[totalPlayers] = boss;
        totalPlayers++;

        sendMssAddBosses(new Boss[]{boss});
    }

    private void sendMssAddBosses(Boss[] bosses) {
        try {
            IMessage ms = new Message(Cmd.GET_BOSS);
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

    private int getNextValidTurn(int currentTurn, int min, int limit) {
        int turn = currentTurn + 1;
        while (turn != currentTurn) {
            if (turn == limit) {
                turn = min;
            }
            Player player = players[turn];
            if (player != null && !player.isDead() && !invalidCharacterIds.contains(player.getCharacterId())) {
                return turn;
            }
            turn++;
        }
        return currentTurn;
    }

    private void sendNextTurnMessage(int turn) {
        try {
            IMessage ms = new Message(Cmd.NEXT_TURN_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(turn);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void leave(int playerId) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }

        //Cập nhật thông tin người chơi
        Player player = players[index];
        player.die();
        player.getUser().updateCup(-5);
        player.setUser(null);

        //Gửi thông báo đến ván chơi
        fightWait.chatMessage(playerId, GameString.ESCAPED_GAME);

        //Kiểm tra chưa kết thúc ván thì chuyển lượt
        if (!checkWin()) {
            if (index == getCurrentTurn()) {
                nextTurn();
            } else {
                sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);
            }
        }
    }

    @Override
    public boolean checkWin() {
        if (!fightWait.isStarted()) {
            return true;
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
                        fightComplete(MatchResult.RED_WIN);
                    } else {
                        fightComplete(MatchResult.BLUE_WIN);
                    }
                } else if (playerAliveCount == 0) {
                    fightComplete(MatchResult.RED_WIN);
                } else {
                    fightComplete(MatchResult.BLUE_WIN);
                }
            } else {
                return false;
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
                    fightComplete(MatchResult.DRAW);
                } else if (redAliveCount == 0) {
                    fightComplete(MatchResult.BLUE_WIN);
                } else {
                    fightComplete(MatchResult.RED_WIN);
                }
            } else {
                return false;
            }
        }
        return true;
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
                player.getUser().getUserService().sendServerMessage2(GameString.MATCH_NOT_COUNTED);
            }
        }

        ClanManager clanManager = ClanManager.getInstance();
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
                IMessage ms = new Message(Cmd.STOP_GAME);
                DataOutputStream ds = ms.writer();
                ds.writeByte(winStatus);
                ds.writeByte(0);
                if (winStatus == 1 || winStatus == 0) {
                    ds.writeInt(fightWait.getMoney());
                } else {
                    ds.writeInt(-fightWait.getMoney());
                }
                ds.flush();
                user.sendMessage(ms);

                //Gửi thông báo số xp và cup nhận được
                user.getUserService().sendUpdateXp(player.getAllXpUp(), false);
                user.getUserService().sendUpdateCup(Math.min(player.getAllCupUp(), Byte.MAX_VALUE));

                //Cộng thêm quà nếu trận đấu là hợp lệ
                if (!fightInValid) {
                    //Nếu chiến thắng trong đấu boss thì cộng thêm 10xp
                    if (winStatus == 1 && fightWait.getRoomType() == 5) {
                        user.updateXp(10, true);
                    }

                    //Cộng xp và cup cho clan
                    if (user.getClanId() != null) {
                        clanManager.updateXp(user.getClanId(), user.getPlayerId(), player.getAllXpUp() / 100);
                        clanManager.updateCup(user.getClanId(), user.getPlayerId(), player.getAllCupUp());
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

        refreshFightManager();
        fightWait.fightComplete();
    }

    @Override
    public void startGame(short teamPointsBlue, short teamPointsRed) {
        //Tải dữ liệu bản đồ
        mapManager.loadMapId(fightWait.getMapId());

        //Tải dữ liệu vị trí
        List<short[]> randomPositions = mapManager.getRandomPlayerPositions(MAX_USER_FIGHT);

        //Sử dụng cache để lưu trữ kết quả clan items
        Map<Short, boolean[]> clanItemsCache = new HashMap<>();
        ClanManager clanManager = ClanManager.getInstance();

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
            boolean[] clanItems = new boolean[ClanItemRepository.CLAN_ITEM_ENTRY_MAP.size()];
            if (user.getClanId() != null) {
                if (clanItemsCache.containsKey(user.getClanId())) {
                    clanItems = clanItemsCache.get(user.getClanId());
                } else {
                    clanItems = clanManager.getClanItems(user.getClanId());
                    clanItemsCache.put(user.getClanId(), clanItems);
                }
            }

            //Xóa túi đựng item nếu sử dụng
            byte[] items = fightWait.getItems(i);
            for (int j = 4; j < items.length; j++) {
                if (items[i] > 0) {
                    user.updateItems((byte) (12 + j - 4), (byte) -1);
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
        sendFightInfo();
        if (fightWait.getRoomType() == 5) {
            nextBosses();
        }
        nextTurn();
    }

    private void sendFightInfo() {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null) {
                continue;
            }

            try {
                IMessage ms = new Message(Cmd.START_ARMY);
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
    }

    @Override
    public synchronized void addShoot(int playerId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1 || index != playerTurn || isBossTurn || !fightWait.isStarted()) {
            return;
        }
        Player player = players[index];
        player.updateXY(x, y);

        newShoot(index, bullId, angle, force, force2, numShoot, true);
    }

    @Override
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
        bulletManager.fillXY();

        //Gửi ms những người chơi may mắn
        updateLuckyPlayers();

        List<Bullet> bullets = bulletManager.getBullets();
        if (bullets.isEmpty()) {
            return;
        }

        byte typeShoot = 0;
        try {
            IMessage ms = new Message(Cmd.FIRE_ARMY);
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
                ds.writeByte(0);
                ds.writeByte(0);
            }
            if (bullId == 44 || bullId == 45 || bullId == 47) {
                ds.writeByte(0);
            }
            ds.writeByte(numShoot);
            ds.writeByte(bullets.size());
            for (Bullet bullet : bullets) {
                List<Short> xArrays = bullet.getXArray();
                List<Short> yArrays = bullet.getYArray();
                ds.writeShort(xArrays.size());
                if (typeShoot == 0) {
                    for (int j = 0; j < xArrays.size(); j++) {
                        if (j == 0) {
                            ds.writeShort(xArrays.getFirst());
                            ds.writeShort(yArrays.getFirst());
                        } else {
                            if ((j == xArrays.size() - 1) && bullId == 49) {
                                ds.writeShort(xArrays.get(j));
                                ds.writeShort(yArrays.get(j));
                                ds.writeByte(bulletManager.getMgtAddX());
                                ds.writeByte(bulletManager.getMgtAddY());
                                break;
                            }
                            ds.writeByte((byte) (xArrays.get(j) - xArrays.get(j - 1)));
                            ds.writeByte((byte) (yArrays.get(j) - yArrays.get(j - 1)));
                        }
                    }
                } else if (typeShoot == 1) {
                    for (int j = 0; j < xArrays.size(); j++) {
                        ds.writeShort(xArrays.get(j));
                        ds.writeShort(yArrays.get(j));
                    }
                }
                if (bullId == 48) {
                    ds.writeByte(1);
                    for (int j = 0; j < 1; j++) {
                        ds.writeShort(0);
                        ds.writeShort(0);
                    }
                }
            }

            byte bulletSuperState = bulletManager.getTypeSC();
            ds.writeByte(bulletSuperState);
            if (bulletSuperState == 1 || bulletSuperState == 2) {
                ds.writeShort(bulletManager.getXSC());
                ds.writeShort(bulletManager.getYSC());
            }
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Xóa các đạn đã bắn
        bulletManager.getBullets().clear();

        //Nếu chưa kết thúc trận đấu thì tìm lượt mới
        if (isNextTurn && !checkWin()) {
            nextTurn();
        }
    }

    @Override
    public void changeLocation(int playerId, short x, short y) {
        int index = getPlayerIndexByPlayerId(playerId);
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

    @Override
    public void sendMessageUpdateXY(int index) {
        try {
            Player player = players[index];
            IMessage ms = new Message(Cmd.MOVE_ARMY);
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

    @Override
    public synchronized void skipTurn(int playerId) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1 || index != playerTurn || isBossTurn) {
            return;
        }
        Player player = players[playerTurn];
        if (player.getSkippedTurns() < 5) {
            nextTurn();
            player.incrementSkippedTurns();
        }
    }

    @Override
    public synchronized void useItem(int playerId, byte itemIndex) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1 || index != playerTurn) {
            return;
        }

        Player player = players[index];
        if (player.isItemUsed() || player.isUsePow()) {
            return;
        }

        //Khi đấu boss thì cấm dùng 1 số item
        if (fightWait.getRoomType() == 5 && (itemIndex == 9 || itemIndex == 23 || itemIndex == 26 || itemIndex == 28 || itemIndex == 30 || itemIndex == 31)) {
            player.getUser().getUserService().sendServerMessage2(GameString.unauthorizedItem);
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
            player.getUser().updateItems(itemIndex, (byte) -1);
        }

        try {
            IMessage ms = new Message(Cmd.USE_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeByte(itemIndex);
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            case 24 -> newShoot(playerIndex, (byte) 50, (short) 0, (byte) 0, (byte) 0, (byte) 1, true);

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

    @Override
    public IMapManager getMapManger() {
        return mapManager;
    }

    @Override
    public void onTimeUp() {
        nextTurn();
    }

    @Override
    public int getTotalPlayers() {
        return totalPlayers;
    }

    @Override
    public int getTurnCount() {
        return turnCount;
    }

    @Override
    public byte getWindY() {
        return windY;
    }

    @Override
    public byte getWindX() {
        return windX;
    }

    @Override
    public Player[] getPlayers() {
        return players;
    }

    @Override
    public Player getPlayerTurn() {
        return players[getCurrentTurn()];
    }

    @Override
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

    @Override
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

    @Override
    public void updateCantMove(Player pl) {
        pl.setFreezeCount((byte) 5);
    }

    @Override
    public void updateCantSee(Player pl) {

    }

    @Override
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

    @Override
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

    @Override
    public void capture(byte index, byte toIndex) {
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

    @Override
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

    @Override
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

    public void sendRewardMessage(Player player, Reward reward) {
        try {
            Message ms = new Message(Cmd.GIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);//null byte
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
                    EquipmentEntry equip = reward.getEquip().getEquipEntry();
                    ds.writeByte(equip.getCharacterId());
                    ds.writeByte(equip.getEquipType());
                    ds.writeShort(equip.getEquipIndex());
                }

                //xp
                case 3 -> ds.writeByte(reward.getXp());

                //notification
                case 4 -> {
                    SpecialItemChestEntry specialItem = reward.getSpecialItem();
                    ds.writeUTF(specialItem.getItem().getName());
                }
            }
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
