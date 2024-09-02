package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.IFightWait;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author tuyen
 */
public class FightManager implements IFightManager {

    private static final byte MAX_ELEMENT_FIGHT = 100;

    private final IFightWait fightWait;
    private final boolean isTraining;
    private final User trainingUser;

    private Player[] players;

    public FightManager(User trainingUser) {
        this.fightWait = null;
        this.isTraining = true;
        this.trainingUser = trainingUser;
    }

    public FightManager(FightWait fightWait) {
        this.fightWait = fightWait;
        this.isTraining = false;
        this.trainingUser = null;
        this.players = new Player[MAX_ELEMENT_FIGHT];
    }

    private void refreshFightManager() {

    }

    private void sendToTeam(IMessage message) {
        if (isTraining && trainingUser != null) {
            trainingUser.sendMessage(message);
            return;
        }
        if (fightWait != null) {
            fightWait.sendToTeam(message);
        }
    }

    private int getPlayerIndexByPlayerId(int playerId) {
        for (int i = 0; i < players.length; i++) {
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
    public void leave(int playerId) {

    }

    @Override
    public void chatMessage(int playerId, String message) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }
        try {
            IMessage ms = new Message(Cmd.CHAT_TO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeUTF(message);
            ds.flush();
            sendToTeam(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void startGame() {
        if (fightWait.isStarted()) {
            return;
        }

        for (byte i = 0; i < fightWait.getNumPlayers(); i++) {
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
            if (isTraining) {
                for (short data : trainingUser.getEquip()) {
                    ds.writeShort(data);
                }
            }

            ds.writeByte(0);
            //Time counter
            if (isTraining) {
                ds.writeByte(0);
            } else {
                ds.writeByte(30);
            }

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
            sendToTeam(ms);
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
    public void startTraining() {

    }

    @Override
    public void stopTraining() {

    }
}
