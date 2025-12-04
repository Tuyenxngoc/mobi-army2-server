package com.teamobi.mobiarmy2.network;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.handler.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageRouter {
    @Getter
    private final AuthMessageHandler authMessageHandler;
    @Setter
    private ClanMessageHandler clanMessageHandler;
    @Setter
    private FriendMessageHandler friendMessageHandler;
    @Setter
    private ShopMessageHandler shopMessageHandler;
    @Setter
    private ResourceMessageHandler resourceMessageHandler;
    @Setter
    private MissionMessageHandler missionMessageHandler;
    @Setter
    private FormulaMessageHandler formulaMessageHandler;
    @Setter
    private RoomMessageHandler roomMessageHandler;
    @Setter
    private FightWaitMessageHandler fightWaitMessageHandler;
    @Setter
    private FightManagerMessageHandler fightManagerMessageHandler;
    @Setter
    private InventoryMessageHandler inventoryMessageHandler;
    @Setter
    private LeaderboardMessageHandler leaderboardMessageHandler;
    @Setter
    private SpinMessageHandler spinMessageHandler;
    @Setter
    private PaymentMessageHandler paymentMessageHandler;
    @Setter
    private CharacterMessageHandler characterMessageHandler;

    public MessageRouter(AuthMessageHandler authMessageHandler) {
        this.authMessageHandler = authMessageHandler;
    }

    public void onMessage(Message ms) {
        try {
            switch (ms.getCommand()) {
                case Cmd.MORE_GAME -> resourceMessageHandler.getMoreGame();

                case Cmd.GET_AGENT_PROVIDER -> authMessageHandler.handleSendAgentAndProviders();

                case Cmd.GET_MORE_DAY -> inventoryMessageHandler.extendItemDuration(ms);

                case Cmd.MISSISON -> missionMessageHandler.handleGetMissions(ms);

                case Cmd.CLAN_MONEY -> clanMessageHandler.contributeToClan(ms);

                case Cmd.CHANGE_ROOM_NAME -> roomMessageHandler.sendRoomName();

                case Cmd.FOMULA -> formulaMessageHandler.handleMergeEquipments(ms);

                case Cmd.GET_LUCKYGIFT -> fightWaitMessageHandler.openLuckyGift(ms);

                case Cmd.BANGTHANHTICH -> leaderboardMessageHandler.viewLeaderboard(ms);

                case Cmd.SHOP_BIETDOI -> clanMessageHandler.handlePurchaseClanItem(ms);

                case Cmd.TRAINING_MAP -> fightManagerMessageHandler.enterTrainingMap();

                case Cmd.SIGN_OUT -> authMessageHandler.handleLogout();

                case Cmd.SHOP_LINHTINH -> shopMessageHandler.handleSpecialItemShop(ms);

                case Cmd.VIP_EQUIP -> inventoryMessageHandler.equipVipItems(ms);

                case Cmd.LOGIN -> authMessageHandler.handleLogin(ms);

                case Cmd.CHAT_TO -> friendMessageHandler.handleSendMessage(ms);

                case Cmd.ROOM_LIST -> roomMessageHandler.handleSendRoomList();

                case Cmd.BOARD_LIST -> roomMessageHandler.handleEnteringRoom(ms);

                case Cmd.JOIN_BOARD -> roomMessageHandler.handleJoinBoard(ms);

                case Cmd.CHAT_TO_BOARD -> fightWaitMessageHandler.handleChatMessage(ms);

                case Cmd.KICK -> fightWaitMessageHandler.handleKickPlayer(ms);

                case Cmd.LEAVE_BOARD -> roomMessageHandler.handleLeaveBoard();

                case Cmd.READY -> fightWaitMessageHandler.setReady(ms);

                case Cmd.IMBUE -> inventoryMessageHandler.imbueGem(ms);

                case Cmd.SET_PASS -> fightWaitMessageHandler.handleSetPasswordFightWait(ms);

                case Cmd.SET_MONEY -> fightWaitMessageHandler.handleSetMoneyFightWait(ms);

                case Cmd.START_ARMY -> fightWaitMessageHandler.handleStartGame();

                case Cmd.MOVE_ARMY -> fightManagerMessageHandler.movePlayer(ms);

                case Cmd.FIRE_ARMY -> fightManagerMessageHandler.handleShot(ms);

                case Cmd.SHOOT_RESULT -> fightManagerMessageHandler.processShootingResult();

                case Cmd.USE_ITEM -> fightManagerMessageHandler.handleUseItem(ms);

                case Cmd.JOIN_ANY_BOARD -> roomMessageHandler.handleJoinAnyBoard(ms);

                case Cmd.REQUEST_FRIENDLIST -> friendMessageHandler.handleViewFriendList();

                case Cmd.ADD_FRIEND -> friendMessageHandler.handleAddFriend(ms);

                case Cmd.DELETE_FRIEND -> friendMessageHandler.handleRemoveFriend(ms);

                case Cmd.PLAYER_DETAIL -> fightWaitMessageHandler.handleGetFlayerDetail(ms);

                case Cmd.SEARCH -> friendMessageHandler.handleFindPlayer(ms);

                case Cmd.PING -> authMessageHandler.ping(ms);

                case Cmd.SKIP -> fightManagerMessageHandler.skipTurn();

                case Cmd.UPDATE_XY -> fightManagerMessageHandler.updateCoordinates(ms);

                case Cmd.SET_BOARD_NAME -> fightWaitMessageHandler.handleSetFightWaitName(ms);

                case Cmd.SET_MAX_PLAYER -> fightWaitMessageHandler.handleSetMaxPlayerFightWait(ms);

                case Cmd.SET_PROVIDER -> authMessageHandler.getProvider(ms);

                case Cmd.CHOOSE_ITEM -> fightWaitMessageHandler.handleChoseItemFight(ms);

                case Cmd.CHOOSE_GUN -> characterMessageHandler.handleChoseCharacter(ms);

                case Cmd.CHANGE_TEAM -> fightWaitMessageHandler.handleChangeTeam();

                case Cmd.BUY_ITEM -> shopMessageHandler.handlePurchaseItem(ms);

                case Cmd.BUY_GUN -> shopMessageHandler.handleBuyCharacter(ms);

                case Cmd.MAP_SELECT -> fightWaitMessageHandler.handleSelectMap(ms);

                case Cmd.LOAD_CARD -> paymentMessageHandler.handleCardRecharge(ms);

                case Cmd.FIND_PLAYER -> fightWaitMessageHandler.handleFindPlayerWait(ms);

                case Cmd.CHECK_CROSS -> fightManagerMessageHandler.clearBullet(ms);

                case Cmd.CHANGE_PASS -> authMessageHandler.handleChangePassword(ms);

                case Cmd.TRAINING -> fightManagerMessageHandler.startTraining(ms);

                case Cmd.TRAININGSHOOT -> fightManagerMessageHandler.trainShooting(ms);

                case Cmd.GET_FILEPACK -> resourceMessageHandler.getFilePack(ms);

                case Cmd.ADD_POINT -> characterMessageHandler.handleAddPoints(ms);

                case Cmd.CHARACTOR_INFO -> characterMessageHandler.handleGetCharacterInfo();

                case Cmd.CHANGE_EQUIP -> inventoryMessageHandler.handleChangeEquipment(ms);

                case Cmd.SHOP_EQUIP -> shopMessageHandler.handleSendShopEquipments();

                case Cmd.BUY_EQUIP -> inventoryMessageHandler.handleEquipmentTransactions(ms);

                case Cmd.RULET -> spinMessageHandler.handleSpinWheel(ms);

                case Cmd.VERSION_CODE -> authMessageHandler.getVersionCode(ms);

                case Cmd.CLAN_ICON -> clanMessageHandler.getClanIcon(ms);

                case Cmd.TOP_CLAN -> clanMessageHandler.getTopClan(ms);

                case Cmd.CLAN_INFO -> clanMessageHandler.getInfoClan(ms);

                case Cmd.CLAN_MEMBER -> clanMessageHandler.getClanMember(ms);

                case Cmd.GET_BIG_IMAGE -> resourceMessageHandler.getBigImage(ms);

                case Cmd.REGISTER_2 -> authMessageHandler.handleRegister(ms);

                case Cmd.CHARGE_MONEY_2 -> paymentMessageHandler.rechargeMoney(ms);

                case Cmd.MATERIAL_ICON -> resourceMessageHandler.getMaterialIconMessage(ms);

                case Cmd.GETSTRING -> authMessageHandler.getAgent(ms);

                default -> log.warn("Command {} is not supported", ms.getCommand());
            }
        } catch (Exception e) {
            log.error("Error processing command {}: {}", ms.getCommand(), e.getMessage(), e);
        }
    }
}
