package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.config.impl.ServerConfig;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dao.impl.*;
import com.teamobi.mobiarmy2.server.ApplicationContext;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IClanService;
import com.teamobi.mobiarmy2.service.IGameDataService;
import com.teamobi.mobiarmy2.service.ILeaderboardService;
import com.teamobi.mobiarmy2.service.impl.ClanService;
import com.teamobi.mobiarmy2.service.impl.GameDataService;
import com.teamobi.mobiarmy2.service.impl.LeaderboardService;
import com.teamobi.mobiarmy2.ui.ServerUI;

/**
 * @author tuyen
 */
public class MobiArmy2 {

    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        context.registerBean(IServerConfig.class, new ServerConfig());

        context.registerBean(IClanDao.class, new ClanDao());
        context.registerBean(IGameDao.class, new GameDao());
        context.registerBean(IGiftCodeDao.class, new GiftCodeDao());
        context.registerBean(IRankingDao.class, new RankingDao(context.getBean(IServerConfig.class)));
        context.registerBean(IUserDao.class, new UserDao());

        context.registerBean(IGameDataService.class, new GameDataService(context.getBean(IGameDao.class)));
        context.registerBean(ILeaderboardService.class, new LeaderboardService(context.getBean(IRankingDao.class), context.getBean(IServerConfig.class)));
        context.registerBean(IClanService.class, new ClanService(context.getBean(IClanDao.class)));

        ServerManager serverManager = ServerManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop, "ServerShutdownHook"));

        new Thread(() -> {
            serverManager.init();
            serverManager.start();
        }, "Main").start();

        new Thread(() -> ServerUI.launchUI(args), "ServerUI").start();
    }
}
