package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.network.Impl.Message;

public interface IUserService {

    void handleLogin(Message ms);

    void handleLogout();

    void sendRoomName();

    void sendServerMessage(String message);

    void sendServerMessage2(String message);

    void handleHandshakeMessage();

    void giaHanDo(Message ms);

    void handleGetMissions(Message ms);

    void sendLoginSuccess();

    void gopClan(Message ms);

    void getVersionCode(Message ms);

    void getProvider(Message ms);

    void handleMergeEquipments(Message ms);

    void moHopQua(Message ms);

    void bangXepHang(Message ms);

    void handlePurchaseClanItem(Message ms);

    void luyenTap(Message ms);

    void handleLogout(Message ms);

    void handleSpecialItemShop(Message ms);

    void macTrangBiVip(Message ms);

    void handleSendMessage(Message ms);

    void handleSendRoomList();

    void handleEnteringRoom(Message ms);

    void handleJoinArea(Message ms);

    void handleChatMessage(Message ms);

    void handleKickPlayer(Message ms);

    void handleLeaveBoard(Message ms);

    void SanSang(Message ms);

    void hopNgoc(Message ms);

    void handleSetPasswordFightWait(Message ms);

    void handleSetMoneyFightWait(Message ms);

    void handleStartGame();

    void diChuyen(Message ms);

    void Bann(Message ms);

    void ketQUaBan(Message ms);

    void dungItem(Message ms);

    void handleJoinAnyBoard(Message ms);

    void handleViewFriendList();

    void handleAddFriend(Message ms);

    void handleRemoveFriend(Message ms);

    void handleGetFlayerDetail(Message ms);

    void handleFindPlayer(Message ms);

    void boLuot(Message ms);

    void capNhatXY(Message ms);

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

    void xoaDan(Message ms);

    void handleChangePassword(Message ms);

    void getFilePack(Message ms);

    void handleAddPoints(Message ms);

    void sendCharacterInfo();

    void handleChangeEquipment(Message ms);

    void handleSendShopEquipments();

    void handleEquipmentTransactions(Message ms);

    void handleSpinWheel(Message ms);

    void clanIcon(Message ms);

    void getTopClan(Message ms);

    void getInfoClan(Message ms);

    void getClanMember(Message ms);

    void getBigImage(Message ms);

    void handleRegister(Message ms);

    void napTien(Message ms);

    void getMaterialIconMessage(Message ms);

    void startLuyenTap(Message ms);

    void sendUpdateMoney();

    void sendUpdateDanhVong(int danhVongUp);

    void sendUpdateXp(int xpUp, boolean updateLevel);

    void ping(Message ms);
}
