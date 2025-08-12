package com.teamobi.mobiarmy2.bootstrap;

import com.teamobi.mobiarmy2.common.config.HikariCPConfig;
import com.teamobi.mobiarmy2.common.config.RedisConfig;
import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.server.HikariCPManager;
import com.teamobi.mobiarmy2.server.RedisConnectionManager;
import com.teamobi.mobiarmy2.server.RoomManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.ConnectionBlockerService;
import com.teamobi.mobiarmy2.service.GameDataService;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;

public class BeanRegistry {
    public static void registerBeans() {
        ApplicationContext context = ApplicationContext.getInstance();

        context.registerBean(ServerConfig.class, new ServerConfig());
        context.registerBean(HikariCPConfig.class, new HikariCPConfig());
        context.registerBean(RedisConfig.class, new RedisConfig());

        context.registerBean(HikariCPManager.class, new HikariCPManager(context.getBean(HikariCPConfig.class)));
        context.registerBean(RedisConnectionManager.class, new RedisConnectionManager(context.getBean(RedisConfig.class)));

        context.registerBean(AccountDAO.class, new AccountDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(CaptionLevelDAO.class, new CaptionLevelDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(CharacterDAO.class, new CharacterDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(ClanDAO.class, new ClanDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(ClanShopDAO.class, new ClanShopDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(EquipmentDAO.class, new EquipmentDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(ExperienceLevelDAO.class, new ExperienceLevelDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(FabricateItemDAO.class, new FabricateItemDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(FightItemDAO.class, new FightItemDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(FormulaDAO.class, new FormulaDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(GiftCodeDAO.class, new GiftCodeDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(MapDAO.class, new MapDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(MissionDAO.class, new MissionDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(PaymentDAO.class, new PaymentDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(RankingDAO.class, new RankingDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(SpecialItemDAO.class, new SpecialItemDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(UserCharacterDAO.class, new UserCharacterDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(UserDAO.class, new UserDAO(context.getBean(HikariCPManager.class)));
        context.registerBean(UserGiftCodeDAO.class, new UserGiftCodeDAO(context.getBean(HikariCPManager.class)));

        context.registerBean(GameDataService.class, new GameDataService(
                context.getBean(MapDAO.class),
                context.getBean(CharacterDAO.class),
                context.getBean(EquipmentDAO.class),
                context.getBean(CaptionLevelDAO.class),
                context.getBean(FightItemDAO.class),
                context.getBean(ClanShopDAO.class),
                context.getBean(SpecialItemDAO.class),
                context.getBean(FormulaDAO.class),
                context.getBean(PaymentDAO.class),
                context.getBean(MissionDAO.class),
                context.getBean(ExperienceLevelDAO.class),
                context.getBean(FabricateItemDAO.class)
        ));
        context.registerBean(LeaderboardService.class, new LeaderboardService(context.getBean(RankingDAO.class)));
        context.registerBean(LoginRateLimiterService.class, new LoginRateLimiterService(context.getBean(RedisConnectionManager.class)));
        context.registerBean(ConnectionBlockerService.class, new ConnectionBlockerService(context.getBean(RedisConnectionManager.class)));

        context.registerBean(RoomManager.class, new RoomManager());
        context.registerBean(ServerManager.class, new ServerManager(
                context.getBean(GameDataService.class),
                context.getBean(LeaderboardService.class),
                context.getBean(ConnectionBlockerService.class)
        ));
    }
}