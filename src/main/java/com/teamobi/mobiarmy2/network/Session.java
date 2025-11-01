package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.handler.*;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
public class Session {
    private static final Set<Byte> WHITE_LIST_CMDS = Set.of((byte) -27, (byte) 1, (byte) 58, (byte) 114, (byte) 121, (byte) 127);

    @Getter
    private final long sessionId;
    private final Channel channel;

    @Getter
    private final String ipAddress;

    @Setter
    @Getter
    private String platform;

    @Setter
    @Getter
    private String version;

    @Setter
    @Getter
    private byte provider = -1;

    @Setter
    @Getter
    private String agent;

    @Getter
    @Setter
    private User user;

    private final MessageRouter messageRouter;
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final Thread workerThread;

    public Session(long sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.ipAddress = channel.remoteAddress().toString();

        ApplicationContext context = ApplicationContext.getInstance();
        AuthMessageHandler authMessageHandler = new AuthMessageHandler(this, context.getBean(LoginRateLimiterService.class), context.getBean(UserDAO.class), context.getBean(AccountDAO.class), context.getBean(UserCharacterDAO.class));
        this.messageRouter = new MessageRouter(authMessageHandler);

        this.workerThread = new Thread(this::processLoop, "Session-Worker-" + sessionId);
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    public void sendMessage(Message msg) {
        if (msg == null || channel == null || !channel.isActive()) return;
        channel.writeAndFlush(msg);
    }

    private void processLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Message msg = messageQueue.take();
                messageRouter.onMessage(msg);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Session {} error while processing message: {}", sessionId, e.getMessage(), e);
            }
        }
    }

    public void enqueueMessage(Message msg) {
        if (isActive() && isAuthorized(msg)) {
            messageQueue.offer(msg);
        }
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    private boolean isAuthorized(Message msg) {
        if (WHITE_LIST_CMDS.contains(msg.getCommand())) {
            return true;
        }
        return user != null && user.isLogged();
    }

    public void closeChannel() {
        if (isActive()) {
            channel.close();
        }
    }

    public void cleanup() {
        // Stop worker thread
        if (workerThread.isAlive()) {
            workerThread.interrupt();
        }

        if (user != null && user.isLogged()) {
            // Save user data
            messageRouter.getAuthMessageHandler().handleUserLogoutCleanup();
        }
    }

    public void initMessageHandlers() {
        ApplicationContext context = ApplicationContext.getInstance();

        ClanMessageHandler clanMessageHandler = new ClanMessageHandler(this, context.getBean(ClanDAO.class));
        FriendMessageHandler friendMessageHandler = new FriendMessageHandler(this, context.getBean(UserDAO.class));
        ShopMessageHandler shopMessageHandler = new ShopMessageHandler(this, context.getBean(UserCharacterDAO.class));
        ResourceMessageHandler resourceService = new ResourceMessageHandler(this);
        MissionMessageHandler missionMessageHandler = new MissionMessageHandler(this);
        FormulaMessageHandler formulaMessageHandler = new FormulaMessageHandler(this);
        RoomMessageHandler roomMessageHandler = new RoomMessageHandler(this);
        FightWaitMessageHandler fightWaitMessageHandler = new FightWaitMessageHandler(this, context.getBean(UserDAO.class));
        FightManagerMessageHandler fightManagerMessageHandler = new FightManagerMessageHandler(this);
        InventoryMessageHandler inventoryMessageHandler = new InventoryMessageHandler(this);
        LeaderboardMessageHandler leaderboardMessageHandler = new LeaderboardMessageHandler(this, context.getBean(LeaderboardService.class));
        GiftBoxMessageHandler giftBoxMessageHandler = new GiftBoxMessageHandler(this);
        SpinMessageHandler spinMessageHandler = new SpinMessageHandler(this);
        PaymentMessageHandler paymentMessageHandler = new PaymentMessageHandler(this, context.getBean(GiftCodeDAO.class), context.getBean(UserGiftCodeDAO.class));
        CharacterMessageHandler characterMessageHandler = new CharacterMessageHandler(this);

        messageRouter.setClanMessageHandler(clanMessageHandler);
        messageRouter.setFriendMessageHandler(friendMessageHandler);
        messageRouter.setShopMessageHandler(shopMessageHandler);
        messageRouter.setResourceMessageHandler(resourceService);
        messageRouter.setMissionMessageHandler(missionMessageHandler);
        messageRouter.setFormulaMessageHandler(formulaMessageHandler);
        messageRouter.setRoomMessageHandler(roomMessageHandler);
        messageRouter.setFightWaitMessageHandler(fightWaitMessageHandler);
        messageRouter.setFightManagerMessageHandler(fightManagerMessageHandler);
        messageRouter.setInventoryMessageHandler(inventoryMessageHandler);
        messageRouter.setLeaderboardMessageHandler(leaderboardMessageHandler);
        messageRouter.setGiftBoxMessageHandler(giftBoxMessageHandler);
        messageRouter.setSpinMessageHandler(spinMessageHandler);
        messageRouter.setPaymentMessageHandler(paymentMessageHandler);
        messageRouter.setCharacterMessageHandler(characterMessageHandler);
    }
}
