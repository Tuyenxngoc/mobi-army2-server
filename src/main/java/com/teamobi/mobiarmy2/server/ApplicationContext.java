package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.HikariCPConfig;
import com.teamobi.mobiarmy2.config.RedisConfig;
import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.service.*;

import java.util.HashMap;
import java.util.Map;

public class ApplicationContext {
    private final Map<Class<?>, Object> beans = new HashMap<>();

    private ApplicationContext() {
        registerBean(ServerConfig.class, new ServerConfig());
        registerBean(HikariCPConfig.class, new HikariCPConfig());
        registerBean(RedisConfig.class, new RedisConfig());

        registerBean(HikariCPManager.class, new HikariCPManager(getBean(HikariCPConfig.class)));
        registerBean(RedisConnectionManager.class, new RedisConnectionManager(getBean(RedisConfig.class)));

        registerBean(AccountDAO.class, new AccountDAO(getBean(HikariCPManager.class)));
        registerBean(CaptionLevelDAO.class, new CaptionLevelDAO(getBean(HikariCPManager.class)));
        registerBean(CharacterDAO.class, new CharacterDAO(getBean(HikariCPManager.class)));
        registerBean(ClanDAO.class, new ClanDAO(getBean(HikariCPManager.class)));
        registerBean(ClanShopDAO.class, new ClanShopDAO(getBean(HikariCPManager.class)));
        registerBean(EquipmentDAO.class, new EquipmentDAO(getBean(HikariCPManager.class)));
        registerBean(ExperienceLevelDAO.class, new ExperienceLevelDAO(getBean(HikariCPManager.class)));
        registerBean(FabricateItemDAO.class, new FabricateItemDAO(getBean(HikariCPManager.class)));
        registerBean(FightItemDAO.class, new FightItemDAO(getBean(HikariCPManager.class)));
        registerBean(FormulaDAO.class, new FormulaDAO(getBean(HikariCPManager.class)));
        registerBean(GiftCodeDAO.class, new GiftCodeDAO(getBean(HikariCPManager.class)));
        registerBean(MapDAO.class, new MapDAO(getBean(HikariCPManager.class)));
        registerBean(MissionDAO.class, new MissionDAO(getBean(HikariCPManager.class)));
        registerBean(PaymentDAO.class, new PaymentDAO(getBean(HikariCPManager.class)));
        registerBean(RankingDAO.class, new RankingDAO(getBean(HikariCPManager.class)));
        registerBean(SpecialItemDAO.class, new SpecialItemDAO(getBean(HikariCPManager.class)));
        registerBean(UserCharacterDAO.class, new UserCharacterDAO(getBean(HikariCPManager.class)));
        registerBean(UserDAO.class, new UserDAO(getBean(HikariCPManager.class)));
        registerBean(UserGiftCodeDAO.class, new UserGiftCodeDAO(getBean(HikariCPManager.class)));

        registerBean(GameDataService.class, new GameDataService(
                getBean(MapDAO.class),
                getBean(CharacterDAO.class),
                getBean(EquipmentDAO.class),
                getBean(CaptionLevelDAO.class),
                getBean(FightItemDAO.class),
                getBean(ClanShopDAO.class),
                getBean(SpecialItemDAO.class),
                getBean(FormulaDAO.class),
                getBean(PaymentDAO.class),
                getBean(MissionDAO.class),
                getBean(ExperienceLevelDAO.class),
                getBean(FabricateItemDAO.class)
        ));
        registerBean(LeaderboardService.class, new LeaderboardService(getBean(RankingDAO.class)));
        registerBean(ClanService.class, new ClanService(getBean(ClanDAO.class)));
        registerBean(LoginRateLimiterService.class, new LoginRateLimiterService(getBean(RedisConnectionManager.class)));
        registerBean(ConnectionBlockerService.class, new ConnectionBlockerService(getBean(RedisConnectionManager.class)));

        registerBean(RoomManager.class, new RoomManager());
        registerBean(ServerManager.class, new ServerManager(
                getBean(GameDataService.class),
                getBean(LeaderboardService.class),
                getBean(ConnectionBlockerService.class)
        ));
    }

    public static ApplicationContext getInstance() {
        return ApplicationContext.SingletonHelper.INSTANCE;
    }

    public <T> void registerBean(Class<T> type, T instance) {
        beans.put(type, instance);
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(Class<T> clazz) {
        return (T) beans.get(clazz);
    }

    public void clearDependencies() {
        beans.clear();
    }

    private static class SingletonHelper {
        private static final ApplicationContext INSTANCE = new ApplicationContext();
    }
}