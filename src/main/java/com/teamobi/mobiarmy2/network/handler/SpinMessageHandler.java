package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;

public class SpinMessageHandler extends BaseMessageHandler {
    private long lastSpinTime;

    public SpinMessageHandler(Session session) {
        super(session);
    }

    public void handleSpinWheel(Message ms) throws IOException {
        ServerConfig serverConfig = ApplicationContext.getInstance().getBean(ServerConfig.class);

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < 5000) {
            sendServerMessage(GameString.SPIN_WAIT_TIME);
            return;
        }

        byte unit = ms.reader().readByte();
        if (unit == 0) {
            if (us().getXu() < serverConfig.getSpinXuCost()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-serverConfig.getSpinXuCost());
        } else {
            if (us().getLuong() < serverConfig.getSpinLuongCost()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateLuong(-serverConfig.getSpinLuongCost());
        }
        ms = new Message(Cmd.RULET);
        DataOutputStream ds = ms.writer();
        int luckyIndex = Utils.nextInt(10);
        for (byte i = 0; i < 10; i++) {
            byte type = (byte) Utils.nextInt(serverConfig.getSpinTypeProbabilities());
            byte itemId = 0;
            int quantity = 0;

            switch (type) {
                case 0 -> {
                    itemId = FightItemManager.getRandomItem();
                    quantity = serverConfig.getSpinItemCounts()[0][Utils.nextInt(serverConfig.getSpinItemCounts()[1])];
                    if (i == luckyIndex) {
                        us().updateFightItems(itemId, (byte) quantity);
                    }
                }
                case 1 -> {
                    quantity = serverConfig.getSpinXuCounts()[0][Utils.nextInt(serverConfig.getSpinXuCounts()[1])];
                    if (i == luckyIndex) {
                        us().updateXu(quantity);
                    }
                }
                case 2 -> {
                    quantity = serverConfig.getSpinXpCounts()[0][Utils.nextInt(serverConfig.getSpinXpCounts()[1])];
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
