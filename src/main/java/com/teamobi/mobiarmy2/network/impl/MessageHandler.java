package com.teamobi.mobiarmy2.network.impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.IMessageHandler;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;

/**
 * @author tuyen
 */
public class MessageHandler implements IMessageHandler {

    private final IUserService userService;

    public MessageHandler(IUserService userService) {
        this.userService = userService;
    }

    @Override
    public void onMessage(IMessage ms) {
        try {
            switch (ms.getCommand()) {
                case Cmd.MORE_GAME -> userService.getMoreGame();

                case Cmd.GET_KEY -> userService.handleHandshakeMessage();

                case Cmd.GET_AGENT_PROVIDER -> userService.handleSendAgentAndProviders();

                case Cmd.GET_MORE_DAY -> userService.extendItemDuration(ms);

                case Cmd.MISSISON -> userService.handleGetMissions(ms);

                case Cmd.CLAN_MONEY -> userService.contributeToClan(ms);

                case Cmd.CHANGE_ROOM_NAME -> userService.sendRoomName();

                case Cmd.FOMULA -> userService.handleMergeEquipments(ms);

                case Cmd.GET_LUCKYGIFT -> userService.openLuckyGift(ms);

                case Cmd.BANGTHANHTICH -> userService.viewLeaderboard(ms);

                case Cmd.SHOP_BIETDOI -> userService.handlePurchaseClanItem(ms);

                case Cmd.TRAINING_MAP -> userService.enterTrainingMap();

                case Cmd.SIGN_OUT -> userService.handleLogout(ms);

                case Cmd.SHOP_LINHTINH -> userService.handleSpecialItemShop(ms);

                case Cmd.VIP_EQUIP -> userService.equipVipItems(ms);

                case Cmd.LOGIN -> userService.handleLogin(ms);

                case Cmd.CHAT_TO -> userService.handleSendMessage(ms);

                case Cmd.ROOM_LIST -> userService.handleSendRoomList();

                case Cmd.BOARD_LIST -> userService.handleEnteringRoom(ms);

                case Cmd.JOIN_BOARD -> userService.handleJoinBoard(ms);

                case Cmd.CHAT_TO_BOARD -> userService.handleChatMessage(ms);

                case Cmd.KICK -> userService.handleKickPlayer(ms);

                case Cmd.LEAVE_BOARD -> userService.handleLeaveBoard(ms);

                case Cmd.READY -> userService.setReady(ms);

                case Cmd.IMBUE -> userService.imbueGem(ms);

                case Cmd.SET_PASS -> userService.handleSetPasswordFightWait(ms);

                case Cmd.SET_MONEY -> userService.handleSetMoneyFightWait(ms);

                case Cmd.START_ARMY -> userService.handleStartGame();

                case Cmd.MOVE_ARMY -> userService.movePlayer(ms);

                case Cmd.FIRE_ARMY -> userService.shoot(ms);

                case Cmd.SHOOT_RESULT -> userService.processShootingResult(ms);

                case Cmd.USE_ITEM -> userService.handleUseItem(ms);

                case Cmd.JOIN_ANY_BOARD -> userService.handleJoinAnyBoard(ms);

                case Cmd.REQUEST_FRIENDLIST -> userService.handleViewFriendList();

                case Cmd.ADD_FRIEND -> userService.handleAddFriend(ms);

                case Cmd.DELETE_FRIEND -> userService.handleRemoveFriend(ms);

                case Cmd.PLAYER_DETAIL -> userService.handleGetFlayerDetail(ms);

                case Cmd.SEARCH -> userService.handleFindPlayer(ms);

                case Cmd.PING -> userService.ping(ms);

                case Cmd.SKIP -> userService.skipTurn();

                case Cmd.UPDATE_XY -> userService.updateCoordinates(ms);

                case Cmd.SET_BOARD_NAME -> userService.handleSetFightWaitName(ms);

                case Cmd.SET_MAX_PLAYER -> userService.handleSetMaxPlayerFightWait(ms);

                case Cmd.SET_PROVIDER -> userService.getProvider(ms);

                case Cmd.CHOOSE_ITEM -> userService.handleChoseItemFight(ms);

                case Cmd.CHOOSE_GUN -> userService.handleChoseCharacter(ms);

                case Cmd.CHANGE_TEAM -> userService.handleChangeTeam(ms);

                case Cmd.BUY_ITEM -> userService.handlePurchaseItem(ms);

                case Cmd.BUY_GUN -> userService.handleBuyCharacter(ms);

                case Cmd.MAP_SELECT -> userService.handleSelectMap(ms);

                case Cmd.LOAD_CARD -> userService.handleCardRecharge(ms);

                case Cmd.FIND_PLAYER -> userService.handleFindPlayerWait(ms);

                case Cmd.CHECK_CROSS -> userService.clearBullet(ms);

                case Cmd.CHANGE_PASS -> userService.handleChangePassword(ms);

                case Cmd.TRAINING -> userService.startTraining(ms);

                case Cmd.TRAININGSHOOT -> userService.trainShooting(ms);

                case Cmd.GET_FILEPACK -> userService.getFilePack(ms);

                case Cmd.ADD_POINT -> userService.handleAddPoints(ms);

                case Cmd.CHARACTOR_INFO -> userService.sendCharacterInfo();

                case Cmd.CHANGE_EQUIP -> userService.handleChangeEquipment(ms);

                case Cmd.SHOP_EQUIP -> userService.handleSendShopEquipments();

                case Cmd.BUY_EQUIP -> userService.handleEquipmentTransactions(ms);

                case Cmd.RULET -> userService.handleSpinWheel(ms);

                case Cmd.VERSION_CODE -> userService.getVersionCode(ms);

                case Cmd.CLAN_ICON -> userService.getClanIcon(ms);

                case Cmd.TOP_CLAN -> userService.getTopClan(ms);

                case Cmd.CLAN_INFO -> userService.getInfoClan(ms);

                case Cmd.CLAN_MEMBER -> userService.getClanMember(ms);

                case Cmd.GET_BIG_IMAGE -> userService.getBigImage(ms);

                case Cmd.REGISTER_2 -> userService.handleRegister(ms);

                case Cmd.CHARGE_MONEY_2 -> userService.rechargeMoney(ms);

                case Cmd.MATERIAL_ICON -> userService.getMaterialIconMessage(ms);

                case Cmd.GETSTRING -> userService.getStringMessage(ms);

                default ->
                        ServerManager.getInstance().logger().logWarning("Command " + ms.getCommand() + " is not supported");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
