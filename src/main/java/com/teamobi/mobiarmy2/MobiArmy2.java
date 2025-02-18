package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.config.IDatabaseConfig;
import com.teamobi.mobiarmy2.config.IRedisConfig;
import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.impl.HikariCPConfig;
import com.teamobi.mobiarmy2.config.impl.RedisConfig;
import com.teamobi.mobiarmy2.config.impl.ServerConfig;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dao.impl.*;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.*;
import com.teamobi.mobiarmy2.service.impl.*;
import com.teamobi.mobiarmy2.ui.ServerUI;

/**
 * @author tuyen
 */
public class MobiArmy2 {

    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        context.registerBean(IServerConfig.class, new ServerConfig());
        context.registerBean(IDatabaseConfig.class, new HikariCPConfig());
        context.registerBean(IRedisConfig.class, new RedisConfig());

        context.registerBean(IAccountDAO.class, new AccountDAO());
        context.registerBean(ICaptionLevelDAO.class, new CaptionLevelDAO());
        context.registerBean(ICharacterDAO.class, new CharacterDAO());
        context.registerBean(IClanDAO.class, new ClanDAO());
        context.registerBean(IClanShopDAO.class, new ClanShopDAO());
        context.registerBean(IEquipmentDAO.class, new EquipmentDAO());
        context.registerBean(IExperienceLevelDAO.class, new ExperienceLevelDAO());
        context.registerBean(IFabricateItemDAO.class, new FabricateItemDAO());
        context.registerBean(IFightItemDAO.class, new FightItemDAO());
        context.registerBean(IFormulaDAO.class, new FormulaDAO());
        context.registerBean(IGiftCodeDAO.class, new GiftCodeDAO());
        context.registerBean(IMapDAO.class, new MapDAO());
        context.registerBean(IMissionDAO.class, new MissionDAO());
        context.registerBean(IPaymentDAO.class, new PaymentDAO());
        context.registerBean(IRankingDAO.class, new RankingDAO(context.getBean(IServerConfig.class)));
        context.registerBean(ISpecialItemDAO.class, new SpecialItemDAO());
        context.registerBean(IUserCharacterDAO.class, new UserCharacterDAO());
        context.registerBean(IUserDAO.class, new UserDAO());
        context.registerBean(IUserGiftCodeDAO.class, new UserGiftCodeDAO());

        context.registerBean(IGameDataService.class, new GameDataService(
                context.getBean(IMapDAO.class),
                context.getBean(ICharacterDAO.class),
                context.getBean(IEquipmentDAO.class),
                context.getBean(ICaptionLevelDAO.class),
                context.getBean(IFightItemDAO.class),
                context.getBean(IClanShopDAO.class),
                context.getBean(ISpecialItemDAO.class),
                context.getBean(IFormulaDAO.class),
                context.getBean(IPaymentDAO.class),
                context.getBean(IMissionDAO.class),
                context.getBean(IExperienceLevelDAO.class),
                context.getBean(IFabricateItemDAO.class)
        ));
        context.registerBean(ILeaderboardService.class, new LeaderboardService(
                context.getBean(IRankingDAO.class),
                context.getBean(IServerConfig.class)
        ));
        context.registerBean(IClanService.class, new ClanService(
                context.getBean(IClanDAO.class)
        ));
        context.registerBean(ILoginRateLimiterService.class, new LoginRateLimiterService());
        context.registerBean(IConnectionBlockerService.class, new ConnectionBlockerService());

        ServerManager serverManager = ServerManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop, "ServerShutdownHook"));

        new Thread(() -> {
            serverManager.init();
            serverManager.start();
        }, "Main").start();

        new Thread(() -> ServerUI.launchUI(args), "ServerUI").start();
    }
}
