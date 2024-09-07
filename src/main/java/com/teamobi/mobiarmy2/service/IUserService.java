package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.network.IMessage;

public interface IUserService {

    void handleLogin(IMessage ms);

    void handleLogout();

    void sendRoomName();

    void sendServerMessage(String message);

    void sendServerMessage2(String message);

    void handleHandshakeMessage();

    void extendItemDuration(IMessage ms);

    void handleGetMissions(IMessage ms);

    void sendLoginSuccess();

    void contributeToClan(IMessage ms);

    void getVersionCode(IMessage ms);

    void getProvider(IMessage ms);

    void handleMergeEquipments(IMessage ms);

    void openLuckyGift(IMessage ms);

    void viewLeaderboard(IMessage ms);

    void handlePurchaseClanItem(IMessage ms);

    void enterTrainingMap();

    void handleLogout(IMessage ms);

    void handleSpecialItemShop(IMessage ms);

    void equipVipItems(IMessage ms);

    void handleSendMessage(IMessage ms);

    void handleSendRoomList();

    void handleEnteringRoom(IMessage ms);

    void handleJoinBoard(IMessage ms);

    void handleChatMessage(IMessage ms);

    void handleKickPlayer(IMessage ms);

    void handleLeaveBoard(IMessage ms);

    void setReady(IMessage ms);

    void imbueGem(IMessage ms);

    void handleSetPasswordFightWait(IMessage ms);

    void handleSetMoneyFightWait(IMessage ms);

    void handleStartGame();

    void movePlayer(IMessage ms);

    void shoot(IMessage ms);

    void processShootingResult(IMessage ms);

    void handleUseItem(IMessage ms);

    void handleJoinAnyBoard(IMessage ms);

    void handleViewFriendList();

    void handleAddFriend(IMessage ms);

    void handleRemoveFriend(IMessage ms);

    void handleGetFlayerDetail(IMessage ms);

    void handleFindPlayer(IMessage ms);

    void skipTurn();

    void updateCoordinates(IMessage ms);

    void handleSetFightWaitName(IMessage ms);

    void handleSetMaxPlayerFightWait(IMessage ms);

    void handleChoseItemFight(IMessage ms);

    void handleChoseCharacter(IMessage ms);

    void handleChangeTeam(IMessage ms);

    void handlePurchaseItem(IMessage ms);

    void handleBuyCharacter(IMessage ms);

    void handleSelectMap(IMessage ms);

    void handleCardRecharge(IMessage ms);

    void handleFindPlayerWait(IMessage ms);

    void clearBullet(IMessage ms);

    void handleChangePassword(IMessage ms);

    void getFilePack(IMessage ms);

    void handleAddPoints(IMessage ms);

    void sendCharacterInfo();

    void handleChangeEquipment(IMessage ms);

    void handleSendShopEquipments();

    void handleEquipmentTransactions(IMessage ms);

    void handleSpinWheel(IMessage ms);

    void getClanIcon(IMessage ms);

    void getTopClan(IMessage ms);

    void getInfoClan(IMessage ms);

    void getClanMember(IMessage ms);

    void getBigImage(IMessage ms);

    void handleRegister(IMessage ms);

    void rechargeMoney(IMessage ms);

    void getMaterialIconMessage(IMessage ms);

    void startTraining(IMessage ms);

    void trainShooting(IMessage ms);

    void sendUpdateMoney();

    void sendUpdateCup(int cupUp);

    void sendUpdateXp(int xpUp, boolean updateLevel);

    void ping(IMessage ms);

    void getMoreGame();
}
