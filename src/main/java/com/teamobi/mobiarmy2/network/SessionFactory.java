package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.dao.AccountDAO;
import com.teamobi.mobiarmy2.dao.GiftCodeDAO;
import com.teamobi.mobiarmy2.dao.UserCharacterDAO;
import com.teamobi.mobiarmy2.dao.UserDAO;
import com.teamobi.mobiarmy2.dao.UserGiftCodeDAO;
import com.teamobi.mobiarmy2.network.handler.*;
import com.teamobi.mobiarmy2.server.ExchangeLimitManager;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.server.RoomManager;
import com.teamobi.mobiarmy2.server.ServerState;
import com.teamobi.mobiarmy2.server.SessionRegistry;
import com.teamobi.mobiarmy2.service.ClanService;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;
import io.netty.channel.Channel;

/**
 * Dựng một {@link Session} hoàn chỉnh cho mỗi kết nối: Session, 15 handler và
 * {@link MessageRouter}. Giữ toàn bộ dependency dùng chung ở tầm server nên
 * handler nhận được đủ thứ cần qua constructor.
 */
public class SessionFactory {
    private final MessageSender messageSender;
    private final LoginRateLimiterService loginRateLimiterService;
    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final UserCharacterDAO userCharacterDAO;
    private final GiftCodeDAO giftCodeDAO;
    private final UserGiftCodeDAO userGiftCodeDAO;
    private final ServerConfig serverConfig;
    private final ServerState serverState;
    private final SessionRegistry sessionRegistry;
    private final HikariCPManager hikariCPManager;
    private final ClanService clanService;
    private final LeaderboardService leaderboardService;
    private final RoomManager roomManager;
    private final ExchangeLimitManager exchangeLimitManager;

    public SessionFactory(MessageSender messageSender,
                          LoginRateLimiterService loginRateLimiterService,
                          UserDAO userDAO,
                          AccountDAO accountDAO,
                          UserCharacterDAO userCharacterDAO,
                          GiftCodeDAO giftCodeDAO,
                          UserGiftCodeDAO userGiftCodeDAO,
                          ServerConfig serverConfig,
                          ServerState serverState,
                          SessionRegistry sessionRegistry,
                          HikariCPManager hikariCPManager,
                          ClanService clanService,
                          LeaderboardService leaderboardService,
                          RoomManager roomManager,
                          ExchangeLimitManager exchangeLimitManager) {
        this.messageSender = messageSender;
        this.loginRateLimiterService = loginRateLimiterService;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.userCharacterDAO = userCharacterDAO;
        this.giftCodeDAO = giftCodeDAO;
        this.userGiftCodeDAO = userGiftCodeDAO;
        this.serverConfig = serverConfig;
        this.serverState = serverState;
        this.sessionRegistry = sessionRegistry;
        this.hikariCPManager = hikariCPManager;
        this.clanService = clanService;
        this.leaderboardService = leaderboardService;
        this.roomManager = roomManager;
        this.exchangeLimitManager = exchangeLimitManager;
    }

    public Session create(long sessionId, Channel channel) {
        Session session = new Session(sessionId, channel);
        session.attachRouter(createRouter(session));
        return session;
    }

    private MessageRouter createRouter(Session session) {
        return new MessageRouter(
                new AuthMessageHandler(session, messageSender, loginRateLimiterService, userDAO, accountDAO,
                        userCharacterDAO, serverConfig, serverState, sessionRegistry, hikariCPManager),
                new ClanMessageHandler(session, messageSender, clanService),
                new FriendMessageHandler(session, messageSender, userDAO, sessionRegistry),
                new ShopMessageHandler(session, messageSender, userCharacterDAO),
                new ResourceMessageHandler(session, messageSender, serverConfig),
                new MissionMessageHandler(session, messageSender),
                new FormulaMessageHandler(session, messageSender),
                new RoomMessageHandler(session, messageSender, roomManager),
                new FightWaitMessageHandler(session, messageSender, userDAO),
                new FightManagerMessageHandler(session, messageSender),
                new InventoryMessageHandler(session, messageSender, serverConfig, exchangeLimitManager),
                new LeaderboardMessageHandler(session, messageSender, leaderboardService),
                new SpinMessageHandler(session, messageSender),
                new PaymentMessageHandler(session, messageSender, giftCodeDAO, userGiftCodeDAO, hikariCPManager),
                new CharacterMessageHandler(session, messageSender));
    }
}
