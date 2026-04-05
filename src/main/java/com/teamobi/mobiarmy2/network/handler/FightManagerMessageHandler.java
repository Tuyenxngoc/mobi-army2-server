package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.TrainingManager;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.server.RoomManager;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

@Slf4j
public class FightManagerMessageHandler extends BaseMessageHandler {
    public FightManagerMessageHandler(Session session) {
        super(session);
    }

    public void movePlayer(Message ms) throws IOException {
        if (us().getState() != UserState.FIGHTING) {
            return;
        }

        DataInputStream dis = ms.reader();
        short x = dis.readShort();
        short y = dis.readShort();
        fm().handlePlayerMove(us().getUserId(), x, y);
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

    public void processShootingResult() {
        // Client vẽ xong đạn bay và xử lý va chạm, gửi kết quả về server
        // Theo logic thì sau khi client gủi thì mói đổi lượt, hoặc quá thời gian chờ
        fm().handlePlayerShootResult(us().getUserId());
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
        fm().updatePlayerCoordinates(us().getUserId(), x, y);
    }

    public void handleCheckCross(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        int size = dis.readByte();
        int[] x = new int[size];
        int[] y = new int[size];
        for (byte i = 0; i < size; i++) {
            x[i] = dis.readInt();
            y[i] = dis.readInt();
        }
        // Danh sách tọa độ (x, y) của các điểm nổ / điểm giao cắt đạn lên server.
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
            us().getTrainingManager().startGame();
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

        us().getTrainingManager().handlePlayerShoot(us().getUserId(), bullId, x, y, angle, force, force2, numShoot);
    }

    private void initializeTrainingManager() {
        if (us().getTrainingManager() == null) {
            us().setTrainingManager(new TrainingManager(us(), RoomManager.TRAINING_MAP_ID));
        }
    }
}
