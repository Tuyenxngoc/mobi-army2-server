package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.fight.boss.BigBoom;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.repository.ClanItemRepository;
import com.teamobi.mobiarmy2.server.ClanManager;
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
    private int currentTurnPlayer;
    private int totalPlayers;
    private byte windX;
    private byte windY;
    private long startTime;
    private final IMapManager mapManager;
    private final IBulletManager bulletManager;
    private final ICountdownTimer countdownTimer;

    public FightManager(FightWait fightWait) {
        this.fightWait = fightWait;
        this.players = new Player[MAX_ELEMENT_FIGHT];
        this.mapManager = new MapManager(this);
        this.bulletManager = new BulletManager(this);
        this.countdownTimer = new CountdownTimer(this);
    }

    private void refreshFightManager() {
        this.players = new Player[MAX_ELEMENT_FIGHT];
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

    private void sendHpUpdate(byte index, Player player) {
        try {
            IMessage ms = new Message(Cmd.UPDATE_HP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(index);
            ds.writeShort(player.getHp());
            ds.writeByte(player.getPixel());//todo
            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendAngryUpdate(byte index, Player player) {
        try {
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

    private void handlePlayerLuck() {
        for (int i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].getUser() != null) {
                players[i].nextLuck();
            }
        }
    }

    private void updateLuckyPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].isLucky()) {
                sendLuckyUpdate(i);
                players[i].setLucky(false);
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
                sendHpUpdate(i, players[i]);
            }
        }
    }

    private void updateAngryPlayers() {
        for (byte i = 0; i < MAX_USER_FIGHT; i++) {
            if (players[i] != null && players[i].isUpdateAngry()) {
                sendAngryUpdate(i, players[i]);
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
        Player player = players[currentTurnPlayer];
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
            case 31 -> System.out.println(11);
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

    private void nextTurn() {

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
        if (currentTurnPlayer == index) {
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
    public void addShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
    }

    @Override
    public void changeLocation(User user, short x, short y) {
    }

    @Override
    public void skipTurn(User user) {

    }

    @Override
    public void useItem(byte itemIndex) {

    }

    @Override
    public IMapManager getMapManger() {
        return mapManager;
    }

}
