package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.fight.ITrainingManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.Impl.Message;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author tuyen
 */
public class TrainingManager implements ITrainingManager {

    private final User trainingUser;
    private final Player[] players;
    private final byte mapId;

    public TrainingManager(User trainingUser, byte mapId) {
        this.trainingUser = trainingUser;
        this.mapId = mapId;
        this.players = new Player[2];
    }

    @Override
    public void startTraining() {
        players[0] = new Player(0, 230, 200, 70, 1000);
        players[1] = new Player(1, 550, 200, 1000, 1000);

        try {
            IMessage ms = new Message(Cmd.START_ARMY);
            DataOutputStream ds = ms.writer();
            short[] equips = trainingUser.getEquips();
            for (short i : equips) {
                ds.writeShort(i);
            }
            ds.writeByte(mapId);
            ds.writeByte(30);
            ds.writeShort(0);
            for (Player player : players) {
                ds.writeShort(player.getX());
                ds.writeShort(player.getY());
                ds.writeShort(player.getMaxHp());
            }
            for (byte i = 0; i < 6; i++) {
                ds.writeShort(-1);
            }
            ds.flush();
            trainingUser.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopTraining() {

    }

    @Override
    public void addShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot) {
        trainingUser.getUserService().sendServerMessage("To be continue...: " + bullId + " " + x + " " + y + " " + angle + " " + force + " " + force2 + " " + numShoot);
    }

    @Override
    public byte getMapId() {
        return mapId;
    }

    @Override
    public void changeLocation(short x, short y) {

    }

}
