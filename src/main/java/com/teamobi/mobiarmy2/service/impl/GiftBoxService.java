package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.service.IGiftBoxService;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author tuyen
 */
public class GiftBoxService implements IGiftBoxService {
    private record Reward(byte id, byte type, String str) {
    }

    private static final int MAX_GIFTS = 12;          // Số quà tối đa
    private static final int MAX_OPENED_GIFTS = 6;    // Số quà có thể mở tối đa
    private static final int XU_COST_PER_GIFT = 1000; // Chi phí mở mỗi quà khi hết lượt

    private int availableGifts;    // Số quà hiện có
    private int giftOpenTime;      // Thời gian mở quà
    private boolean openingGift;   // Trạng thái mở quà
    private boolean[] giftOpened;  // Mảng kiểm tra quà đã mở hay chưa
    private int openedGiftCount;
    private final User user;
    private final ScheduledExecutorService executorService;
    private ScheduledFuture<?> giftBoxTask;

    public GiftBoxService(User user) {
        this.user = user;
        this.availableGifts = 0;
        this.giftOpenTime = 0;
        this.openingGift = false;
        this.giftOpened = new boolean[MAX_GIFTS];
        this.openedGiftCount = 0;
        this.executorService = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public boolean isOpeningGift() {
        return openingGift;
    }

    private void sendStartMessage(int availableGifts, int giftOpenTime) {
        try {
            IMessage ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(-1);
            ds.writeByte(giftOpenTime);
            ds.writeUTF(GameString.createGiftOpeningSummaryMessage(availableGifts, MAX_OPENED_GIFTS, XU_COST_PER_GIFT));
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    private void startGiftBoxThread() {
        if (giftBoxTask != null && !giftBoxTask.isDone()) {
            return;
        }

        giftBoxTask = executorService.scheduleAtFixedRate(() -> {
            if (openedGiftCount >= MAX_OPENED_GIFTS || giftOpenTime <= 0 || !openingGift) {
                sendGiftResults();
                resetGiftBoxState();
                giftBoxTask.cancel(false);
                return;
            }

            giftOpenTime--;
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void sendGiftResults() {
        try {
            Message ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(-2);
            for (boolean opened : giftOpened) {
                if (opened) {
                    ds.writeByte(-1);
                } else {
                    Reward reward = generateAndProcessReward(false);
                    ds.writeByte(reward.type());
                    ds.writeByte(reward.id());
                    ds.writeUTF(reward.str());
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    private Reward generateAndProcessReward(boolean updateUser) {
        byte id;
        byte type = 2;
        String str;
        int[] rate = new int[]{140, 200, 150, 300, 200, 10};
        int randomIndex = Utils.nextInt(rate);
        switch (randomIndex) {
            case 0 -> {
                int randomXu = Utils.getNonLinearRandom(50, 1049);
                int xuUp = (randomXu / 50) * 50;
                if (updateUser) {
                    user.updateXu(xuUp);
                } else {
                    xuUp += 100;
                }
                id = 55;
                str = "+" + Utils.getStringNumber(xuUp) + " xu";
            }
            case 1 -> {
                int randomXp = Utils.getNonLinearRandom(50, 1049);
                int xpUp = (randomXp / 50) * 50;
                if (updateUser) {
                    user.updateXp(xpUp, false);
                } else {
                    xpUp += 100;
                }
                id = 56;
                str = "+" + Utils.getStringNumber(xpUp) + " xp";
            }
            case 2 -> {
                byte[] nextItem = new byte[]{0, 10, 20, 30, 40};
                if (updateUser) {
                    byte idItem = (byte) Utils.nextInt(6);
                    id = (byte) (idItem + nextItem[Utils.nextInt(nextItem.length)]);
                    user.addSpecialItem(id, (short) 1);
                } else {
                    byte idItem = (byte) (Utils.nextInt(6) + 4);
                    id = (byte) (idItem + nextItem[Utils.nextInt(nextItem.length)]);
                }
                str = "+1";
            }
            case 3 -> {
                type = 3;
                id = FightItemManager.getRandomItem();
                byte numb;
                if (updateUser) {
                    numb = (byte) Utils.nextInt(1, 5);
                    user.updateFightItems(id, numb);
                } else {
                    numb = (byte) Utils.nextInt(1, 10);
                }
                str = "+" + numb;
            }
            case 4 -> {
                id = (byte) Utils.nextInt(62, 68);
                short numb;
                if (updateUser) {
                    numb = (short) Utils.nextInt(1, 5);
                    user.addSpecialItem(id, numb);
                } else {
                    numb = (short) Utils.nextInt(1, 10);
                }
                str = "+" + numb;
            }
            default -> {
                byte[] arrItems = new byte[]{54};
                id = arrItems[Utils.nextInt(arrItems.length)];
                if (updateUser) {
                    user.addSpecialItem(id, (short) 1);
                }
                str = "+1";
            }
        }
        return new Reward(id, type, str);
    }

    private void resetGiftBoxState() {
        this.availableGifts = 0;
        this.openingGift = false;
        this.giftOpened = new boolean[MAX_GIFTS];
        this.openedGiftCount = 0;
    }

    @Override
    public void startGiftBoxOpening(int availableGifts, int giftOpenTime) {
        this.availableGifts = availableGifts;
        this.giftOpenTime = giftOpenTime;
        this.openingGift = true;
        sendStartMessage(availableGifts, giftOpenTime);
        startGiftBoxThread();
    }

    @Override
    public void openGiftBoxAfterFight(byte boxIndex) {
        if (boxIndex == -2) {
            openingGift = false;
            return;
        }
        if (boxIndex < 0 || boxIndex >= giftOpened.length || giftOpened[boxIndex]) {
            return;
        }
        if (availableGifts > 0) {
            availableGifts--;
        } else if (user.getXu() >= XU_COST_PER_GIFT) {
            user.updateXu(-XU_COST_PER_GIFT);
        } else {
            openingGift = false;
            return;
        }

        giftOpened[boxIndex] = true;
        openedGiftCount++;

        Reward reward = generateAndProcessReward(true);
        try {
            Message ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(boxIndex);
            ds.writeByte(reward.type());
            ds.writeByte(reward.id());
            ds.writeUTF(reward.str());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException ignored) {
        }
    }
}
