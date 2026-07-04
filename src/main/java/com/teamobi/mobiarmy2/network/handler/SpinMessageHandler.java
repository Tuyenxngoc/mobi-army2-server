package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.util.RandomUtil;

import java.io.DataOutputStream;
import java.io.IOException;

public class SpinMessageHandler extends BaseMessageHandler {
    private static final int SPIN_WAIT_TIME_MS = 5000;
    private static final int SPIN_XU_COST = 1000;
    private static final int SPIN_LUONG_COST = 1;

    // Xác suất loại phần thưởng (0: item, 1: xu, 2: xp, 3: rỗng)
    private static final int[] SPIN_TYPE_PROBABILITIES = {300, 150, 450, 100};

    // Mỗi mảng gồm 2 hàng: [0] là giá trị phần thưởng, [1] là xác suất tương ứng
    private static final int[][] SPIN_ITEM_COUNTS = {
            {1, 5, 10, 15},
            {400, 300, 200, 100}
    };

    private static final int[][] SPIN_XU_COUNTS = {
            {500, 1000, 5000, 10000},
            {400, 300, 200, 100}
    };

    private static final int[][] SPIN_XP_COUNTS = {
            {1, 50, 100, 500},
            {400, 300, 200, 100}
    };

    private long lastSpinTime;

    public SpinMessageHandler(Session session) {
        super(session);
    }

    public void handleSpinWheel(Message ms) throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < SPIN_WAIT_TIME_MS) {
            messageSender.sendServerMessage(us(), GameString.SPIN_WAIT_TIME);
            return;
        }

        byte unit = ms.reader().readByte();
        if (unit == 0) {
            if (us().getXu() < SPIN_XU_COST) {
                messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-SPIN_XU_COST);
        } else {
            if (us().getLuong() < SPIN_LUONG_COST) {
                messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateLuong(-SPIN_LUONG_COST);
        }
        ms = new Message(Cmd.RULET);
        DataOutputStream ds = ms.writer();
        int luckyIndex = RandomUtil.nextInt(10);
        for (byte i = 0; i < 10; i++) {
            byte type = (byte) RandomUtil.nextInt(SPIN_TYPE_PROBABILITIES);
            byte itemId = 0;
            int quantity = 0;

            switch (type) {
                case 0 -> {
                    itemId = FightItemManager.getRandomItem();
                    quantity = SPIN_ITEM_COUNTS[0][RandomUtil.nextInt(SPIN_ITEM_COUNTS[1])];
                    if (i == luckyIndex) {
                        us().updateFightItems(itemId, (byte) quantity);
                    }
                }
                case 1 -> {
                    quantity = SPIN_XU_COUNTS[0][RandomUtil.nextInt(SPIN_XU_COUNTS[1])];
                    if (i == luckyIndex) {
                        us().updateXu(quantity);
                    }
                }
                case 2 -> {
                    quantity = SPIN_XP_COUNTS[0][RandomUtil.nextInt(SPIN_XP_COUNTS[1])];
                    if (i == luckyIndex) {
                        us().updateXp(quantity);
                    }
                }
            }
            ds.writeByte(type);
            ds.writeByte(itemId);
            ds.writeInt(quantity);
        }
        ds.writeByte(luckyIndex);
        ds.flush();
        sendMessage(ms);

        lastSpinTime = currentTime;
    }
}
