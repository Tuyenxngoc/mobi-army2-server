package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.BangXHManager;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.util.Until;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
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

            user.setLever(userFound.getLever());
            user.setLeverPercent(userFound.getLeverPercent());
            user.setNvStt(userFound.getNvStt());
            user.setXp(userFound.getXp());
            user.setPoint(userFound.getPoint());
            user.setPointAdd(userFound.getPointAdd());
            user.setNvData(userFound.getNvData());
            user.setNvEquip(userFound.getNvEquip());

            user.setRuongDoItem(userFound.getRuongDoItem());
            user.setRuongDoTB(userFound.getRuongDoTB());
            user.setNvEquip(userFound.getNvEquip());

            user.getSession().setVersion(version);
            user.setLogged(true);

            userDao.updateOnline(true, userFound.getId());

            sendLoginSuccess();
            IServerConfig config = serverManager.config();
            sendNVData(config);
            sendRoomInfo(config);
            sendMapCollisionInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendNVData(IServerConfig config) {
        try {
            // Send mss 64
            Message ms = new Message(64);
            DataOutputStream ds = ms.writer();
            ArrayList<NVData.NVEntry> nvdatas = NVData.entrys;
            int len = nvdatas.size();
            ds.writeByte(len);
            // Ma sat gio cac nv
            for (NVData.NVEntry nvdata : nvdatas) {
                ds.writeByte(nvdata.ma_sat_gio);
            }
            // Goc cuu tieu
            ds.writeByte(len);
            for (NVData.NVEntry nvdata : nvdatas) {
                ds.writeShort(nvdata.goc_min);
            }
            // Sat thuong 1 vien dan
            ds.writeByte(len);
            for (NVData.NVEntry nvdata : nvdatas) {
                ds.writeByte(nvdata.sat_thuong_dan);
            }
            // So dan
            ds.writeByte(len);
            for (NVData.NVEntry nvdata : nvdatas) {
                ds.writeByte(nvdata.so_dan);
            }
            // Max player
            ds.writeByte(config.getMaxElementFight());
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
        try {
            Message ms = new Message(-19);
            DataOutputStream ds = ms.writer();
            // Size
            ds.writeByte(config.getNameRooms().length);
            for (int i = 0; i < config.getNameRooms().length; i++) {
                // He so cong
                int namen = config.getNameRoomNumbers()[i];
                int typen = config.getNameRoomTypes()[i];
                if (namen > (config.getnRoom()[typen] + config.getRoomTypeStartNum()[typen])) {
                    continue;
                }
                int notRoom = 0;
                for (int j = 0; j < typen; j++) {
                    if (config.getnRoom()[j] > 0) {
                        notRoom++;
                    }
                }
                ds.writeByte(config.getRoomTypeStartNum()[typen] + notRoom);
                // Ten cho phong viet hoa
                ds.writeUTF("Phòng " + (config.getRoomTypeStartNum()[typen] + namen) + ": " + config.getNameRooms()[i]);
                // So
                ds.writeByte(namen);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void sendMapCollisionInfo() {
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
                    if (user.nvEquip[i][j] != null) {
                        ds.writeShort(user.nvEquip[i][j].entry.id);
                    } else if (User.nvEquipDefault[i][j] != null) {
                        ds.writeShort(User.nvEquipDefault[i][j].id);
                    } else {
                        ds.writeShort(-1);
                    }
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
            ds.writeUTF(config.getAddInfo());
            // Dia chi cua About me
            ds.writeUTF(config.getTaiGameInfo());
            // Dia chi dang ki doi
            ds.writeUTF(config.getRegTeamURL());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void gopClan(Message ms) {
        if (user.getClanId() <= 0) {
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            int quantity = dis.readInt();

            if (quantity <= 0) {
                return;
            }

            if (type == 0) {
                if (quantity > user.getXu()) {
                    return;
                }
                if (quantity < 1000) {
                    sendServerMessage(GameString.gopClanMinXu(1000));
                    return;
                }
                //Update xu user
                user.updateXu(-quantity);
                //Update xu clan
                ClanManager.getInstance().contributeClan(user.getClanId(), user.getId(), quantity, Boolean.TRUE);
                sendServerMessage(GameString.gopClanThanhCong());
            } else if (type == 1) {
                if (quantity > user.getLuong()) {
                    return;
                }
                //Update lg user
                user.updateLuong(-quantity);
                //Update lg clan
                ClanManager.getInstance().contributeClan(user.getClanId(), user.getId(), quantity, Boolean.FALSE);
                sendServerMessage(GameString.gopClanThanhCong());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        user.getSession().close();
    }

    @Override
    public void doDacBietShop(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {//send item
                sendDoDacBietShop();
            } else if (type == 1) {//buy item
                byte buyLuong = ms.reader().readByte();
                byte idS = ms.reader().readByte();
                int numb = ms.reader().readUnsignedByte();
                muaDoDacBietShop(buyLuong, idS, numb);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void muaDoDacBietShop(byte type, byte itemId, int quantity) {
        //Todo check num ruong
        if (quantity < 1) {
            return;
        }
        SpecialItemData.SpecialItemEntry spE = SpecialItemData.getSpecialItemById(itemId);
        if (!spE.onSale || (type == 0 ? spE.buyXu : spE.buyLuong) < 0) {
            return;
        }

        if (type == 0) {// mua xu
            int gia = quantity * spE.buyXu;
            if (user.getXu() < gia) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateXu(-gia);
        } else if (type == 1) {// mua luong
            int gia = quantity * spE.buyLuong;
            if (user.getLuong() < gia) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateLuong(-gia);
        }

        ArrayList<ruongDoItemEntry> arrayI = new ArrayList<>();
        ruongDoItemEntry rdE = new ruongDoItemEntry();
        rdE.entry = spE;
        rdE.numb = quantity;
        arrayI.add(rdE);
        try {
            user.updateRuong(null, null, -1, arrayI, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendServerMessage(GameString.buySuccess());
    }

    private void updateRuongItem(ArrayList<ruongDoItemEntry> arrayI) {
        for (ruongDoItemEntry spE : arrayI) {
            boolean isHave = false;
            for (ruongDoItemEntry spE1 : user.getRuongDoItem()) {
                if (spE1.entry.id == spE.entry.id) {
                    isHave = true;
                    spE1.numb += spE.numb;
                    break;
                }
            }
            // ko co=> Tao moi
            if (!isHave) {
                user.getRuongDoItem().add(spE);
            }
        }
    }

    private void sendDoDacBietShop() {
        try {
            Message ms = new Message(-3);
            DataOutputStream ds = ms.writer();
            for (SpecialItemData.SpecialItemEntry spEntry : SpecialItemData.entrys) {
                if (!spEntry.onSale) {
                    continue;
                }
                ds.writeByte(spEntry.id);
                ds.writeUTF(spEntry.name);
                ds.writeUTF(spEntry.detail);
                ds.writeInt(spEntry.buyXu);
                ds.writeInt(spEntry.buyLuong);
                ds.writeByte(spEntry.hanSD);
                ds.writeByte(spEntry.showChon ? 0 : 1);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (user.getState() == UserState.FIGHTING) {

        } else if (user.getState() == UserState.WAIT_FIGHT) {

        }
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
        if (user.getFriends().length == 0) {
            return;
        }
        try {
            List<User> friends = userDao.getFriendsList(user.getId(), user.getFriends());

            ms = new Message(Cmd.FRIENDLIST);
            DataOutputStream ds = ms.writer();
            for (User friend : friends) {
                ds.writeInt(friend.getId());
                ds.writeUTF(friend.getUsername());
                ds.writeInt(friend.getXu());
                ds.writeByte(friend.getNvUsed());
                ds.writeShort(friend.getClanId());
                ds.writeByte(friend.isOnline() ? 1 : 0);
                ds.writeByte(friend.getCurrentLever());
                ds.writeByte(friend.getCurrentLeverPercent());
                /* data nhan vat */
//                short[] data = ServerManager.data(iddbFR, (byte) nv);
//                for (byte j = 0; j < 5; j++) {
//                    ds.writeShort(data[j]);
//                }
                ds.flush();
                user.sendMessage(ms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void ketBan(Message ms) {

    }

    @Override
    public void xoaBan(Message ms) {

    }

    @Override
    public void xemThongTIn(Message ms) {
        try {
            int userId = ms.reader().readInt();
            if (userId < 0) {
                return;
            }

            User userFound = userDao.getUserDetails(userId);
            ms = new Message(Cmd.PLAYER_DETAIL);
            DataOutputStream ds = ms.writer();
            if (userFound == null
                    || userFound.isLogged()
                    || !userFound.isActive()
                    || userFound.getId() != userId
            ) {
                ds.writeInt(-1);
            } else {
                ds.writeInt(userFound.getId());
                ds.writeUTF(userFound.getUsername());
                ds.writeInt(userFound.getXu());

                byte nvUsed = userFound.getNvUsed();

                ds.writeByte(userFound.getLever(nvUsed));
                ds.writeByte(0);/* %lever */
                ds.writeInt(userFound.getLuong());
                ds.writeInt(0);/* XP */
                ds.writeInt(1000);/* XP Level */
                ds.writeInt(userFound.getDanhVong());
                ds.writeUTF("Chưa có hạng");
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void handleChoseCharacter(Message ms) {
        try {
            byte idNv = ms.reader().readByte();
            if (idNv >= NVData.entrys.size() || idNv < 0 || !user.nvStt[idNv]) {
                return;
            }
            user.nvUsed = idNv;
            ms = new Message(Cmd.CHOOSE_GUN);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getId());
            ds.writeByte(idNv);
            ds.flush();
            user.sendMessage(ms);
            sendCharacterInfo();
            sendTBInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTBInfo() {
        try {
            Message ms = new Message(-7);
            DataOutputStream ds = ms.writer();
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.NvData[user.nvUsed][i] | 0x10000);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getIconversion2());
                    if (version != config.getIconversion2()) {
                        byte[] ab = Until.getFile(CommonConstant.iconCacheName);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }

                case 2 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getValuesversion2());
                    if (version != config.getValuesversion2()) {
                        byte[] ab = Until.getFile(CommonConstant.mapCacheName);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 3 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getPlayerVersion2());
                    if (version != config.getPlayerVersion2()) {
                        byte[] ab = Until.getFile(CommonConstant.playerCacheName);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 4 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getEquipVersion2());
                    if (version != config.getEquipVersion2()) {
                        byte[] ab = Until.getFile(CommonConstant.equipCacheName);
                        if (ab == null) {
                            return;
                        }
                        ds.writeInt(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 5 -> {
                    IServerConfig config = ServerManager.getInstance().config();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getLevelCVersion2());
                    if (version != config.getLevelCVersion2()) {
                        byte[] ab = Until.getFile(CommonConstant.levelCacheName);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 6 -> {
                    sendCharacterInfo();
                    sendRuongDoInfo();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRuongDoInfo() {
        try {
            // Ruong trang bi
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
            int lent = user.ruongDoTB.size();
            ds.writeByte(lent);
            for (int i = 0; i < lent; i++) {
                ruongDoTBEntry rdtbEntry = user.ruongDoTB.get(i);
                // dbKey
                ds.writeInt(i | 0x10000);
                // idNV
                ds.writeByte(rdtbEntry.entry.idNV);
                // EquipType
                ds.writeByte(rdtbEntry.entry.idEquipDat);
                // idEquip
                ds.writeShort(rdtbEntry.entry.id);
                // Name
                ds.writeUTF(rdtbEntry.entry.name + (rdtbEntry.cap > 0 ? String.format(" (+%d)", rdtbEntry.cap) : ""));
                // pointNV
                ds.writeByte(rdtbEntry.invAdd.length * 2);
                for (int j = 0; j < rdtbEntry.invAdd.length; j++) {
                    ds.writeByte(rdtbEntry.invAdd[j]);
                    ds.writeByte(rdtbEntry.percentAdd[j]);
                }
                // Ngay het han
                int hanSD = rdtbEntry.entry.hanSD - Until.getNumDay(rdtbEntry.dayBuy, new Date());
                if (hanSD < 0) {
                    hanSD = 0;
                }
                ds.writeByte(hanSD);
                // Slot trong
                ds.writeByte(rdtbEntry.slotNull);
                // Vip I != 0 -> co tang % thoc tinh
                ds.writeByte(rdtbEntry.entry.isSet ? 1 : 0);
                // Vip Level
                ds.writeByte(rdtbEntry.vipLevel);
            }
            // DB Key
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.NvData[user.nvUsed][i] | 0x10000);
            }
            ds.flush();
            user.sendMessage(ms);

            // Ruong do dac biet
            ms = new Message(125);
            ds = ms.writer();
            lent = user.ruongDoItem.size();
            ds.writeByte(0);
            ds.writeByte(lent);
            for (int i = 0; i < lent; i++) {
                ruongDoItemEntry rdiE = user.ruongDoItem.get(i);
                // Id
                ds.writeByte(rdiE.entry.id);
                // Numb
                ds.writeShort(rdiE.numb);
                // Name
                ds.writeUTF(rdiE.entry.name);
                // Detail
                ds.writeUTF(rdiE.entry.detail);
            }
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
    public void sendCharacterInfo() {
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
            ds.writeInt(user.getDanhVong());
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
        try {
            DataInputStream dis = ms.reader();
            byte typeIcon = dis.readByte();
            byte idIcon = dis.readByte();

            byte indexIcon = 0;
            byte[] ab0 = null;
            switch (typeIcon) {
                case 0, 1 -> ab0 = Until.getFile("res/icon/item/" + idIcon + ".png");
                case 2 -> ab0 = Until.getFile("res/icon/map/" + idIcon + ".png");
                case 3, 4 -> {
                    indexIcon = ms.reader().readByte();
                    ab0 = Until.getFile("res/icon/item/" + idIcon + ".png");
                }
            }
            ms = new Message(126);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeIcon);
            ds.writeByte(idIcon);
            if (ab0 == null) {
                ab0 = Until.getFile("/icon.png");
            }
            ds.writeShort(ab0.length);
            ds.write(ab0);
            if ((typeIcon == 3) || (typeIcon == 4)) {
                ds.writeByte(indexIcon);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override
    public void sendUpdateMoney() {
        try {
            Message ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendUpdateDanhVong(int danhVongUp) {
        try {
            Message ms = new Message(Cmd.CUP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(danhVongUp);
            ds.writeInt(user.getDanhVong());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
