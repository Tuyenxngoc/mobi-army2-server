package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;

/**
 * @author tuyen
 */
public class FightManager {

    private static final byte MAX_ELEMENT_FIGHT = 100;

    private final FightWait fightWait;
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

    private void sendToTeam(Message message) {
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

    public void leave(int playerId) {

    }

    public void chatMessage(int playerId, String message) {

    }

    public void startGame() {

    }

    public void addShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
    }

    public void changeLocation(User user, short x, short y) {
    }

    public void skipTurn(User user) {

    }

    public void stopTraining() {

    }
}
