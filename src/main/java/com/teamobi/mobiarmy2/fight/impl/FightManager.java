package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.fight.boss.BigBoom;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.repository.ClanItemRepository;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.util.MapTileExporter;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class FightManager implements IFightManager {

    private static final int MAX_ELEMENT_FIGHT = 100;
    private static final int MAX_USER_FIGHT = 8;
    private static final int MAX_PLAY_TIME = 30;
    private static final byte[][] BOSS_COUNTS = {
            {4, 6, 6, 8, 8, 8, 10, 10},
            {2, 4, 5, 6, 6, 7, 8, 8}
    };

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
    private final BulletManager bulletManager;
    private final ICountdownTimer countdownTimer;

    public FightManager(FightWait fightWait) {
        this.fightWait = fightWait;
        this.players = new Player[MAX_ELEMENT_FIGHT];
        this.mapManager = new MapManager(this);
        this.bulletManager = new BulletManager(this);
        this.countdownTimer = new CountdownTimer(MAX_PLAY_TIME + 10, this::onTimeUp);

        this.playerTurn = -1;
    }

    private void refreshFightManager() {
        players = new Player[MAX_ELEMENT_FIGHT];
        countdownTimer.stop();
    }

    private void sendLuckyUpdate(byte index) {
        try {
            IMessage ms = new Message(Cmd.LUCKY);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMoneyUpdate(Player player) {
        try {
            User user = player.getUser();
            IMessage ms = new Message(Cmd.BONUS_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
            ds.writeInt(-fightWait.getMoney());
            ds.writeInt(user.getXu());
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleLuckUpdates() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].getUser() != null) {
                players[i].nextLuck();

                if (players[i].isLucky()) {
                    sendLuckyUpdate(i);
                    players[i].setLucky(false);
                }
            }
        }
    }

    private void updatePoisonedPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].isPoisoned()) {
                sendPoisonUpdate(i);
            }
        }
    }

    private void updateEyeSmokeStatus() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].getEyeSmokeCount() > 0) {
                sendEyeSmokeUpdate(i);
            }
        }
    }

    private void updateFrozenPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].getFreezeCount() > 0) {
                sendFreezeUpdate(i);
            }
        }
    }

    private void updateHpPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].isUpdateHP()) {
                sendHpUpdate(i);
            }
        }
    }

    private void updateAngryPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].isUpdateAngry()) {
                sendAngryUpdate(i);
            }
        }
    }

    private void updateMoneyPlayers() {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].getUser() != null) {
                sendMoneyUpdate(players[i]);
            }
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

    private void nextWind() {
        Player player = players[getCurrentTurn()];
        if (player.getWindStopCount() > 0) {
            player.decreaseWindStopCount();

            windX = 0;
            windY = 0;
        } else {
            if (Utils.nextInt(0, 100) > 25) {
                windX = Utils.nextByte(-70, 70);
                windY = Utils.nextByte(-70, 70);
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
        switch (fightWait.getMapId()) {
            case 30 -> {
                byte playerCount = fightWait.getNumPlayers();
                byte bossCount = BOSS_COUNTS[0][playerCount - 1];
                for (byte i = 0; i < bossCount; i++) {
                    short bossX = (short) ((i % 2 == 0) ? Utils.nextInt(95, 315) : Utils.nextInt(890, 1070));
                    short bossY = (short) (50 + 40 * Utils.nextInt(3));
                    short bossHealth = 1000;
                    players[totalPlayers] = new BigBoom(this, (byte) totalPlayers, bossX, bossY, bossHealth);
                    totalPlayers++;
                }
            }
        }

        try {
            int bossCount = totalPlayers - MAX_USER_FIGHT;
            IMessage ms = new Message(Cmd.GET_BOSS);
            DataOutputStream ds = ms.writer();
            ds.writeByte(bossCount);
            for (byte i = 0; i < bossCount; i++) {
                Boss boss = (Boss) players[i + MAX_USER_FIGHT];
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

    @Override
    public void nextTurn() {
        turnCount++;
        byte roomType = fightWait.getRoomType();

        //Lần đầu radom lượt chơi
        if (playerTurn == -1) {
            while (true) {
                int next;
                if (roomType == 5) {
                    next = Utils.nextInt(totalPlayers);
                } else {
                    next = Utils.nextInt(MAX_USER_FIGHT);
                }
                if (players[next] != null) {
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

        //Đặt lại giá trị của người chơi trong lượt mới như thể lưc, ..., vv
        if (isBossTurn) {
            players[bossTurn].resetValueInNewTurn();
        } else {
            players[playerTurn].resetValueInNewTurn();
        }

        nextWind();
        sendNextTurnMessage(isBossTurn ? bossTurn : playerTurn);
        countdownTimer.reset();
        if (isBossTurn) {
            new Thread(() -> ((Boss) players[bossTurn]).turnAction()).start();
        }

        //Test
        try {
            MapTileExporter.saveMapTilesToFile(
                    mapManager.getMapTiles(),
                    mapManager.getWidth(),
                    mapManager.getHeight(),
                    players,
                    String.format("temp/turn_%d.png", turnCount));
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
            if (players[turn] != null) {
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

        Player player = players[index];
        player.die();
        player.getUser().updateCup(-5);

        players[index] = null;

        fightWait.chatMessage(playerId, GameString.leave2(player.getUser().getUsername()));

        //đổi lượt chơi
        if (playerTurn == index) {
            nextTurn();
        }
    }

    @Override
    public void startGame(short teamPointsBlue, short teamPointsRed) {
        if (fightWait.isStarted()) {
            return;
        }

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
            if (fightWait.getRoomType() == 5 || i % 2 == 0) {
                teamPoints = teamPointsBlue;
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

            players[i] = new Player(this, user, i, x, y, items, abilities, teamPoints, clanItems);
        }

        //Cập nhật trang thái game
        startTime = System.currentTimeMillis();
        totalPlayers = MAX_USER_FIGHT;

        if (fightWait.getMoney() > 0) {
            updateMoneyPlayers();
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
                fightWait.sendToTeam(ms);
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

        newShoot(index, bullId, angle, force, force2, numShoot);
    }

    private void newShoot(int index, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        Player player = players[index];
        if (player.isDoubleShoot()) {
            player.setDoubleShoot(false);
        } else {
            numShoot = 1;
        }

        //Next lucky
        handleLuckUpdates();

        bulletManager.addShoot(player, bullId, angle, force, force2, numShoot);
        bulletManager.fillXY();

        List<Bullet> bullets = bulletManager.getBullets();
        if (bullets.isEmpty()) {
            return;
        }

        byte typeShoot = 0;
        try {
            IMessage ms = new Message(Cmd.FIRE_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeShoot);
            ds.writeByte(player.getPowerUsageStatus());
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
                            ds.writeShort(xArrays.get(0));
                            ds.writeShort(yArrays.get(0));
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

        bulletManager.reset();
        nextTurn();
    }

    @Override
    public void changeLocation(int playerId, short x, short y) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }

        Player player = players[index];
        player.updateXY(x, y);

        try {
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
    public void skipTurn(int playerId) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1 || index != playerTurn) {
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
        if (player.isItemUsed()) {
            return;
        }

        if (fightWait.getRoomType() == 5 && (itemIndex == 9)) {
            player.getUser().getUserService().sendServerMessage2(GameString.unauthorizedItem());
            return;
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

        player.setItemUsed(true);
        handleItem(player, index, itemIndex);
    }

    private void handleItem(Player player, int playerIndex, byte itemIndex) {
        switch (itemIndex) {
            case 0 -> {
                player.updateHP((short) 350);
                sendHpUpdate((byte) playerIndex);
            }

            case 2 -> player.setDoubleShoot(true);

            case 3 -> player.setDoubleSpeed(true);

            //todo
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
}
