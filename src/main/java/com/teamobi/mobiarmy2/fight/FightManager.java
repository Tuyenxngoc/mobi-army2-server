package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.DataOutputStream;
import java.io.IOException;

public class FightManager {
    private final FightWait fightWait;
    private Player[] players;
    private byte windX;
    private byte windY;


    public FightManager(FightWait fightWait) {
        this.fightWait = fightWait;

        this.players = new Player[fightWait.getRoom().getMaxElementFight()];
    }

    public void leave(int targetPlayerId) {

    }

    public void chatMessage(int playerId, String message) {

    }

    public void startGame() {
        if (fightWait.isStarted()) {
            return;
        }

        windX = 0;
        windY = 0;

        for (int i = 0; i < fightWait.getUsers().length; i++) {
            User user = fightWait.getUsers()[i];
            if (user == null) {
                continue;
            }
            players[i] = new Player(user);
        }

        sendFightInfo();
    }

    private void sendFightInfo() {

        if (fightWait.getMoney() > 0) {
            for (User user : fightWait.getUsers()) {
                if (user == null) {
                    continue;
                }
                try {
                    Message ms = new Message(Cmd.BONUS_MONEY);
                    DataOutputStream ds = ms.writer();
                    ds.writeInt(user.getPlayerId());
                    ds.writeInt(-fightWait.getMoney());
                    ds.writeInt(user.getXu());
                    ds.flush();
                    user.sendMessage(ms);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        for (byte i = 0; i < 8; i++) {
            Player player = players[i];
            if (player == null || player.getUser() == null) {
                continue;
            }
            try {
                Message ms = new Message(Cmd.START_ARMY);
                DataOutputStream ds = ms.writer();
                ds.flush();
                player.getUser().sendMessage(ms);
            } catch (IOException e) {
               e.printStackTrace();
            }
        }
    }

    public void changeLocationMessage(User user, short x, short y) {

    }

    public void newShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {

    }

    public void skipTurn(User user) {

    }
}
