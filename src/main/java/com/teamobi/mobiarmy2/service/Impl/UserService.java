package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IUserService;

public class UserService implements IUserService {

    private final User user;
    private final IUserDao userDao = new UserDao();

    public UserService(User user) {
        this.user = user;
    }


    @Override
    public void handleLogin(Message ms) {

    }

    @Override
    public void sendServerMessage(String ms) {

    }

    @Override
    public void handleHandshakeMessage() {
        user.getSession().sendKeys();
    }

    @Override
    public void giaHanDo(Message ms) {

    }

    @Override
    public void nhiemVuView(Message ms) {

    }

    @Override
    public void sendLoginSuccess() {

    }

    @Override
    public void gopClan(Message ms) {

    }

    @Override
    public void getVersionCode(Message ms) {

    }

    @Override
    public void getProvider(Message ms) {

    }

    @Override
    public void hopTrangBi(Message ms) {

    }

    @Override
    public void buyItem(Message ms) {

    }

    @Override
    public void moHopQua(Message ms) {

    }

    @Override
    public void bangXepHang(Message ms) {

    }

    @Override
    public void clanShop(Message ms) {

    }

    @Override
    public void luyenTap(Message ms) {

    }

    @Override
    public void dangXuat(Message ms) {

    }

    @Override
    public void doDacBietShop(Message ms) {

    }

    @Override
    public void macTrangBiVip(Message ms) {

    }

    @Override
    public void guiTinNhan(Message ms) {

    }

    @Override
    public void denKhuVuc(Message ms) {

    }

    @Override
    public void vaoPhong(Message ms) {

    }

    @Override
    public void thamGiaKhuVuc(Message ms) {

    }

    @Override
    public void nhanTinn(Message ms) {

    }

    @Override
    public void duoiNguoiCHoi(Message ms) {

    }

    @Override
    public void roiKhuVuc(Message ms) {

    }

    @Override
    public void SanSang(Message ms) {

    }

    @Override
    public void hopNgoc(Message ms) {

    }

    @Override
    public void datMatKhau(Message ms) {

    }

    @Override
    public void datCuoc(Message ms) {

    }

    @Override
    public void batDau(Message ms) {

    }

    @Override
    public void diChuyen(Message ms) {

    }

    @Override
    public void Bann(Message ms) {

    }

    @Override
    public void ketQUaBan(Message ms) {

    }

    @Override
    public void dungItem(Message ms) {

    }

    @Override
    public void choiNgay(Message ms) {

    }

    @Override
    public void xembanBe(Message ms) {

    }

    @Override
    public void ketBan(Message ms) {

    }

    @Override
    public void xoaBan(Message ms) {

    }

    @Override
    public void xemThongTIn(Message ms) {

    }

    @Override
    public void timNguoiChoi(Message ms) {

    }

    @Override
    public void boLuot(Message ms) {

    }

    @Override
    public void capNhatXY(Message ms) {

    }

    @Override
    public void datTenKhuVUc(Message ms) {

    }

    @Override
    public void datSoNguoi(Message ms) {

    }

    @Override
    public void mangItem(Message ms) {

    }

    @Override
    public void chonNhanVat(Message ms) {

    }

    @Override
    public void doiPhe(Message ms) {

    }

    @Override
    public void muaItem(Message ms) {

    }

    @Override
    public void muaNhanVat(Message ms) {

    }

    @Override
    public void chonBanDo(Message ms) {

    }

    @Override
    public void napTheCao(Message ms) {

    }

    @Override
    public void timBanChoi(Message ms) {

    }

    @Override
    public void xoaDan(Message ms) {

    }

    @Override
    public void doiMatKhau(Message ms) {

    }

    @Override
    public void layFilePack(Message ms) {

    }

    @Override
    public void nangCap(Message ms) {

    }

    @Override
    public void nangCap2(Message ms) {

    }

    @Override
    public void macTrangBi(Message ms) {

    }

    @Override
    public void shopTrangBi(Message ms) {

    }

    @Override
    public void muaTrangBi(Message ms) {

    }

    @Override
    public void quaySo(Message ms) {

    }

    @Override
    public void clanIcon(Message ms) {

    }

    @Override
    public void getTopClan(Message ms) {

    }

    @Override
    public void getInfoClan(Message ms) {

    }

    @Override
    public void getClanMember(Message ms) {

    }

    @Override
    public void getBigImage(Message ms) {

    }

    @Override
    public void register(Message ms) {

    }

    @Override
    public void napTien(Message ms) {

    }

    @Override
    public void getMaterialIconMessage(Message ms) {

    }

    @Override
    public void startLuyenTap(Message ms) {

    }
}
