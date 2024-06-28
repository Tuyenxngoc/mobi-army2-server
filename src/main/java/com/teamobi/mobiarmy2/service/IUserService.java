package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.network.Impl.Message;

public interface IUserService {

    void handleLogin(Message ms);

    void handleLogout();

    void sendRoomName();

    void sendServerMessage(String message);

    void sendServerMessage2(String message);

    void handleHandshakeMessage();

    void extendItemDuration(Message ms);

    void handleGetMissions(Message ms);

    void sendLoginSuccess();

    void contributeToClan(Message ms);

    void getVersionCode(Message ms);

    void getProvider(Message ms);

    void handleMergeEquipments(Message ms);

    void openLuckyGift(Message ms);

    void viewLeaderboard(Message ms);

    void handlePurchaseClanItem(Message ms);

    void enterTrainingMap(Message ms);

    void handleLogout(Message ms);

    void handleSpecialItemShop(Message ms);

    void equipVipItems(Message ms);

    void handleSendMessage(Message ms);

    void handleSendRoomList();

    void handleEnteringRoom(Message ms);

    void handleJoinArea(Message ms);

    void handleChatMessage(Message ms);

    void handleKickPlayer(Message ms);

    void handleLeaveBoard(Message ms);

    void setReady(Message ms);

    void imbueGem(Message ms);

    void handleSetPasswordFightWait(Message ms);

    void handleSetMoneyFightWait(Message ms);

    void handleStartGame();

    void movePlayer(Message ms);

    void shoot(Message ms);

    void processShootingResult(Message ms);

    void handleUseItem(Message ms);

    void handleJoinAnyBoard(Message ms);

    void handleViewFriendList();

    void handleAddFriend(Message ms);

    void handleRemoveFriend(Message ms);

    void handleGetFlayerDetail(Message ms);

    void handleFindPlayer(Message ms);

    void skipTurn(Message ms);

    void updateCoordinates(Message ms);

    void handleSetFightWaitName(Message ms);

    void handleSetMaxPlayerFightWait(Message ms);

    void handleChoseItemFight(Message ms);

    void handleChoseCharacter(Message ms);

    void handleChangeTeam(Message ms);

    void handlePurchaseItem(Message ms);

    void handleBuyCharacter(Message ms);

    void handleSelectMap(Message ms);

    void handleCardRecharge(Message ms);

    void handleFindPlayerWait(Message ms);

    void clearBullet(Message ms);

    void handleChangePassword(Message ms);

    void getFilePack(Message ms);

    void handleAddPoints(Message ms);

    void sendCharacterInfo();

    void handleChangeEquipment(Message ms);

    void handleSendShopEquipments();

    void handleEquipmentTransactions(Message ms);

    void handleSpinWheel(Message ms);

    void getClanIcon(Message ms);

    void getTopClan(Message ms);

    void getInfoClan(Message ms);

    void getClanMember(Message ms);

    void getBigImage(Message ms);

    void handleRegister(Message ms);

    void rechargeMoney(Message ms);

    void getMaterialIconMessage(Message ms);

    void startTraining(Message ms);

    void sendUpdateMoney();

    void sendUpdateDanhVong(int danhVongUp);

    void sendUpdateXp(int xpUp, boolean updateLevel);

    void ping(Message ms);
}
