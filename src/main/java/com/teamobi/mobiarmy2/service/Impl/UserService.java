package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.NVData;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.BangXHManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.util.Until;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UserService implements IUserService {

    private final User user;
    private final IUserDao userDao = new UserDao();

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

            User userFound = userDao.findByUsernameAndPassword(username, password);
            if (userFound == null) {
                sendServerMessage(GameString.loginPassFail());
                return;
            }
            if (userFound.isLock()) {
                sendServerMessage(GameString.loginLock());
                return;
            }
            if (!userFound.isActive()) {
                sendServerMessage(GameString.loginActive());
                return;
            }

            ServerManager serverManager = ServerManager.getInstance();

            //Kiểm tra có đang đăng nhập hay không
            User userLogin = serverManager.getUser(userFound.getId());
            if (userLogin != null) {
                userLogin.getUserService().sendMs10(GameString.userLoginMany());
                userLogin.getSession().close();

                sendMessageLoginFail(GameString.loginErr1());
                return;
            }

            user.setId(userFound.getId());
            user.setUsername(userFound.getUsername());
            user.setPassword(userFound.getPassword());
            user.setXu(userFound.getXu());
            user.setLuong(userFound.getLuong());
            user.setDanhVong(userFound.getDanhVong());
            user.setRuongDoItem(userFound.getRuongDoItem());
            user.setRuongDoTB(userFound.getRuongDoTB());

            user.getSession().setVersion(version);
            user.setLogged(true);

            userDao.updateOnline(true, userFound.getId());

            sendLoginSuccess();
            IServerConfig config = serverManager.config();
            sendNVData(config);
            sendRoomInfo(config);
            sendMapCollisionInfo(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNVData(IServerConfig config) {
        try {
            Message ms = new Message(Cmd.ANTI_HACK_MESS);
            DataOutputStream ds = ms.writer();
            ArrayList<NVData.NVEntry> entries = NVData.entrys;
            int len = entries.size();
            ds.writeByte(len);
            // Ma sat gio cac nv
            for (NVData.NVEntry entry : entries) {
                ds.writeByte(entry.ma_sat_gio);
            }
            // Goc cuu tieu
            ds.writeByte(len);
            for (NVData.NVEntry entry : entries) {
                ds.writeShort(entry.goc_min);
            }
            // Sat thuong 1 vien dan
            ds.writeByte(len);
            for (NVData.NVEntry nvEntry : entries) {
                ds.writeByte(nvEntry.sat_thuong_dan);
            }
            // So dan
            ds.writeByte(len);
            for (NVData.NVEntry nvdata : entries) {
                ds.writeByte(nvdata.so_dan);
            }
            // Max player
            ds.writeByte(config.getMaxPlayerFight());
            // Map boss
            ds.writeByte(config.getNumMapBoss());
            for (int i = 0; i < config.getNumMapBoss(); i++) {
                ds.writeByte(config.getStartMapBoss() + i);
            }
            // Type map boss
            for (int i = 0; i < config.getNumMapBoss(); i++) {
                ds.writeByte(config.getMapIdBoss()[i]);
            }
            // NUMB Player
            ds.writeByte(config.getNumbPlayers());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRoomInfo(IServerConfig config) {
        sendRoomCaption(config);
        sendRoomName(config);
    }

    private void sendRoomName(IServerConfig config) {

    }

    private void sendRoomCaption(IServerConfig config) {
        try {
            Message ms = new Message(Cmd.ROOM_CAPTION);
            DataOutputStream ds = ms.writer();
            ds.writeByte(config.getRoomTypes().length);
            for (int i = 0; i < config.getRoomTypes().length; i++) {
                ds.writeUTF(config.getRoomTypes()[i]);
                ds.writeUTF(config.getRoomTypesEng()[i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMapCollisionInfo(IServerConfig config) {
        try {
            // Send mss 92
            Message ms = new Message(92);
            DataOutputStream ds = ms.writer();
            ds.writeShort(MapData.idNotCollisions.length);
            for (int i = 0; i < MapData.idNotCollisions.length; i++) {
                ds.writeShort(MapData.idNotCollisions[i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            Message ms = new Message(Cmd.LOGIN_SUCESS);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getId());
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.writeByte(user.getNhanVat());
            ds.writeShort(user.getClanId());
            ds.writeByte(0);

            // Trang bị
            for (int i = 0; i < 10; i++) {
                ds.writeBoolean(false);
                for (int j = 0; j < 5; j++) {
                    ds.writeShort(-1);
                }
            }

            //Item
            for (int i = 0; i < 36; i++) {
                ds.writeByte(99);
                ds.writeInt(1);
                ds.writeInt(1);
            }

            //Nhan vat
            for (int i = 0; i < 10; i++) {
                if (i > 2) {
                    ds.writeByte(0);
                    ds.writeShort(1);
                    ds.writeShort(1);
                }
            }

            IServerConfig config = ServerManager.getInstance().config();
            // Thong tin them
            ds.writeUTF(config.getGameInfo());
            // Dia chi cua About me
            ds.writeUTF(config.getGameInfoUrl());
            // Dia chi dang ki doi
            ds.writeUTF(config.getGameClanUrl());
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
        try {
            DataInputStream dis = ms.reader();
            int id = dis.readInt();
            String content = dis.readUTF().trim();
            if (content.isEmpty() || content.length() > 100) {
                return;
            }
            // Neu la admin -> bo qua
            if (id == 1) {
                return;
            }
            // Neu la nguoi dua tin -> send Mss 46-> chat The gioi
            if (id == 2) {
                // 10000xu/lan
                if (user.getXu() < CommonConstant.PRICE_CHAT) {
                    return;
                }
                user.updateXu(-CommonConstant.PRICE_CHAT);
                sendServerInfo(GameString.mssTGString(user.getUsername(), content));
                return;
            }
            User receiver = ServerManager.getInstance().getUser(id);
            if (receiver == null) {
                return;
            }
            sendMSSToUser(receiver, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendServerInfo(String s) {
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(s);
            ds.flush();
            ServerManager.getInstance().sendToServer(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMSSToUser(User us, String s) {
        try {
            Message ms = new Message(5);
            DataOutputStream ds = ms.writer();
            if (us != null) {
                ds.writeInt(us.getId());
                ds.writeUTF(us.getUsername());
            } else {
                ds.writeInt(1);
                ds.writeUTF("ADMIN");
            }
            ds.writeUTF(s);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void getFilePack(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            byte version = dis.readByte();

            switch (type) {
                case 1 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    byte currentVersion = config.getIconversion2();
                    writeFilePack(CommonConstant.iconCacheName, type, version, currentVersion);
                }

                case 2 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    byte currentVersion = config.getValuesversion2();
                    writeFilePack(CommonConstant.mapCacheName, type, version, currentVersion);
                }

                case 3 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    byte currentVersion = config.getPlayerVersion2();
                    writeFilePack(CommonConstant.playerCacheName, type, version, currentVersion);
                }

                case 4 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    byte currentVersion = config.getEquipVersion2();
                    writeFilePack(CommonConstant.equipCacheName, type, version, currentVersion);
                }

                case 5 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    byte currentVersion = config.getLevelCVersion2();
                    writeFilePack(CommonConstant.levelCacheName, type, version, currentVersion);
                }

                case 6 -> {
                    nangCap2();
                    sendRuongDoInfo();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeFilePack(String fileName, byte type, byte version, byte currentVersion) {
        try {
            Message ms = new Message(Cmd.GET_FILEPACK);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            ds.writeByte(currentVersion);
            if (version != currentVersion) {
                byte[] ab = Until.getFile(fileName);
                if (ab != null) {
                    ds.writeShort(ab.length);
                    ds.write(ab);
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRuongDoInfo() {
        try {
            // Ruong trang bi
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            // DB Key
            for (int i = 0; i < 5; i++) {
                ds.writeInt(65536);
            }
            ds.flush();
            user.sendMessage(ms);

            // Ruong do dac biet
            ms = new Message(125);
            ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nangCap(Message ms) {

    }

    @Override
    public void nangCap2() {
        try {
            Message ms = new Message(Cmd.CHARACTOR_INFO);
            DataOutputStream ds = ms.writer();
            // lever
            ds.writeByte(1);
            // lever %
            ds.writeByte(0);
            // Diem con lai de nang cap
            ds.writeShort(0);
            // So diem da cong
            for (int i = 0; i < 5; i++) {
                ds.writeShort(10);
            }
            // XP Get
            ds.writeInt(0);
            // XP Max Lever
            ds.writeInt(1000);
            /* Danh vong */
            ds.writeInt(0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        try {
            int idS = ms.reader().readByte();
            ms = new Message(120);
            DataOutputStream ds = ms.writer();
            ds.writeByte(idS);
            byte[] ab1 = Until.getFile("res/bigImage/bigImage" + idS + ".png");
            if (ab1 != null) {
                ds.writeShort(ab1.length);
                ds.write(ab1);
            } else {
                ds.writeShort(0);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void sendMs10(String message) {
        try {
            Message ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
