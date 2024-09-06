package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author tuyen
 */
public class FightManager implements IFightManager {

    private static final int MAX_ELEMENT_FIGHT = 100;

    private final IFightWait fightWait;
    private Player[] players;
    private int currentTurnPlayer;
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

    private int getPlayerIndexByPlayerId(int playerId) {
        for (int i = 0; i < fightWait.getNumPlayers(); i++) {
            if (players[i] != null
                    && players[i].getUser() != null
                    && players[i].getUser().getPlayerId() == playerId) {
                return i;
            }
        }
        return -1;
    }

    private void nextBosses() {

    }

    private void nextTurn() {

    }

    @Override
    public void leave(int playerId) {//Todo
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
    public void startGame() {
        if (fightWait.isStarted()) {
            return;
        }

        //Tải dữ liệu bản đồ
        mapManager.loadMapId(fightWait.getMapId());

        for (int i = 0; i < fightWait.getNumPlayers(); i++) {
            User user = fightWait.getUsers()[i];
            if (user == null) {
                continue;
            }
            players[i] = new Player(user);
        }

        sendFightInfo();
    }

    private void sendFightInfo() {
        try {
            IMessage ms = new Message(Cmd.START_ARMY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);

            //Time counter
            ds.writeByte(30);

            //Team point
            ds.writeShort(0);

            for (Player player : players) {
                if (player == null) {
                    ds.writeShort(-1);
                    continue;
                }
                ds.writeShort(player.getX());
                ds.writeShort(player.getY());
                ds.writeShort(player.getMaxHp());
            }

            ds.flush();
            fightWait.sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
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
