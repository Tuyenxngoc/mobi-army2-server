package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.*;
import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.GiftCodeDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.json.GiftCodeRewardData;
import com.teamobi.mobiarmy2.json.ItemData;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.GiftCode.GetGiftCode;
import com.teamobi.mobiarmy2.model.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.clan.ClanItem;
import com.teamobi.mobiarmy2.model.clan.ClanMemEntry;
import com.teamobi.mobiarmy2.model.mission.Mission;
import com.teamobi.mobiarmy2.model.response.GetFriendResponse;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.server.LeaderboardManager;
import com.teamobi.mobiarmy2.server.Room;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Until;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
public class UserService implements IUserService {

    private final User user;
    private final IUserDao userDao = new UserDao();
    private final IGiftCodeDao giftCodeDao = new GiftCodeDao();

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

        if (!LeaderboardManager.getInstance().isComplete()) {
            sendMessageLoginFail(GameString.getNotFinishedLoadingRanking());
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            String username = dis.readUTF();
            String password = dis.readUTF();
            String version = dis.readUTF();

            if (isInvalidInput(username) || isInvalidInput(password)) {
                sendMessageLoginFail(GameString.reg_Error1());
                return;
            }

            User userFound = userDao.findByUsernameAndPassword(username, password);
            if (userFound == null) {
                sendMessageLoginFail(GameString.loginPassFail());
                return;
            }
            if (userFound.isLock()) {
                sendMessageLoginFail(GameString.loginLock());
                return;
            }
            if (!userFound.isActive()) {
                sendMessageLoginFail(GameString.loginActive());
                return;
            }

            ServerManager serverManager = ServerManager.getInstance();

            //Kiểm tra có đang đăng nhập hay không
            User userLogin = serverManager.getUserByPlayerId(userFound.getPlayerId());
            if (userLogin != null) {
                userLogin.getUserService().sendServerMessage2(GameString.userLoginMany());
                userLogin.getSession().close();

                sendMessageLoginFail(GameString.loginErr1());
                return;
            }

            copyUserData(user, userFound);
            user.getSession().setVersion(version);
            user.setLogged(true);

            //Kiểm tra chưa online hơn 1 ngày;
            LocalDateTime now = LocalDateTime.now();
            if (Until.hasLoggedInOnNewDay(user.getLastOnline(), now)) {
                //Gửi item
                byte indexItem = ItemFightData.randomItem();
                byte quantity = 1;
                user.updateItems(indexItem, quantity);
                sendMSSToUser(GameString.dailyReward(quantity, ItemFightData.ITEM_FIGHTS.get(indexItem).getName()));
                //Cập nhật quà top
                if (user.getTopEarningsXu() > 0) {
                    user.updateXu(user.getTopEarningsXu());
                    sendMSSToUser(GameString.dailyTopReward(user.getTopEarningsXu()));
                }
                //Gửi messeage khi login
                sendMSSToUser(serverManager.config().getSEND_THU1());
                sendMSSToUser(serverManager.config().getSEND_THU2());
                sendMSSToUser(serverManager.config().getSEND_THU3());
                sendMSSToUser(serverManager.config().getSEND_THU4());
                sendMSSToUser(serverManager.config().getSEND_THU5());
                //Cập nhật nhiệm vụ
                user.updateMission(16, 1);
                //Lưu lại lần đăng nhập
                userDao.updateLastOnline(now, user.getPlayerId());
            }

            userDao.updateOnline(true, userFound.getPlayerId());

            sendLoginSuccess();
            IServerConfig config = serverManager.config();
            sendNVData(config);
            sendRoomInfo(config);
            sendMapCollisionInfo();

            sendServerInfo(config.getSEND_CHAT_LOGIN());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isInvalidInput(String input) {
        return !input.matches(CommonConstant.ALPHANUMERIC_PATTERN);
    }

    private void copyUserData(User target, User source) {
        target.setUserId(source.getUserId());
        target.setPlayerId(source.getPlayerId());
        target.setUsername(source.getUsername());
        target.setXu(source.getXu());
        target.setLuong(source.getLuong());
        target.setDanhVong(source.getDanhVong());
        target.setClanId(source.getClanId());
        target.setLevels(source.getLevels());
        target.setLevelPercents(source.getLevelPercents());
        target.setNvUsed(source.getNvUsed());
        target.setNvStt(source.getNvStt());
        target.setXps(source.getXps());
        target.setPoints(source.getPoints());
        target.setPointAdd(source.getPointAdd());
        target.setNvData(source.getNvData());
        target.setNvEquip(source.getNvEquip());
        target.setFriends(source.getFriends());
        target.setMission(source.getMission());
        target.setMissionLevel(source.getMissionLevel());
        target.setRuongDoItem(source.getRuongDoItem());
        target.setRuongDoTB(source.getRuongDoTB());
        target.setNvEquip(source.getNvEquip());
        target.setItems(source.getItems());
        target.setXpX2Time(source.getXpX2Time());
        target.setLastOnline(source.getLastOnline());
        target.setTopEarningsXu(source.getTopEarningsXu());
    }

    @Override
    public void handleLogout() {
        user.isLogged = false;
        userDao.update(user);
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
    public void sendServerMessage2(String message) {
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
    public void handleHandshakeMessage() {
        user.getSession().sendKeys();
    }

    @Override
    public void giaHanDo(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte action = ms.reader().readByte();
            int idKey = ms.reader().readInt();
            DataOutputStream ds;
            if (action == 0) {
                int gia = 0;
                if ((idKey & 0x10000) > 0) {
                    idKey &= 0xFFFF;
                    if (idKey < user.ruongDoTB.size()) {
                        ruongDoTBEntry rdE = user.ruongDoTB.get(idKey);
                        for (int i = 0; i < 3; i++) {
                            if (rdE.slot[i] >= 0) {
                                SpecialItemData.SpecialItemEntry spE = SpecialItemData.getSpecialItemById(rdE.slot[i]);
                                gia += spE.buyXu;
                            }
                        }
                        gia = gia / 20;
                        if (rdE.entry.giaXu > 0) {
                            gia += rdE.entry.giaXu;
                        } else if (rdE.entry.giaLuong > 0) {
                            gia += rdE.entry.giaLuong * 1000;
                        }
                        ms = new Message(-25);
                        ds = ms.writer();
                        ds.writeInt(rdE.index | 0x10000);
                        ds.writeUTF(String.format(GameString.giaHanRequest(), gia));
                        ds.flush();
                        user.sendMessage(ms);
                    }
                }
            }
            if (action == 1) {
                int gia = 0;
                if ((idKey & 0x10000) > 0) {
                    idKey &= 0xFFFF;
                    if (idKey < user.ruongDoTB.size()) {
                        ruongDoTBEntry rdE = user.ruongDoTB.get(idKey);
                        for (int i = 0; i < 3; i++) {
                            if (rdE.slot[i] >= 0) {
                                SpecialItemData.SpecialItemEntry spE = SpecialItemData.getSpecialItemById(rdE.slot[i]);
                                gia += spE.buyXu;
                            }
                        }
                        gia = gia / 20;
                        if (rdE.entry.giaXu > 0) {
                            gia += rdE.entry.giaXu;
                        } else if (rdE.entry.giaLuong > 0) {
                            gia += rdE.entry.giaLuong * 1000;
                        }
                        if (user.getXu() < gia) {
                            ms = new Message(45);
                            ds = ms.writer();
                            ds.writeUTF(GameString.xuNotEnought());
                            ds.flush();
                            user.sendMessage(ms);
                            return;
                        }
                        user.updateXu(-gia);
                        rdE.dayBuy = new Date();
                        user.updateRuong(rdE, null, -1, null, null);
                        ms = new Message(45);
                        ds = ms.writer();
                        ds.writeUTF(GameString.giaHanSucess());
                        ds.flush();
                        user.sendMessage(ms);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleGetMissions(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte action = ms.reader().readByte();

            if (action == 0) {
                sendMissionInfo();
            } else if (action == 1) {
                byte missionId = ms.reader().readByte();
                missionComplete(missionId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void missionComplete(byte missionId) throws IOException {
        String message;
        Mission mission = MissionData.getMissionById(missionId);
        if (mission == null) {
            message = GameString.missionError1();
        } else {
            byte missionType = mission.getType();
            byte missionLevel = user.getMissionLevel()[missionType];
            byte requiredLevel = mission.getLevel();

            if (user.getMission()[missionType] < mission.getRequirement()) {
                message = GameString.missionError2();
            } else if (missionLevel == requiredLevel) {
                user.getMissionLevel()[mission.getType()]++;
                if (mission.getRewardXu() > 0) {
                    user.updateXu(mission.getRewardXu());
                }
                if (mission.getRewardLuong() > 0) {
                    user.updateLuong(mission.getRewardLuong());
                }
                if (mission.getRewardXp() > 0) {
                    user.updateXp(mission.getRewardXp());
                }
                if (mission.getRewardCup() > 0) {
                    user.updateDanhVong(mission.getRewardCup());
                }
                sendMissionInfo();
                message = GameString.missionComplete(mission.getReward());
            } else if (missionLevel < requiredLevel) {
                message = GameString.missionError2();
            } else {
                message = GameString.missionError3();
            }
        }
        sendServerMessage2(message);
    }

    private void sendMissionInfo() throws IOException {
        Message ms = new Message(Cmd.MISSISON);
        DataOutputStream ds = ms.writer();
        int i = 0;
        for (List<Mission> missionList : MissionData.MISSION_LIST.values()) {
            int index = user.getMissionLevel()[i] - 1;// Subtracting 1 to access the correct index
            if (index >= missionList.size()) {
                index = missionList.size() - 1;
            }
            Mission mission = missionList.get(index);
            ds.writeByte(mission.getId());
            ds.writeByte(mission.getLevel());
            ds.writeUTF(mission.getName());
            ds.writeUTF(mission.getReward());
            ds.writeInt(mission.getRequirement());
            ds.writeInt(Math.min(user.getMission()[i], mission.getRequirement()));
            ds.writeBoolean(user.getMission()[i] >= mission.getRequirement());
            i++;
        }
        ds.flush();
        user.sendMessage(ms);
    }

    @Override
    public void sendLoginSuccess() {
        try {
            Message ms = new Message(Cmd.LOGIN_SUCESS);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.writeByte(user.getNvUsed());
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
            for (int i = 0; i < ItemFightData.ITEM_FIGHTS.size(); i++) {
                ds.writeByte(user.items[i]);
                ItemFightData.ItemFight itemFight = ItemFightData.ITEM_FIGHTS.get(i);
                // Gia xu
                ds.writeInt(itemFight.getBuyXu());
                // Gia luong
                ds.writeInt(itemFight.getBuyLuong());
            }

            //Nhan vat
            for (int i = 0; i < 10; i++) {
                if (i > 2) {
                    ds.writeByte(user.nvStt[i] ? 1 : 0);
                    NVData.NVEntry nvEntry = NVData.entrys.get(i);
                    ds.writeShort(nvEntry.buyXu / 1000);
                    ds.writeShort(nvEntry.buyLuong);
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
        if (user.isNotWaiting()) {
            return;
        }
        if (user.getClanId() == 0) {
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
                if (quantity < CommonConstant.MIN_XU_CONTRIBUTE_CLAN) {
                    sendServerMessage(GameString.gopClanMinXu(CommonConstant.MIN_XU_CONTRIBUTE_CLAN));
                    return;
                }
                //Update xu user
                user.updateXu(-quantity);
                //Update xu clan
                ClanManager.getInstance().contributeClan(user.getClanId(), user.getPlayerId(), quantity, Boolean.TRUE);
                sendServerMessage(GameString.gopClanThanhCong());
            } else if (type == 1) {
                if (quantity > user.getLuong()) {
                    return;
                }
                //Update lg user
                user.updateLuong(-quantity);
                //Update lg clan
                ClanManager.getInstance().contributeClan(user.getClanId(), user.getPlayerId(), quantity, Boolean.FALSE);
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
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte materialId = ms.reader().readByte();
            byte action = ms.reader().readByte();
            byte index = -1;
            if (action == 2) {
                index = ms.reader().readByte();
            }
            DataOutputStream ds;
            ms = new Message(-18);
            ds = ms.writer();
            if (action == 1) {
                ds.writeByte(1);
                FormulaData.FormulaDataEntry fDatE = FormulaData.getFomularDataEntryById(materialId);
                if (fDatE == null) {
                    return;
                }
                NVData.NVEntry nvE = NVData.entrys.get(user.getNvUsed());
                ds.writeByte(fDatE.ins.id);
                ds.writeByte(fDatE.entrys.size());
                for (int i = 0; i < fDatE.entrys.size(); i++) {
                    FormulaData.FormulaEntry fE = fDatE.entrys.get(i);
                    ds.writeByte(fDatE.equip[user.getNvUsed()].id);
                    ds.writeUTF(fDatE.equip[user.getNvUsed()].name + " " + nvE.name + " cấp " + fE.level);
                    ds.writeByte(fE.levelRequire);
                    ds.writeByte(user.getNvUsed());
                    ds.writeByte(fDatE.equipType);
                    ds.writeByte(fE.itemNeed.length);
                    boolean isFinish = true;
                    for (int j = 0; j < fE.itemNeed.length; j++) {
                        int itemNumHave = user.getNumItemRuong(fE.itemNeed[j].id);
                        ds.writeByte(fE.itemNeed[j].id);
                        ds.writeUTF(fE.itemNeed[j].name);
                        ds.writeByte(fE.itemNeedNum[j]);
                        ds.writeByte(itemNumHave > fE.itemNeedNum[j] ? fE.itemNeedNum[j] : itemNumHave);
                        if (itemNumHave < fE.itemNeedNum[j]) {
                            isFinish = false;
                        }
                    }
                    boolean isHave;
                    if (fE.level == 1) {
                        ds.writeByte(fDatE.equipNeed[user.getNvUsed()].id);
                        ds.writeUTF(fDatE.equipNeed[user.getNvUsed()].name);
                        isHave = user.getEquipNoNgoc(fDatE.equipNeed[user.getNvUsed()], (byte) 0) != null;
                    } else {
                        ds.writeByte(fDatE.equip[user.getNvUsed()].id);
                        ds.writeUTF(fDatE.equip[user.getNvUsed()].name);
                        isHave = user.getEquipNoNgoc(fDatE.equip[user.getNvUsed()], (byte) (fE.level - 1)) != null;
                    }
                    if (!isHave) {
                        isFinish = false;
                    }
                    ds.writeByte(fE.level - 1);
                    ds.writeBoolean(isHave);
                    ds.writeBoolean(isFinish);
                    ds.writeByte(fE.detail.length);
                    for (int j = 0; j < fE.detail.length; j++) {
                        ds.writeUTF(fE.detail[j]);
                    }
                }
            }
            if (action == 2) {
                FormulaData.FormulaDataEntry fDatE = FormulaData.getFomularDataEntryById(materialId);
                if (fDatE == null || index < 0 || index >= fDatE.entrys.size()) {
                    return;
                }
                ArrayList<ruongDoItemEntry> arrayI = new ArrayList<>();
                ruongDoTBEntry rdE = new ruongDoTBEntry(), rdE2;
                rdE.entry = fDatE.equip[user.getNvUsed()];
                FormulaData.FormulaEntry fE = fDatE.entrys.get(index);
                boolean isFinish = true;
                for (int j = 0; j < fE.itemNeed.length; j++) {
                    int itemNumHave = user.getNumItemRuong(fE.itemNeed[j].id);
                    if (itemNumHave < fE.itemNeedNum[j]) {
                        isFinish = false;
                        break;
                    }
                    ruongDoItemEntry rdE1 = new ruongDoItemEntry();
                    rdE1.entry = fE.itemNeed[j];
                    rdE1.numb = fE.itemNeedNum[j];
                    arrayI.add(rdE1);
                }
                if (fE.level == 1) {
                    rdE2 = user.getEquipNoNgoc(fDatE.equipNeed[user.getNvUsed()], (byte) 0);
                } else {
                    rdE2 = user.getEquipNoNgoc(fDatE.equip[user.getNvUsed()], (byte) (fE.level - 1));
                }
                if (rdE2 == null) {
                    isFinish = false;
                }
                int numFomular = user.getNumItemRuong(materialId);
                if (isFinish && (numFomular > 0 || user.xu >= fDatE.ins.buyXu)) {
                    if (numFomular == 0) {
                        user.updateXu(fDatE.ins.buyXu);
                    } else {
                        ruongDoItemEntry rdE1 = new ruongDoItemEntry();
                        rdE1.entry = fDatE.ins;
                        rdE1.numb = 1;
                        arrayI.add(rdE1);
                    }
                    rdE.vipLevel = fE.level;
                    rdE.invAdd = new short[5];
                    rdE.percentAdd = new short[5];
                    for (int i = 0; i < 5; i++) {
                        rdE.invAdd[i] = (short) Until.nextInt(fE.invAddMin[i], fE.invAddMax[i]);
                        rdE.percentAdd[i] = (short) Until.nextInt(fE.percenAddMin[i], fE.percenAddMax[i]);
                    }
                    user.updateRuong(null, rdE, rdE2.index, null, arrayI);
                    ds.writeByte(0);
                    ds.writeUTF(GameString.cheDoSuccess());
                } else {
                    ds.writeByte(0);
                    ds.writeUTF(GameString.cheDoFail());
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void moHopQua(Message ms) {
        try {
            byte index = ms.reader().readByte();

            byte type = 2;
            byte itemId = 55;
            String name = "Tuyenngoc";

            ms = new Message(Cmd.GET_LUCKYGIFT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(index);
            ds.writeByte(type);
            ds.writeByte(itemId);
            ds.writeUTF(name);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void bangXepHang(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            byte page = dis.readByte();

            LeaderboardManager leaderboardManager = LeaderboardManager.getInstance();
            if (type >= leaderboardManager.getLeaderboardCategories().length) {
                return;
            }
            ms = new Message(Cmd.BANGTHANHTICH);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            if (type < 0) {
                ds.writeByte(leaderboardManager.getLeaderboardCategories().length);
                for (String name : leaderboardManager.getLeaderboardCategories()) {
                    ds.writeUTF(name);
                }
            } else {
                //Kiểm tra page num
                byte maxPage = (byte) (leaderboardManager.getLeaderboardEntries().get(type).size() / 10);
                if (page > maxPage || page >= 10) {
                    page = 0;
                }
                if (page < 0) {
                    page = maxPage;
                }
                //Gửi dữ liệu
                ds.writeByte(page);
                ds.writeUTF(leaderboardManager.getLeaderboardLabels()[type]);
                List<PlayerLeaderboardEntry> bangXH = leaderboardManager.getLeaderboardEntries(type, page, 10);
                if (bangXH != null) {
                    for (PlayerLeaderboardEntry pl : bangXH) {
                        ds.writeInt(pl.getPlayerId());
                        ds.writeUTF(pl.getUsername());
                        ds.writeByte(pl.getNvUsed());
                        ds.writeShort(pl.getClanId());
                        ds.writeByte(pl.getLevel());
                        ds.writeByte(pl.getLevelPt());
                        ds.writeByte(pl.getIndex());
                        for (short i : pl.getData()) {
                            ds.writeShort(i);
                        }
                        ds.writeUTF(pl.getDetail());
                    }
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handlePurchaseClanItem(Message ms) {
        if (user.getClanId() == 0) {
            sendServerMessage(GameString.notClan());
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {
                sendClanShop();
            } else if (type == 1) {
                byte unit = dis.readByte();
                byte itemId = dis.readByte();
                buyClanShop(unit, itemId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void buyClanShop(byte unit, byte itemId) {
        ClanManager clanManager = ClanManager.getInstance();
        ItemClanData.ClanItemDetail clanItemDetail = ItemClanData.getItemClanById(itemId);

        if (clanItemDetail == null || clanItemDetail.getOnSale() != 1) {
            return;
        }

        byte currentLevel = clanManager.getClanLevel(user.getClanId());
        if (currentLevel < clanItemDetail.getLevel()) {
            sendServerMessage(GameString.clanLevelNotEnought());
            return;
        }

        if (unit == 0) {//Xu
            if (clanItemDetail.getXu() < 0) {
                return;
            }
            int xuClan = clanManager.getClanXu(user.getClanId());
            if (xuClan < clanItemDetail.getXu()) {
                sendServerMessage(GameString.clanXuNotEnought());
                return;
            }

            clanManager.updateItemClan(user.getClanId(), user.getPlayerId(), clanItemDetail, true);
        } else if (unit == 1) {//Luong
            if (clanItemDetail.getLuong() < 0) {
                return;
            }
            int luongClan = clanManager.getClanLuong(user.getClanId());
            if (luongClan < clanItemDetail.getLuong()) {
                sendServerMessage(GameString.clanLuongNotEnought());
                return;
            }

            clanManager.updateItemClan(user.getClanId(), user.getPlayerId(), clanItemDetail, false);
        }
        sendServerMessage(GameString.buySuccess());
    }

    private void sendClanShop() {
        try {
            Message ms = new Message(Cmd.SHOP_BIETDOI);
            DataOutputStream ds = ms.writer();
            ds.writeByte(ItemClanData.clanItemsMap.size());
            for (ItemClanData.ClanItemDetail clanItemDetail : ItemClanData.clanItemsMap.values()) {
                ds.writeByte(clanItemDetail.getId());
                ds.writeUTF(clanItemDetail.getName());
                ds.writeInt(clanItemDetail.getXu());
                ds.writeInt(clanItemDetail.getLuong());
                ds.writeByte(clanItemDetail.getTime());
                ds.writeByte(clanItemDetail.getLevel());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void luyenTap(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
    }

    @Override
    public void dangXuat(Message ms) {
        user.getSession().close();
    }

    @Override
    public void doDacBietShop(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {//send item
                sendDoDacBietShop();
            } else if (type == 1) {//buy item
                byte isBuyXu = dis.readByte();
                byte itemId = dis.readByte();
                int quantity = dis.readUnsignedByte();
                muaDoDacBietShop(isBuyXu, itemId, quantity);
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

        sendServerMessage(GameString.buySuccess());
    }

    private void sendDoDacBietShop() {
        try {
            Message ms = new Message(Cmd.SHOP_LINHTINH);
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
        if (user.isNotWaiting()) {
            return;
        }
    }

    @Override
    public void handleSendMessage(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            int playerId = dis.readInt();
            String content = dis.readUTF().trim();
            if (content.isEmpty() || content.length() > 100) {
                return;
            }
            // Neu la admin -> bo qua
            if (playerId == 1) {
                return;
            }
            // Neu la nguoi dua tin -> send Mss 46-> chat The gioi
            if (playerId == 2) {
                // 10000xu/lan
                if (user.getXu() < CommonConstant.PRICE_CHAT) {
                    return;
                }
                user.updateXu(-CommonConstant.PRICE_CHAT);
                sendServerInfo(GameString.mssTGString(user.getUsername(), content));
                return;
            }
            User receiver = ServerManager.getInstance().getUserByPlayerId(playerId);
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

    public void sendMSSToUser(String message) {
        sendMSSToUser(null, message);
    }

    private void sendMSSToUser(User userSend, String message) {
        try {
            Message ms = new Message(5);
            DataOutputStream ds = ms.writer();
            if (userSend != null) {
                ds.writeInt(userSend.getPlayerId());
                ds.writeUTF(userSend.getUsername());
            } else {
                ds.writeInt(1);
                ds.writeUTF("ADMIN");
            }
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void denKhuVuc() {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            Message ms = new Message(Cmd.ROOM_LIST);
            DataOutputStream ds = ms.writer();
            ServerManager server = ServerManager.getInstance();
            for (int i = 0; i < server.getRooms().length; i++) {
                // So phong
                ds.writeByte(i);
                Room room = server.getRooms()[i];
                // Tinh trang 0: do 1: vang 2: xanh
                ds.writeByte(2);
                // Null byte
                ds.writeByte(0);
                // Loai phong 0->6
                ds.writeByte(room.type);
            }
            ds.flush();
            user.sendMessage(ms);
            sendRoomInfo(server.config());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void vaoPhong(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte roomNumber = ms.reader().readByte();
            Room room = ServerManager.getInstance().getRooms()[roomNumber];
            if (room.type == 6 && user.getClanId() == 0) {
                sendServerMessage(GameString.notClan());
                return;
            }
            ms = new Message(7);
            DataOutputStream ds = ms.writer();
            ds.writeByte(roomNumber);
            synchronized (ServerManager.getInstance().getRooms()) {
                for (int i = 0; i < room.entrys.length; i++) {
                    FightWait fightWait = room.entrys[i];
                    synchronized (fightWait.users) {
                        if (fightWait.numPlayer == fightWait.maxSetPlayer || fightWait.started || (fightWait.isLienHoan && fightWait.ntLH > 0)) {
                            continue;
                        }
                        // So khu vuc
                        ds.writeByte(i);
                        // So nguoi trong khu vuc
                        ds.writeByte(fightWait.numPlayer);
                        // So nguoi toi da
                        ds.writeByte(fightWait.maxSetPlayer);
                        // Co mat khau or khong
                        ds.writeBoolean(fightWait.passSet);
                        // So tien
                        ds.writeInt(fightWait.money);
                        // Null boolean
                        ds.writeBoolean(true);
                        // Ten khu vuc
                        ds.writeUTF(fightWait.name);
                        // Kieu 0: Tea, 1: Free
                        ds.writeByte(0);
                    }
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void thamGiaKhuVuc(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte soPhong = dis.readByte();
            byte soKhuVuc = dis.readByte();
            String password = dis.readUTF();
            FightWait fightWait = ServerManager.getInstance().getRooms()[soPhong].entrys[soKhuVuc];

            if (fightWait.passSet && !fightWait.pass.equals(password)) {
                sendServerMessage(GameString.joinKVError1());
                return;
            }

            fightWait.enterFireOval(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void nhanTinn(Message ms) {
        try {
            String message = ms.reader().readUTF().trim();
            if (message.isEmpty() || message.length() > 100) {
                return;
            }
            user.getFightWait().chatMessage(user.getPlayerId(), message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void duoiNguoiCHoi(Message ms) {
        try {
            int playerId = ms.reader().readInt();
            user.getFightWait().kickPlayer(user.getPlayerId(), playerId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void roiKhuVuc(Message ms) {

    }

    @Override
    public void SanSang(Message ms) {
        try {
            boolean ready = ms.reader().readBoolean();
            user.getFightWait().setReady(ready, user.getPlayerId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void hopNgoc(Message ms) {

    }

    @Override
    public void datMatKhau(Message ms) {
        try {
            String password = ms.reader().readUTF().trim();
            if (password.isEmpty() || password.length() > 10) {
                return;
            }
            user.getFightWait().setPassRoom(password, user.getPlayerId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void datCuoc(Message ms) {
        try {
            int xu = ms.reader().readInt();
            if (xu < 0) {
                return;
            }
            user.getFightWait().setMoney(xu, user.getPlayerId());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void batDau() {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().startGame(user.getPlayerId());
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
    public void handleViewFriendList() {
        try {
            Message ms = new Message(Cmd.FRIENDLIST);
            DataOutputStream ds = ms.writer();

            if (!user.getFriends().isEmpty()) {
                List<GetFriendResponse> friends = userDao.getFriendsList(user.getPlayerId(), user.getFriends());
                for (GetFriendResponse friend : friends) {
                    ds.writeInt(friend.getId());
                    ds.writeUTF(friend.getName());
                    ds.writeInt(friend.getXu());
                    ds.writeByte(friend.getNvUsed());
                    ds.writeShort(friend.getClanId());
                    ds.writeByte(friend.getOnline());
                    ds.writeByte(friend.getLevel());
                    ds.writeByte(friend.getLevelPt());
                    for (short i : friend.getData()) {
                        ds.writeShort(i);
                    }
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleAddFriend(Message ms) {
        try {
            Integer id = ms.reader().readInt();
            if (user.getFriends().size() > ServerManager.getInstance().config().getMax_friends()) {
                sendMessageUpdateFriends(Boolean.FALSE, 2);
                return;
            }
            if (user.getFriends().contains(id)) {
                sendMessageUpdateFriends(Boolean.FALSE, 1);
            } else {
                user.getFriends().add(id);
                sendMessageUpdateFriends(Boolean.FALSE, 0);
            }
        } catch (IOException e) {
            e.printStackTrace();
            sendMessageUpdateFriends(Boolean.FALSE, 1);
        }
    }

    @Override
    public void handleRemoveFriend(Message ms) {
        try {
            Integer id = ms.reader().readInt();
            user.getFriends().remove(id);
            sendMessageUpdateFriends(Boolean.TRUE, 0);
        } catch (IOException e) {
            e.printStackTrace();
            sendMessageUpdateFriends(Boolean.TRUE, 1);
        }
    }

    private void sendMessageUpdateFriends(boolean isDelete, int status) {
        try {
            Message ms;
            if (isDelete) {
                ms = new Message(Cmd.DELETE_FRIEND_RESULT);
            } else {
                ms = new Message(Cmd.ADD_FRIEND_RESULT);
            }
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleGetFlayerDetail(Message ms) {
        try {
            int playerId = ms.reader().readInt();

            ms = new Message(Cmd.PLAYER_DETAIL);
            DataOutputStream ds = ms.writer();
            if (user.getPlayerId() != playerId) {
                ds.writeInt(-1);
            } else {
                ds.writeInt(user.getPlayerId());
                ds.writeUTF(user.getUsername());
                ds.writeInt(user.getXu());
                ds.writeByte(user.getCurrentLevel());
                ds.writeByte(user.getCurrentLevelPercent());
                ds.writeInt(user.getLuong());
                ds.writeInt(user.getCurrentXp());
                ds.writeInt(user.getCurrentXpLevel());
                ds.writeInt(user.getDanhVong());
                ds.writeUTF(GameString.notRanking());
            }
            ds.flush();
            this.user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleFindPlayer(Message ms) {
        try {
            String username = ms.reader().readUTF().trim();
            if (username.length() == 0) {
                sendMessageLoginFail(GameString.addFrienvError2());
                return;
            }
            if (isInvalidInput(username)) {
                sendMessageLoginFail(GameString.addFrienvError1());
                return;
            }
            Integer id = userDao.findPlayerIdByUsername(username);
            ms = new Message(Cmd.SEARCH);
            DataOutputStream ds = ms.writer();
            if (id != null) {
                ds.writeInt(id);
                ds.writeUTF(username);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void boLuot(Message ms) {

    }

    @Override
    public void capNhatXY(Message ms) {

    }

    @Override
    public void datTenKhuVUc(Message ms) {
        try {
            String name = ms.reader().readUTF().trim();
            user.getFightWait().setRoomName(user.getPlayerId(), name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void datSoNguoi(Message ms) {
        try {
            byte maxPlayers = ms.reader().readByte();
            user.getFightWait().setMaxPlayers(user.getPlayerId(), maxPlayers);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            user.setNvUsed(idNv);
            ms = new Message(Cmd.CHOOSE_GUN);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
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
    public void handlePurchaseItem(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte unit = dis.readByte();
            byte itemIndex = dis.readByte();
            byte quantity = dis.readByte();
            if (itemIndex < 0 || itemIndex >= ItemFightData.ITEM_FIGHTS.size()) {
                return;
            }
            if (user.getItems()[itemIndex] + quantity > ServerManager.getInstance().config().getMax_item()) {
                return;
            }

            switch (unit) {
                case 0 -> {
                    int total = ItemFightData.ITEM_FIGHTS.get(itemIndex).getBuyXu() * quantity;
                    if (user.getXu() < total || total < 0) {
                        return;
                    }
                    user.updateXu(-total);
                    user.updateItems(itemIndex, quantity);
                }
                case 1 -> {
                    int total = ItemFightData.ITEM_FIGHTS.get(itemIndex).getBuyLuong() * quantity;
                    if (user.getLuong() < total || total < 0) {
                        return;
                    }
                    user.updateLuong(-total);
                    user.updateItems(itemIndex, quantity);
                }
                default -> {
                    return;
                }
            }
            ms = new Message(72);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(itemIndex);
            ds.writeByte(user.getItems()[itemIndex]);
            ds.writeInt(user.getXu());
            ds.writeInt(user.luong);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleBuyCharacter(Message ms) {
        try {
            byte idnv = ms.reader().readByte();
            idnv += 3;
            if (user.nvStt[idnv]) {
                return;
            }
            NVData.NVEntry nventry = NVData.entrys.get(idnv);
            byte buyLuong = ms.reader().readByte();
            boolean buyOK = false;
            if (buyLuong == 1) {
                if (user.luong >= nventry.buyLuong && nventry.buyLuong >= 0) {
                    user.updateLuong(-nventry.buyLuong);
                    buyOK = true;
                }
            } else {
                if (user.xu >= nventry.buyXu && nventry.buyXu >= 0) {
                    user.updateXu(-nventry.buyXu);
                    buyOK = true;
                }
            }
            if (buyOK) {
                user.nvStt[idnv] = true;
                ms = new Message(74);
                DataOutputStream ds = ms.writer();
                ds.writeByte(idnv - 3);
                ds.flush();
                user.sendMessage(ms);
            } else {
                ms = new Message(45);
                DataOutputStream ds = ms.writer();
                ds.writeUTF(GameString.xuNotEnought());
                ds.flush();
                user.sendMessage(ms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void chonBanDo(Message ms) {
        try {
            byte mapId = ms.reader().readByte();
            user.getFightWait().setMap(user.getPlayerId(), mapId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void napTheCao(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            String type = dis.readUTF().trim();
            String serial = dis.readUTF().trim();
            String pin = dis.readUTF().trim();

            if (type.equals("giftcode") && !serial.isEmpty()) {
                handleGiftCode(serial);
                return;
            }
            sendServerMessage("..." + serial + " " + pin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGiftCode(String code) {
        GetGiftCode giftCode = giftCodeDao.getGiftCode(code);
        if (giftCode == null) {
            sendServerMessage(GameString.giftCodeError1());
            return;
        }
        if (giftCode.getLimit() <= 0) {
            sendServerMessage(GameString.giftCodeError2());
            return;
        }
        if (LocalDateTime.now().isAfter(giftCode.getExpiryDate())) {
            sendServerMessage(GameString.giftCodeError3(giftCode.getExpiryDate()));
            return;
        }
        for (int i = 0; i < giftCode.getUsedPlayerIds().length; i++) {
            if (giftCode.getUsedPlayerIds()[i] == user.getPlayerId()) {
                sendServerMessage(GameString.giftCodeError4());
                return;
            }
        }

        GiftCodeRewardData rewardData = GsonUtil.GSON.fromJson(giftCode.getReward(), GiftCodeRewardData.class);

        StringBuilder totalRewardBuilder = new StringBuilder();
        if (rewardData.getXu() > 0) {
            user.updateXu(rewardData.getXu());
            totalRewardBuilder.append("+ ").append(rewardData.getXu()).append(" xu, ");
        }
        if (rewardData.getLuong() > 0) {
            user.updateLuong(rewardData.getLuong());
            totalRewardBuilder.append("+ ").append(rewardData.getLuong()).append(" lượng, ");
        }
        if (rewardData.getExp() > 0) {
            user.updateXp(rewardData.getExp());
            totalRewardBuilder.append("+ ").append(rewardData.getExp()).append(" exp");
        }
        if (rewardData.getItems() != null) {
            for (ItemData item : rewardData.getItems()) {
                //todo update items
            }
        }

        String totalReward = totalRewardBuilder.toString().trim();
        if (!totalReward.isEmpty()) {
            sendMSSToUser(String.format("CODE %s: %s", code, totalReward));
        }

        giftCode.addUsedPlayerId(user.getPlayerId());
        giftCodeDao.updateGiftCode(giftCode);

        sendServerMessage(GameString.giftCodeSuccess());
    }

    @Override
    public void handleFindPlayerWait(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            boolean find = dis.readBoolean();
            if (find) {
                user.getFightWait().findPlayer(user.getPlayerId());
            } else {
                int playerId = dis.readInt();
                user.getFightWait().inviteToRoom(playerId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void xoaDan(Message ms) {

    }

    @Override
    public void handleChangePassword(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            String oldPass = dis.readUTF();
            String newPass = dis.readUTF();

            if (isInvalidInput(oldPass) || isInvalidInput(newPass)) {
                sendServerMessage(GameString.changPassError1());
                return;
            }

            if (!userDao.existsByUserIdAndPassword(user.getUserId(), oldPass)) {
                sendServerMessage(GameString.changPassError2());
                return;
            }

            userDao.changePassword(user.getUserId(), newPass);
            sendServerMessage(GameString.changPassSuccess());
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                ds.writeByte(rdtbEntry.entry.characterId);
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
    public void handleAddPoints(Message ms) {
        try {
            short[] points = new short[5];
            short totalPoints = 0;
            for (int i = 0; i < points.length; i++) {
                points[i] = ms.reader().readShort();
                if (points[i] < 0) {
                    return;
                }
                totalPoints += points[i];
            }
            if (totalPoints <= user.getCurrentPoint()) {
                user.updatePoints(points, totalPoints);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendCharacterInfo();
    }

    @Override
    public void sendCharacterInfo() {
        try {
            Message ms = new Message(Cmd.CHARACTOR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getCurrentLevel());
            ds.writeByte(user.getCurrentLevelPercent());
            ds.writeShort(user.getCurrentPoint());
            for (short point : user.getCurrentPointAdd()) {
                ds.writeShort(point);
            }
            ds.writeInt(user.getCurrentXp());
            ds.writeInt(user.getCurrentXpLevel());
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
    public void shopTrangBi() {
        try {
            Message ms = new Message(103);
            DataOutputStream ds = ms.writer();
            // Size
            ds.writeShort(NVData.nSaleEquip);
            // Cac trang bi
            for (NVData.EquipmentEntry eqEntry : NVData.equips) {
                if (!eqEntry.onSale) {
                    continue;
                }
                // idNV
                ds.writeByte(eqEntry.characterId);
                ds.writeByte(eqEntry.idEquipDat);
                ds.writeShort(eqEntry.id);
                ds.writeUTF(eqEntry.name);
                ds.writeInt(eqEntry.giaXu);
                ds.writeInt(eqEntry.giaLuong);
                ds.writeByte(eqEntry.hanSD);
                ds.writeByte(eqEntry.lvRequire);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEquipmentPurchases(Message ms) {
        DataInputStream dis = ms.reader();

        try {
            byte type = dis.readByte();
            switch (type) {
                case 0 -> {
                    //Mua trang bi
                    short indexSale = dis.readShort();
                    byte buyLuong = dis.readByte();
                    muaTrangBi(indexSale, buyLuong);
                }
                case 1 -> {
                    //Ban trang bi
                    byte size = dis.readByte();
                    for (int i = 0; i < size; i++) {
                        int id = dis.readInt();
                    }
                }
                case 2 -> {//Xac nhan ban trang bi
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void muaTrangBi(short indexSale, byte buyLuong) {
        if (user.getRuongDoTB().size() >= ServerManager.getInstance().config().getMax_ruong_tb()) {
            sendServerMessage(GameString.ruongNoSlot());
            return;
        }
        NVData.EquipmentEntry eqEntry = NVData.getEquipEntryByIndexSale(indexSale);
        if (eqEntry == null || !eqEntry.onSale || (buyLuong == 0 ? eqEntry.giaXu : eqEntry.giaLuong) < 0) {
            return;
        }

        switch (buyLuong) {
            case 0 -> {
                if (user.getXu() < eqEntry.giaXu) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateXu(-eqEntry.giaXu);
            }
            case 1 -> {
                if (user.getLuong() < eqEntry.giaLuong) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateLuong(-eqEntry.giaLuong);
            }
            default -> {
                return;
            }
        }
        ruongDoTBEntry rdE = new ruongDoTBEntry();
        rdE.entry = eqEntry;
        user.updateRuong(null, rdE, -1, null, null);
        sendServerMessage(GameString.buySuccess());
    }

    @Override
    public void handleSpinWheel(Message ms) {
        try {
            byte unit = ms.reader().readByte();
            switch (unit) {
                case 0 -> {
                    if (user.getXu() < SpinWheelConstants.XU_COST) {
                        sendServerMessage(GameString.xuNotEnought());
                        return;
                    }
                    user.updateXu(-SpinWheelConstants.XU_COST);
                }
                case 1 -> {
                    if (user.getLuong() < SpinWheelConstants.LUONG_COST) {
                        sendServerMessage(GameString.xuNotEnought());
                        return;
                    }
                    user.updateLuong(-SpinWheelConstants.LUONG_COST);
                }
                default -> {
                    return;
                }
            }

            ms = new Message(Cmd.RULET);
            DataOutputStream ds = ms.writer();

            int luckyIndex = Until.nextInt(10);
            for (byte i = 0; i < 10; i++) {
                byte type = Until.nextByte(SpinWheelConstants.TYPE_PROBABILITIES);
                byte itemId = 0;
                int quantity = 0;

                switch (type) {
                    case 0 -> {
                        itemId = ItemFightData.randomItem();
                        quantity = SpinWheelConstants.ITEM_COUNTS[Until.nextInt(SpinWheelConstants.ITEM_PROBABILITIES)];
                        if (i == luckyIndex) {
                            user.updateItems(itemId, quantity);
                        }
                    }
                    case 1 -> {
                        quantity = SpinWheelConstants.XU_COUNTS[Until.nextInt(SpinWheelConstants.XU_PROBABILITIES)];
                        if (i == luckyIndex) {
                            user.updateXu(quantity);
                        }
                    }
                    case 2 -> {
                        quantity = SpinWheelConstants.XP_COUNTS[Until.nextInt(SpinWheelConstants.XP_PROBABILITIES)];
                        if (i == luckyIndex) {
                            user.updateXp(quantity);
                        }
                    }
                }
                ds.writeByte(type);
                ds.writeByte(itemId);
                ds.writeInt(quantity);
            }
            ds.writeByte(luckyIndex);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clanIcon(Message ms) {
        try {
            short clanId = ms.reader().readShort();
            byte[] data = ClanManager.getInstance().getClanIcon(clanId);
            if (data == null) {
                return;
            }
            ms = new Message(Cmd.CLAN_ICON);
            DataOutputStream ds = ms.writer();
            ds.writeShort(clanId);
            ds.writeShort(data.length);
            ds.write(data);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getTopClan(Message ms) {
        try {
            byte page = ms.reader().readByte();

            ClanManager clanManager = ClanManager.getInstance();
            byte totalPages = clanManager.getTotalPagesClan();
            if (page > totalPages) {
                page = 0;
            }

            List<ClanEntry> topClan = clanManager.getTopTeams(page);
            ms = new Message(Cmd.TOP_CLAN);
            DataOutputStream ds = ms.writer();
            ds.writeByte(page);
            for (ClanEntry clan : topClan) {
                ds.writeShort(clan.getId());
                ds.writeUTF(clan.getName());
                ds.writeByte(clan.getMemberCount());
                ds.writeByte(clan.getMaxMemberCount());
                ds.writeUTF(clan.getMasterName());
                ds.writeInt(clan.getXu());
                ds.writeInt(clan.getLuong());
                ds.writeInt(clan.getCup());
                ds.writeByte(clan.getLevel());
                ds.writeByte(clan.getLevelPercentage());
                ds.writeUTF(clan.getDescription());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getInfoClan(Message ms) {
        try {
            short clanId = ms.reader().readShort();
            ClanInfo clanDetails = ClanManager.getInstance().getClanInfo(clanId);
            if (clanDetails == null) {
                sendMessageLoginFail(GameString.clanNull());
                return;
            }
            ms = new Message(Cmd.CLAN_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeShort(clanDetails.getId());
            ds.writeUTF(clanDetails.getName());
            ds.writeByte(clanDetails.getMemberCount());
            ds.writeByte(clanDetails.getMaxMemberCount());
            ds.writeUTF(clanDetails.getMasterName());
            ds.writeInt(clanDetails.getXu());
            ds.writeInt(clanDetails.getLuong());
            ds.writeInt(clanDetails.getCup());
            ds.writeInt(clanDetails.getExp());
            ds.writeInt(clanDetails.getXpUpLevel());
            ds.writeByte(clanDetails.getLevel());
            ds.writeByte(clanDetails.getLevelPercentage());
            ds.writeUTF(clanDetails.getDescription());
            ds.writeUTF(clanDetails.getDateCreated());
            ds.writeByte(clanDetails.getItems().size());
            for (ClanItem item : clanDetails.getItems()) {
                ds.writeUTF(item.getName());
                ds.writeInt(item.getTime());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getClanMember(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte page = dis.readByte();
            short clanId = dis.readShort();

            byte totalPage = ClanManager.getInstance().getTotalPage(clanId);
            if (totalPage == -1) {
                return;
            }
            if (page >= totalPage) {
                page = 0;
            }
            if (page < 0) {
                page = (byte) (totalPage - 1);
            }

            List<ClanMemEntry> clanMemEntry = ClanManager.getInstance().getMemberClan(clanId, page);

            ms = new Message(Cmd.CLAN_MEMBER);
            DataOutputStream ds = ms.writer();
            ds.writeByte(page);
            ds.writeUTF("BIỆT ĐỘI");
            for (ClanMemEntry memClan : clanMemEntry) {
                ds.writeInt(memClan.getPlayerId());
                ds.writeUTF(memClan.getUsername());
                ds.writeInt(memClan.getPoint());
                ds.writeByte(memClan.getNvUsed());
                ds.writeByte(memClan.getOnline());
                ds.writeByte(memClan.getLever());
                ds.writeByte(memClan.getLevelPt());
                ds.writeByte(memClan.getIndex());
                ds.writeInt(memClan.getCup());
                for (int j = 0; j < 5; j++) {
                    ds.writeShort(memClan.getDataEquip()[j]);
                }
                ds.writeUTF(memClan.getContribute_text());
                ds.writeUTF(memClan.getContribute_count());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
    public void handleRegister(Message ms) {
        sendMessageLoginFail(GameString.reg_Error6());
    }

    @Override
    public void napTien(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            switch (type) {
                case 0 -> {
                    ms = new Message(Cmd.CHARGE_MONEY_2);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(0);
                    for (PaymentData.Payment payment : PaymentData.payments.values()) {
                        ds.writeUTF(payment.id);
                        ds.writeUTF(payment.info);
                        ds.writeUTF(payment.url);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 1 -> {
                    String id = dis.readUTF();
                    PaymentData.Payment payment = PaymentData.payments.get(id);
                    if (payment != null) {
                        ms = new Message(Cmd.CHARGE_MONEY_2);
                        DataOutputStream ds = ms.writer();
                        ds.writeByte(2);
                        ds.writeUTF(payment.mssTo);
                        ds.writeUTF(payment.mssContent);
                        ds.flush();
                        user.sendMessage(ms);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getMaterialIconMessage(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte typeIcon = dis.readByte();
            byte iconId = dis.readByte();

            byte indexIcon = 0;
            byte[] data = null;
            switch (typeIcon) {
                case 0, 1 -> data = Until.getFile("res/icon/item/" + iconId + ".png");
                case 2 -> data = Until.getFile("res/icon/map/" + iconId + ".png");
                case 3, 4 -> {
                    indexIcon = dis.readByte();
                    data = Until.getFile("res/icon/item/" + iconId + ".png");
                }
            }
            if (data == null) {
                data = new byte[0];
            }

            ms = new Message(Cmd.MATERIAL_ICON);
            DataOutputStream ds = ms.writer();
            ds.writeByte(typeIcon);
            ds.writeByte(iconId);
            ds.writeShort(data.length);
            ds.write(data);
            if (typeIcon == 3 || typeIcon == 4) {
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

    @Override
    public void sendUpdateXp(int xpUp, boolean updateLevel) {
        try {
            Message ms = new Message(Cmd.UPDATE_EXP);
            DataOutputStream ds = ms.writer();
            ds.writeInt(xpUp);
            ds.writeInt(user.getCurrentLevel());
            ds.writeInt(XpData.getXpRequestLevel(user.getCurrentLevel() + 1));
            if (updateLevel) {
                ds.writeByte(1);
                ds.writeByte(user.getCurrentLevel());
                ds.writeByte(user.getCurrentLevelPercent());
                ds.writeShort(user.getCurrentPoint());
            } else {
                ds.writeByte(0);
                ds.writeByte(user.getCurrentLevelPercent());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ping(Message ms) {
    }
}
