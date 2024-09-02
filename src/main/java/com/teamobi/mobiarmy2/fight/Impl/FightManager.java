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
    private Player[] players;

    public FightManager(FightWait fightWait) {
        this.fightWait = fightWait;
        this.players = new Player[MAX_ELEMENT_FIGHT];
    }

    private void refreshFightManager() {
        this.players = new Player[MAX_ELEMENT_FIGHT];
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
            fightWait.sendToTeam(ms);
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

}
