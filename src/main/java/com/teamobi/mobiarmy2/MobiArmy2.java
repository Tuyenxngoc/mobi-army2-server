package com.teamobi.mobiarmy2;

import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dao.impl.*;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IClanService;
import com.teamobi.mobiarmy2.service.impl.ClanService;
import com.teamobi.mobiarmy2.ui.ServerUI;

/**
 * @author tuyen
 */
public class MobiArmy2 {

    public static void main(String[] args) {
        ApplicationContext context = ApplicationContext.getInstance();
        context.registerBean(IAccountDAO.class, new AccountDAO());
        context.registerBean(ICaptionLevelDAO.class, new CaptionLevelDAO());
        context.registerBean(ICharacterDAO.class, new CharacterDAO());
        context.registerBean(IClanDAO.class, new ClanDAO());
        context.registerBean(IClanItemDAO.class, new ClanItemDAO());
        context.registerBean(IClanMemberDAO.class, new ClanMemberDAO());
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
        context.registerBean(IPlayerDAO.class, new PlayerDAO());
        context.registerBean(IPlayerEquipDAO.class, new PlayerEquipDAO());
        context.registerBean(IPlayerSpecialItemDAO.class, new PlayerSpecialItemDAO());
        context.registerBean(IRankingDAO.class, new RankingDAO());
        context.registerBean(ISpecialItemDAO.class, new SpecialItemDAO());
        context.registerBean(IUserCharacterDAO.class, new UserCharacterDAO());
        context.registerBean(IUserDAO.class, new UserDAO());
        context.registerBean(IUserEquipmentDAO.class, new UserEquipmentDAO());
        context.registerBean(IUserFriendDAO.class, new UserFriendDAO());
        context.registerBean(IUserGiftCodeDAO.class, new UserGiftCodeDAO());
        context.registerBean(IUserSpecialItemDAO.class, new UserSpecialItemDAO());
        context.registerBean(IClanService.class, new ClanService(context.getBean(IClanDAO.class), context.getBean(IClanItemDAO.class), context.getBean(IClanMemberDAO.class)));

        ServerManager serverManager = ServerManager.getInstance();
        Runtime.getRuntime().addShutdownHook(new Thread(serverManager::stop, "ServerShutdownHook"));

        new Thread(() -> {
            serverManager.init();
            serverManager.start();
        }, "Main").start();

        new Thread(() -> ServerUI.launchUI(args), "ServerUI").start();
    }
}
