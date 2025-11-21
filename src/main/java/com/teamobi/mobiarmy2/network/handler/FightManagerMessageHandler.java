package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.TrainingManager;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FightManagerMessageHandler extends BaseMessageHandler {
    public FightManagerMessageHandler(Session session) {
        super(session);
    }

    public void movePlayer(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        short x = dis.readShort();
        short y = dis.readShort();

        if (us().getState() == UserState.FIGHTING) {
            fm().changeLocation(us().getUserId(), x, y);
        } else if (us().getState() == UserState.TRAINING) {
            us().getTrainingManager().changeLocation(x, y);
        }
    }

    public void handleShot(Message ms) throws IOException {
        if (us().getState() != UserState.FIGHTING) {
            return;
        }
        DataInputStream dis = ms.reader();
        byte bullId = dis.readByte();
        short x = dis.readShort();
        short y = dis.readShort();
        short angle = (short) Utils.clamp(dis.readShort(), -360, 360);
        byte force = (byte) Utils.clamp(dis.readByte(), 0, 30);
        byte force2 = 0;
        if (bullId == 17 || bullId == 19) {
            force2 = (byte) Utils.clamp(dis.readByte(), 0, 30);
        }
        byte numShoot = dis.readByte();

        fm().handlePlayerShoot(us().getUserId(), bullId, x, y, angle, force, force2, numShoot);
    }

    public void processShootingResult(Message ms) {
        //todo
    }

    public void handleUseItem(Message ms) throws IOException {
        byte itemIndex = ms.reader().readByte();
        if (itemIndex != 100) {
            if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
                return;
            }

            if (us().getItemFightQuantity(itemIndex) < 1) {
                return;
            }
        }
        fm().useItem(us().getUserId(), itemIndex);
    }

    public void skipTurn() {
        fm().skipTurn(us().getUserId());
    }

    public void updateCoordinates(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        short x = dis.readShort();
        short y = dis.readShort();
    }

    public void clearBullet(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        int size = dis.readByte();
        int[] x = new int[size];
        int[] y = new int[size];
        for (byte i = 0; i < size; i++) {
            x[i] = dis.readInt();
            y[i] = dis.readInt();
        }
        //todo
    }

    public void enterTrainingMap() throws IOException {
        initializeTrainingManager();
        Message ms = new Message(Cmd.TRAINING_MAP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(us().getTrainingManager().getMapId());
        ds.flush();
        sendMessage(ms);
    }

    public void startTraining(Message ms) throws IOException {
        byte type = ms.reader().readByte();

        initializeTrainingManager();

        if (type == 0) {//Start game
            if (us().isNotWaiting()) {
                return;
            }

            us().setState(UserState.TRAINING);
            us().getTrainingManager().startTraining();
        } else {//Out game
            if (us().getState() != UserState.TRAINING) {
                return;
            }

            us().setState(UserState.WAITING);
            us().getTrainingManager().stopTraining();

            ms = new Message(Cmd.TRAINING);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.flush();
            sendMessage(ms);
        }
    }

    public void trainShooting(Message ms) throws IOException {
        if (us().getState() != UserState.TRAINING) {
            return;
        }

        DataInputStream dis = ms.reader();
        byte bullId = dis.readByte();
        short x = dis.readShort();
        short y = dis.readShort();
        short angle = (short) Utils.clamp(dis.readShort(), -360, 360);
        byte force = (byte) Utils.clamp(dis.readByte(), 0, 30);
        byte force2 = 0;
        if (bullId == 17 || bullId == 19) {
            force2 = (byte) Utils.clamp(dis.readByte(), 0, 30);
        }
        byte numShoot = dis.readByte();

        us().getTrainingManager().addShoot(us(), bullId, x, y, angle, force, force2, numShoot);
    }

    private void initializeTrainingManager() {
        if (us().getTrainingManager() == null) {
            ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
            us().setTrainingManager(new TrainingManager(us(), serverConfig.getTrainingMapId()));
        }
    }
}
