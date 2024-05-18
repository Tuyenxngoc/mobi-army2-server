package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.DataOutputStream;

/**
 * @author tuyen
 */
public class FightManager {

    private final Player[] players;

    public FightManager() {
        this.players = new Player[8];
    }

    public void leaveFight(int targetPlayerId) {

    }

    public void chatMessage(int playerId, String message) {
        int index = getPlayerIndexByPlayerId(playerId);
        if (index == -1) {
            return;
        }
        try {
            Message ms = new Message(Cmd.CHAT_TO_BOARD);
            DataOutputStream ds = ms.writer();
            ds.writeInt(playerId);
            ds.writeUTF(message);
            ds.flush();
            sendToTeam(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendToTeam(Message ms) {
        for (byte i = 0; i < 8; i++) {
            Player player = players[i];
            if (player != null && player.getUser() != null) {
                player.getUser().sendMessage(ms);
            }
        }
    }

    private int getPlayerIndexByPlayerId(int playerId) {
        for (byte i = 0; i < 8; i++) {
            Player player = players[i];
            if (player != null &&
                    player.getUser() != null &&
                    player.getUser().getPlayerId() == playerId
            ) {
                return i;
            }
        }
        return -1;
    }
}