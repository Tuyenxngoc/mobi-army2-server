package com.teamobi.mobiarmy2.app;

import com.teamobi.mobiarmy2.config.HikariCPConfig;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.server.ExchangeLimitManager;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.server.RoomManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.server.SessionRegistry;
import com.teamobi.mobiarmy2.service.*;
import lombok.Getter;

@Getter
public final class AppContext {

    // Config
    private final ServerConfig serverConfig;
    private final HikariCPConfig hikariCPConfig;

    // Database
    private final HikariCPManager hikariCPManager;

    // DAO
    private final AccountDAO accountDAO;
    private final CaptionLevelDAO captionLevelDAO;
    private final CharacterDAO characterDAO;
    private final ClanDAO clanDAO;
    private final ClanShopDAO clanShopDAO;
    private final EquipmentDAO equipmentDAO;
    private final ExperienceLevelDAO experienceLevelDAO;
    private final FabricateItemDAO fabricateItemDAO;
    private final FightItemDAO fightItemDAO;
    private final FormulaDAO formulaDAO;
    private final GiftCodeDAO giftCodeDAO;
    private final MapDAO mapDAO;
    private final MissionDAO missionDAO;
    private final PaymentDAO paymentDAO;
    private final RankingDAO rankingDAO;
    private final SpecialItemDAO specialItemDAO;
    private final UserCharacterDAO userCharacterDAO;
    private final UserDAO userDAO;
    private final UserGiftCodeDAO userGiftCodeDAO;

    // Service
    private final GameDataService gameDataService;
    private final LeaderboardService leaderboardService;
    private final ClanService clanService;
    private final LoginRateLimiterService loginRateLimiterService;
    private final ConnectionBlockerService connectionBlockerService;

    // Manager
    private final SessionRegistry sessionRegistry;
    private final MessageSender messageSender;
    private final ExchangeLimitManager exchangeLimitManager;
    private final RoomManager roomManager;
    private final ServerManager serverManager;

    public AppContext() {
        serverConfig = new ServerConfig();
        hikariCPConfig = new HikariCPConfig();

        hikariCPManager = new HikariCPManager(hikariCPConfig);

        HikariCPManager db = hikariCPManager;
        accountDAO = new AccountDAO(db);
        captionLevelDAO = new CaptionLevelDAO(db);
        characterDAO = new CharacterDAO(db);
        clanDAO = new ClanDAO(db);
        clanShopDAO = new ClanShopDAO(db);
        equipmentDAO = new EquipmentDAO(db);
        experienceLevelDAO = new ExperienceLevelDAO(db);
        fabricateItemDAO = new FabricateItemDAO(db);
        fightItemDAO = new FightItemDAO(db);
        formulaDAO = new FormulaDAO(db);
        giftCodeDAO = new GiftCodeDAO(db);
        mapDAO = new MapDAO(db);
        missionDAO = new MissionDAO(db);
        paymentDAO = new PaymentDAO(db);
        rankingDAO = new RankingDAO(db);
        specialItemDAO = new SpecialItemDAO(db);
        userCharacterDAO = new UserCharacterDAO(db);
        userDAO = new UserDAO(db);
        userGiftCodeDAO = new UserGiftCodeDAO(db);

        gameDataService = new GameDataService(
                mapDAO,
                characterDAO,
                equipmentDAO,
                captionLevelDAO,
                fightItemDAO,
                clanShopDAO,
                specialItemDAO,
                formulaDAO,
                paymentDAO,
                missionDAO,
                experienceLevelDAO,
                fabricateItemDAO);
        leaderboardService = new LeaderboardService(rankingDAO);
        clanService = new ClanService(clanDAO);
        loginRateLimiterService = new LoginRateLimiterService();
        connectionBlockerService = new ConnectionBlockerService();

        sessionRegistry = new SessionRegistry(serverConfig);
        messageSender = new MessageSender(sessionRegistry);

        exchangeLimitManager = new ExchangeLimitManager();
        roomManager = new RoomManager();
        serverManager = new ServerManager(
                serverConfig,
                gameDataService,
                leaderboardService,
                roomManager,
                exchangeLimitManager,
                sessionRegistry);
    }
}
