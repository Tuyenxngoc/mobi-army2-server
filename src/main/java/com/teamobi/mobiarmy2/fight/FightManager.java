package com.teamobi.mobiarmy2.fight;

/**
 * @author tuyen
 */
public class FightManager {

    public void leaveFight(int targetPlayerId) {

    }

    public void chatMessage(int playerId, String message) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }

    }

    private int getPlayerIndexByPlayerId(int playerId) {
        return 0;
    }
}