package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.BangXHManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserService implements IUserService {

    private User user;
    private final UserDao userDao = new UserDao();

    public UserService(User user) {
        this.user = user;
    }

    private void sendMessageLoginFail(String message) {
        try {
            Message ms = new Message(4);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendKeys() throws IOException {
        user.getSession().sendKeys();
    }

    @Override
    public void sendServerMessage(String message) {
        try {
            Message ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void giaHanDo(Message ms) {

    }

    @Override
    public void nhiemVuView(Message ms) {

    }

    @Override
    public void handleLogin(Message ms) {
        if (user.isLogged()) {
            return;
        }

        if (!BangXHManager.getInstance().isComplete) {
            sendMessageLoginFail(GameString.getNotFinishedLoadingRanking());
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            String username = dis.readUTF().trim();
            String password = dis.readUTF().trim();
            String version = dis.readUTF();

            if (!username.matches(CommonConstant.ALPHANUMERIC_PATTERN) || !password.matches(CommonConstant.ALPHANUMERIC_PATTERN)) {
                sendMessageLoginFail(GameString.reg_Error1());
                return;
            }

            ServerManager.getInstance().logger().logMessage("Client: " + user.getSession().getSessionId() + " name: " + username + " pass: " + password + " version: " + version);

            User userFound = userDao.findByUsernameAndPassword(username, password);
            if (userFound == null) {
                sendServerMessage(GameString.loginPassFail());
                return;
            }
            if (userFound.isLock()) {
                sendServerMessage(GameString.loginLock());
                return;
            }
            if (userFound.isActive()) {
                sendServerMessage(GameString.loginActive());
                return;
            }

            ISession session = user.getSession();
            session.setVersion(version);

            user = userFound;
            user.setSession(session);
            user.setLogged(true);

            sendLoginSuccess();
            ServerManager serverManager = ServerManager.getInstance();
            serverManager.sendNVData(user);
            serverManager.sendRoomInfo(user);
            serverManager.sendMapCollisionInfo(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLoginSuccess() {
        try {
            Message ms = new Message(Cmd.LOGIN_SUCESS);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getId());
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.writeByte(user.getNhanVat());
            ds.writeShort(user.getClanId());
            ds.writeByte(0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void gopClan(Message ms) {

    }

    @Override
    public void getVersionCode(Message ms) {
        try {
            String platform = ms.reader().readUTF();
            user.getSession().setPlatform(platform);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getProvider(Message ms) {
        try {
            byte provider = ms.reader().readByte();
            user.getSession().setProvider(provider);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
