package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.network.Impl.Message;

public interface IUserService {

    void handleLogin(Message ms);

    void handleLogout();

    void sendServerMessage(String message);

    void handleHandshakeMessage();

    void giaHanDo(Message ms);

    void nhiemVuView(Message ms);

    void sendLoginSuccess();

    void gopClan(Message ms);

    void getVersionCode(Message ms);

    void getProvider(Message ms);

    void hopTrangBi(Message ms);

    void buyItem(Message ms);

    void moHopQua(Message ms);

    void bangXepHang(Message ms);

    void clanShop(Message ms);

    void luyenTap(Message ms);

    void dangXuat(Message ms);

    void doDacBietShop(Message ms);

    void macTrangBiVip(Message ms);

    void guiTinNhan(Message ms);

    void denKhuVuc(Message ms);

    void vaoPhong(Message ms);

    void thamGiaKhuVuc(Message ms);

    void nhanTinn(Message ms);

    void duoiNguoiCHoi(Message ms);

    void roiKhuVuc(Message ms);

    void SanSang(Message ms);

    void hopNgoc(Message ms);

    void datMatKhau(Message ms);

    void datCuoc(Message ms);

    void batDau(Message ms);

    void diChuyen(Message ms);

    void Bann(Message ms);

    void ketQUaBan(Message ms);

    void dungItem(Message ms);

    void choiNgay(Message ms);

    void xembanBe(Message ms);

    void ketBan(Message ms);

    void xoaBan(Message ms);

    void xemThongTIn(Message ms);

    void timNguoiChoi(Message ms);

    void boLuot(Message ms);

    void capNhatXY(Message ms);

    void datTenKhuVUc(Message ms);

    void datSoNguoi(Message ms);

    void mangItem(Message ms);

    void handleChoseCharacter(Message ms);

    void doiPhe(Message ms);

    void muaItem(Message ms);

    void muaNhanVat(Message ms);

    void chonBanDo(Message ms);

    void napTheCao(Message ms);

    void timBanChoi(Message ms);

    void xoaDan(Message ms);

    void handleChangePassword(Message ms);

    void getFilePack(Message ms);

    void nangCap(Message ms);

    void sendCharacterInfo();

    void macTrangBi(Message ms);

    void shopTrangBi();

    void handleEquipmentPurchases(Message ms);

    void quaySo(Message ms);

    void clanIcon(Message ms);

    void getTopClan(Message ms);

    void getInfoClan(Message ms);

    void getClanMember(Message ms);

    void getBigImage(Message ms);

    void handleRegister(Message ms);

    void napTien(Message ms);

    void getMaterialIconMessage(Message ms);

    void startLuyenTap(Message ms);

    void sendMs10(String userLoginMany);

    void sendUpdateMoney();

    void sendUpdateDanhVong(int danhVongUp);

    void ping(Message ms);
}
