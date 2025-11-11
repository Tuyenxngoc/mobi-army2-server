package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.app.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.entity.User;
import com.teamobi.mobiarmy2.network.handler.*;
import com.teamobi.mobiarmy2.service.ClanService;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;
import io.netty.channel.Channel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.*;

@Slf4j
public class Session {
    private static final ExecutorService VIRTUAL_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();
    private static final Message POISON_PILL = new PoisonMessage();
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
    private volatile boolean running = true;

    public Session(long sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.ipAddress = channel.remoteAddress().toString();

        ApplicationContext ctx = ApplicationContext.getInstance();
        AuthMessageHandler authMessageHandler = new AuthMessageHandler(
                this,
                ctx.getBean(LoginRateLimiterService.class),
                ctx.getBean(UserDAO.class),
                ctx.getBean(AccountDAO.class),
                ctx.getBean(UserCharacterDAO.class)
        );
        this.messageRouter = new MessageRouter(authMessageHandler);

        VIRTUAL_EXECUTOR.submit(this::processLoop);
    }

    public void sendMessage(Message msg) {
        if (msg == null || channel == null || !channel.isActive()) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Server sends ms {} to client {}", Cmd.getCmdNameByValue(msg.getCommand()), sessionId);
        }

        channel.writeAndFlush(msg);
    }

    private void processLoop() {
        try {
            while (running) {
                Message msg = messageQueue.take();

                // Check for poison pill
                if (msg instanceof PoisonMessage) {
                    break;
                }

                messageRouter.onMessage(msg);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            // Cleanup on exit
            if (isUserLoggedIn()) {
                messageRouter.getAuthMessageHandler().handleUserLogoutCleanup();
            }
        }
    }

    public void enqueueMessage(Message msg) {
        if (!running || !isActive() || !isAuthorized(msg)) {
            return;
        }

        if (!messageQueue.offer(msg)) {
            log.warn("Failed to enqueue message for session {}, queue might be full", sessionId);
        }
    }

    public boolean isActive() {
        return channel != null && channel.isActive();
    }

    private boolean isAuthorized(Message msg) {
        if (WHITE_LIST_CMDS.contains(msg.getCommand())) {
            return true;
        }
        return isUserLoggedIn();
    }

    private boolean isUserLoggedIn() {
        return user != null && user.isLogged();
    }

    public void closeChannel() {
        if (isActive()) {
            channel.close();
        }
    }

    public void cleanup() {
        if (!running) {
            return;
        }
        running = false;

        messageQueue.clear();
        messageQueue.offer(POISON_PILL);
    }

    public void registerHandlers() {
        ApplicationContext context = ApplicationContext.getInstance();

        UserDAO userDAO = context.getBean(UserDAO.class);
        UserCharacterDAO userCharacterDAO = context.getBean(UserCharacterDAO.class);
        LeaderboardService leaderboardService = context.getBean(LeaderboardService.class);
        GiftCodeDAO giftCodeDAO = context.getBean(GiftCodeDAO.class);
        UserGiftCodeDAO userGiftCodeDAO = context.getBean(UserGiftCodeDAO.class);
        ClanService clanService = context.getBean(ClanService.class);

        ClanMessageHandler clanMessageHandler = new ClanMessageHandler(this, clanService);
        FriendMessageHandler friendMessageHandler = new FriendMessageHandler(this, userDAO);
        ShopMessageHandler shopMessageHandler = new ShopMessageHandler(this, userCharacterDAO);
        ResourceMessageHandler resourceMessageHandler = new ResourceMessageHandler(this);
        MissionMessageHandler missionMessageHandler = new MissionMessageHandler(this);
        FormulaMessageHandler formulaMessageHandler = new FormulaMessageHandler(this);
        RoomMessageHandler roomMessageHandler = new RoomMessageHandler(this);
        FightWaitMessageHandler fightWaitMessageHandler = new FightWaitMessageHandler(this, userDAO);
        FightManagerMessageHandler fightManagerMessageHandler = new FightManagerMessageHandler(this);
        InventoryMessageHandler inventoryMessageHandler = new InventoryMessageHandler(this);
        LeaderboardMessageHandler leaderboardMessageHandler = new LeaderboardMessageHandler(this, leaderboardService);
        SpinMessageHandler spinMessageHandler = new SpinMessageHandler(this);
        PaymentMessageHandler paymentMessageHandler = new PaymentMessageHandler(this, giftCodeDAO, userGiftCodeDAO);
        CharacterMessageHandler characterMessageHandler = new CharacterMessageHandler(this);

        messageRouter.setClanMessageHandler(clanMessageHandler);
        messageRouter.setFriendMessageHandler(friendMessageHandler);
        messageRouter.setShopMessageHandler(shopMessageHandler);
        messageRouter.setResourceMessageHandler(resourceMessageHandler);
        messageRouter.setMissionMessageHandler(missionMessageHandler);
        messageRouter.setFormulaMessageHandler(formulaMessageHandler);
        messageRouter.setRoomMessageHandler(roomMessageHandler);
        messageRouter.setFightWaitMessageHandler(fightWaitMessageHandler);
        messageRouter.setFightManagerMessageHandler(fightManagerMessageHandler);
        messageRouter.setInventoryMessageHandler(inventoryMessageHandler);
        messageRouter.setLeaderboardMessageHandler(leaderboardMessageHandler);
        messageRouter.setSpinMessageHandler(spinMessageHandler);
        messageRouter.setPaymentMessageHandler(paymentMessageHandler);
        messageRouter.setCharacterMessageHandler(characterMessageHandler);
    }

    public static void shutdownExecutor() {
        VIRTUAL_EXECUTOR.shutdown();
        try {
            if (!VIRTUAL_EXECUTOR.awaitTermination(10, TimeUnit.SECONDS)) {
                log.warn("Executor did not terminate in time, forcing shutdown...");
                VIRTUAL_EXECUTOR.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("Interrupted while waiting for executor shutdown", e);
            VIRTUAL_EXECUTOR.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
