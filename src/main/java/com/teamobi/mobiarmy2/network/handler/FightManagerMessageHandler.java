package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.bootstrap.ApplicationContext;
import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.UserState;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.fight.TrainingManager;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.FightItemManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class FightManagerMessageHandler extends BaseMessageHandler {
    public FightManagerMessageHandler(Session session) {
        super(session);
    }

    public void movePlayer(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            short x = dis.readShort();
            short y = dis.readShort();

            if (user.getState() == UserState.FIGHTING) {
                user.getFightWait().getFightManager().changeLocation(user.getUserId(), x, y);
            } else if (user.getState() == UserState.TRAINING) {
                user.getTrainingManager().changeLocation(x, y);
            }
        } catch (IOException ignored) {
        }
    }

    public void shoot(Message ms) {
        if (user.getState() != UserState.FIGHTING) {
            return;
        }
        DataInputStream dis = ms.reader();
        try {
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

            user.getFightWait().getFightManager().addShoot(user.getUserId(), bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException ignored) {
        }
    }

    public void processShootingResult(Message ms) {
        //todo
    }

    public void handleUseItem(Message ms) {
        try {
            byte itemIndex = ms.reader().readByte();
            if (itemIndex != 100) {
                if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
                    return;
                }

                if (user.getItemFightQuantity(itemIndex) < 1) {
                    return;
                }
            }
            user.getFightWait().getFightManager().useItem(user.getUserId(), itemIndex);
        } catch (IOException ignored) {
        }
    }

    public void skipTurn() {
        user.getFightWait().getFightManager().skipTurn(user.getUserId());
    }

    public void updateCoordinates(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            short x = dis.readShort();
            short y = dis.readShort();
        } catch (IOException ignored) {
        }
    }

    public void clearBullet(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            int size = dis.readByte();
            int[] x = new int[size];
            int[] y = new int[size];
            for (byte i = 0; i < size; i++) {
                x[i] = dis.readInt();
                y[i] = dis.readInt();
            }
            //todo
        } catch (IOException ignored) {
        }
    }

    public void enterTrainingMap() {
        try {
            initializeTrainingManager();
            Message ms = new Message(Cmd.TRAINING_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getTrainingManager().getMapId());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void startTraining(Message ms) {
        try {
            byte type = ms.reader().readByte();

            initializeTrainingManager();

            if (type == 0) {//Start game
                if (user.isNotWaiting()) {
                    return;
                }

                user.setState(UserState.TRAINING);
                user.getTrainingManager().startTraining();
            } else {//Out game
                if (user.getState() != UserState.TRAINING) {
                    return;
                }

                user.setState(UserState.WAITING);
                user.getTrainingManager().stopTraining();

                ms = new Message(Cmd.TRAINING);
                DataOutputStream ds = ms.writer();
                ds.writeByte(1);
                ds.flush();
                sendMessage(ms);
            }
        } catch (IOException ignored) {
        }
    }

    public void trainShooting(Message ms) {
        if (user.getState() != UserState.TRAINING) {
            return;
        }

        DataInputStream dis = ms.reader();
        try {
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

            user.getTrainingManager().addShoot(user, bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException ignored) {
        }
    }

    private void initializeTrainingManager() {
        if (user.getTrainingManager() == null) {
            ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);
            user.setTrainingManager(new TrainingManager(user, serverConfig.getTrainingMapId()));
        }
    }
}
