package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.server.FightItemManager;
import com.teamobi.mobiarmy2.util.RandomUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class GiftBoxService {
    private static final int MAX_GIFTS = 12;          // Số quà tối đa
    private static final int MAX_OPENED_GIFTS = 6;    // Số quà có thể mở tối đa
    private static final int XU_COST_PER_GIFT = 1000; // Chi phí mở mỗi quà khi hết lượt
    private static final int DEFAULT_GIFT_TIME = 30; // Thời gian mặc định để mở quà (giây)

    private final MessageSender messageSender;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> giftTask;

    private final Map<Integer, GiftBoxState> userGiftStates = new ConcurrentHashMap<>();
    private int giftOpenTime;
    private boolean running = false;

    public GiftBoxService() {
        this.messageSender = ApplicationContext.getInstance().getBean(MessageSender.class);
    }

    static class GiftBoxState {
        User user;
        int freeGiftBoxCount; // Số lượt mở quà miễn phí còn lại
        boolean[] giftOpened = new boolean[MAX_GIFTS]; // Mảng kiểm tra quà đã mở hay chưa
        int openedGiftCount = 0; // Số quà đã mở
        boolean openingGift = true; // Trạng thái đang mở quà

        GiftBoxState(User user, int freeGiftBoxCount) {
            this.user = user;
            this.freeGiftBoxCount = freeGiftBoxCount;
        }
    }

    static class Reward {
        byte id;
        byte type;
        String str;

        Reward(byte id, byte type, String str) {
            this.id = id;
            this.type = type;
            this.str = str;
        }
    }

    private void sendStartMessage(User user, int freeGiftBoxCount, int giftOpenTime) {
        try {
            Message ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(-1);
            ds.writeByte(giftOpenTime);
            ds.writeUTF(GameString.createGiftOpeningSummaryMessage(freeGiftBoxCount, MAX_OPENED_GIFTS, XU_COST_PER_GIFT));
            ds.flush();
            messageSender.sendTo(user, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendGiftResults(GiftBoxState state) {
        try {
            Message ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(-2);
            for (boolean opened : state.giftOpened) {
                if (opened) {
                    ds.writeByte(-1);
                } else {
                    Reward reward = generateAndProcessReward(null);
                    ds.writeByte(reward.type);
                    ds.writeByte(reward.id);
                    ds.writeUTF(reward.str);
                }
            }
            ds.flush();
            messageSender.sendTo(state.user, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Reward generateAndProcessReward(User user) {
        byte id;
        byte type = 2;
        String str;
        int[] rate = new int[]{140, 200, 150, 300, 200, 10};
        int randomIndex = RandomUtil.nextInt(rate);
        switch (randomIndex) {
            case 0 -> {
                int randomXu = RandomUtil.getNonLinearRandom(50, 1049);
                int xuUp = (randomXu / 50) * 50;
                if (user != null) {
                    user.updateXu(xuUp);
                } else {
                    xuUp += 100;
                }
                id = 55;
                str = "+" + Utils.getStringNumber(xuUp) + " xu";
            }
            case 1 -> {
                int randomXp = RandomUtil.getNonLinearRandom(50, 1049);
                int xpUp = (randomXp / 50) * 50;
                if (user != null) {
                    user.updateXp(xpUp, false);
                } else {
                    xpUp += 100;
                }
                id = 56;
                str = "+" + Utils.getStringNumber(xpUp) + " xp";
            }
            case 2 -> {
                byte[] nextItem = new byte[]{0, 10, 20, 30, 40};
                if (user != null) {
                    byte idItem = (byte) RandomUtil.nextInt(6);
                    id = (byte) (idItem + nextItem[RandomUtil.nextInt(nextItem.length)]);
                    user.addSpecialItem(id, (short) 1);
                } else {
                    byte idItem = (byte) (RandomUtil.nextInt(6) + 4);
                    id = (byte) (idItem + nextItem[RandomUtil.nextInt(nextItem.length)]);
                }
                str = "+1";
            }
            case 3 -> {
                type = 3;
                id = FightItemManager.getRandomItem();
                byte numb;
                if (user != null) {
                    numb = (byte) RandomUtil.nextInt(1, 5);
                    user.updateFightItems(id, numb);
                } else {
                    numb = (byte) RandomUtil.nextInt(1, 10);
                }
                str = "+" + numb;
            }
            case 4 -> {
                id = (byte) RandomUtil.nextInt(62, 68);
                short numb;
                if (user != null) {
                    numb = (short) RandomUtil.nextInt(1, 5);
                    user.addSpecialItem(id, numb);
                } else {
                    numb = (short) RandomUtil.nextInt(1, 10);
                }
                str = "+" + numb;
            }
            default -> {
                byte[] arrItems = new byte[]{54};
                id = arrItems[RandomUtil.nextInt(arrItems.length)];
                if (user != null) {
                    user.addSpecialItem(id, (short) 1);
                }
                str = "+1";
            }
        }
        return new Reward(id, type, str);
    }

    public void startGiftBoxOpening(List<User> winners, int freeGiftBoxCount) {
        if (running) {
            return;
        }

        giftOpenTime = DEFAULT_GIFT_TIME;
        running = true;

        // Khởi tạo trạng thái từng người
        for (User u : winners) {
            GiftBoxState state = new GiftBoxState(u, freeGiftBoxCount);
            userGiftStates.put(u.getUserId(), state);
            sendStartMessage(u, freeGiftBoxCount, giftOpenTime);
        }

        // Bộ đếm chung
        giftTask = executor.scheduleAtFixedRate(() -> {
            giftOpenTime--;

            // Lặp qua toàn bộ người chơi đang mở quà
            for (GiftBoxState state : userGiftStates.values()) {
                if (!state.openingGift) {
                    continue;
                }

                if (state.openedGiftCount >= MAX_OPENED_GIFTS) {
                    finishForPlayer(state);
                    continue;
                }

                if (giftOpenTime <= 0) {
                    finishForPlayer(state);
                }
            }

            // Dừng khi hết thời gian hoặc tất cả người chơi đều đóng
            if (giftOpenTime <= 0 || allPlayersDone()) {
                stopGiftEvent();
            }

        }, 0, 1, TimeUnit.SECONDS);
    }

    private boolean allPlayersDone() {
        return userGiftStates.values().stream().noneMatch(s -> s.openingGift);
    }

    public boolean isOpeningGift(int userId) {
        GiftBoxState state = userGiftStates.get(userId);
        return state != null && state.openingGift;
    }

    private void finishForPlayer(GiftBoxState state) {
        if (!state.openingGift) {
            return;
        }
        state.openingGift = false;
        sendGiftResults(state);
    }

    private void stopGiftEvent() {
        if (giftTask != null) {
            giftTask.cancel(false);
        }
        running = false;
        userGiftStates.clear();
    }

    public void openGiftBoxAfterFight(int userId, byte boxIndex) {
        GiftBoxState state = userGiftStates.get(userId);
        if (state == null || !state.openingGift) {
            return;
        }

        if (boxIndex == -2) {// Lệnh đóng mở quà
            finishForPlayer(state);
            return;
        }
        if (boxIndex < 0 || boxIndex >= state.giftOpened.length || state.giftOpened[boxIndex]) {// Kiểm tra chỉ số hợp lệ
            return;
        }

        User user = state.user;
        if (state.freeGiftBoxCount > 0) {
            state.freeGiftBoxCount--;
        } else if (user.getXu() >= XU_COST_PER_GIFT) {
            user.updateXu(-XU_COST_PER_GIFT);
        } else {
            finishForPlayer(state);
            return;
        }

        state.giftOpened[boxIndex] = true;
        state.openedGiftCount++;

        Reward reward = generateAndProcessReward(user);
        try {
            Message ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(boxIndex);
            ds.writeByte(reward.type);
            ds.writeByte(reward.id);
            ds.writeUTF(reward.str);
            ds.flush();
            messageSender.sendTo(user, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
