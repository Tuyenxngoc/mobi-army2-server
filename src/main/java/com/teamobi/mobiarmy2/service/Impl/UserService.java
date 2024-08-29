package com.teamobi.mobiarmy2.service.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.*;
import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.GiftCodeDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.GiftCodeRewardJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.CharacterData;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.entry.*;
import com.teamobi.mobiarmy2.model.entry.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.entry.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.entry.clan.ClanItem;
import com.teamobi.mobiarmy2.model.entry.clan.ClanMemEntry;
import com.teamobi.mobiarmy2.model.entry.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.item.ClanItemEntry;
import com.teamobi.mobiarmy2.model.entry.item.FightItemEntry;
import com.teamobi.mobiarmy2.model.entry.item.SpecialItemEntry;
import com.teamobi.mobiarmy2.model.entry.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.entry.user.FriendEntry;
import com.teamobi.mobiarmy2.model.entry.user.PlayerLeaderboardEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.server.LeaderboardManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.util.GsonUtil;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class UserService implements IUserService {

    private final User user;
    private final IUserDao userDao;
    private IGiftCodeDao giftCodeDao;

    private UserAction userAction;
    private int totalTransactionAmount;
    private List<EquipmentChestEntry> selectedEquips;
    private List<SpecialItemChestEntry> selectedSpecialItems;
    private FabricateItemEntry fabricateItemEntry;

    public UserService(User user) {
        this.user = user;
        this.userDao = new UserDao();
    }

    private IGiftCodeDao getGiftCodeDao() {
        if (giftCodeDao == null) {
            giftCodeDao = new GiftCodeDao();
        }
        return giftCodeDao;
    }

    private List<EquipmentChestEntry> getSelectedEquips() {
        if (selectedEquips == null) {
            selectedEquips = new ArrayList<>();
        }
        return selectedEquips;
    }

    private List<SpecialItemChestEntry> getSelectedSpecialItems() {
        if (selectedSpecialItems == null) {
            selectedSpecialItems = new ArrayList<>();
        }
        return selectedSpecialItems;
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
            if (Utils.hasLoggedInOnNewDay(user.getLastOnline(), now)) {
                //Gửi item
                byte indexItem = FightItemData.getRandomItem();
                byte quantity = 1;
                user.updateItems(indexItem, quantity);
                sendMessageToUser(GameString.dailyReward(quantity, FightItemData.FIGHT_ITEM_ENTRIES.get(indexItem).getName()));

                //Cập nhật quà top
                if (user.getTopEarningsXu() > 0) {
                    user.updateXu(user.getTopEarningsXu());
                    sendMessageToUser(GameString.dailyTopReward(user.getTopEarningsXu()));
                    user.setTopEarningsXu(0);
                }

                //Đặt lại số lần mua nguyên liệu
                user.setMaterialsPurchased((byte) 0);

                //Gửi messeage khi login
                for (String msg : serverManager.config().getMessage()) {
                    sendMessageToUser(msg);
                }

                //Cập nhật nhiệm vụ
                user.updateMission(16, 1);

                //Lưu lại lần đăng nhập
                userDao.updateLastOnline(now, user.getPlayerId());
            }

            userDao.updateOnline(true, userFound.getPlayerId());

            sendLoginSuccess();
            IServerConfig config = serverManager.config();
            sendCharacterData(config);
            sendRoomCaption(config);
            sendMapCollisionInfo();

            sendServerInfoToUser(config.getMessageLogin());
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
        target.setCup(source.getCup());
        target.setClanId(source.getClanId());
        target.setLevels(source.getLevels());
        target.setLevelPercents(source.getLevelPercents());
        target.setActiveCharacterId(source.getActiveCharacterId());
        target.setPlayerCharacterIds(source.getPlayerCharacterIds());
        target.setOwnedCharacters(source.getOwnedCharacters());
        target.setXps(source.getXps());
        target.setPoints(source.getPoints());
        target.setPointAdd(source.getPointAdd());
        target.setEquipData(source.getEquipData());
        target.setNvEquip(source.getNvEquip());
        target.setFriends(source.getFriends());
        target.setMission(source.getMission());
        target.setMissionLevel(source.getMissionLevel());
        target.setSpecialItemChest(source.getSpecialItemChest());
        target.setEquipmentChest(source.getEquipmentChest());
        target.setNvEquip(source.getNvEquip());
        target.setItems(source.getItems());
        target.setXpX2Time(source.getXpX2Time());
        target.setLastOnline(source.getLastOnline());
        target.setTopEarningsXu(source.getTopEarningsXu());
        target.setMaterialsPurchased(source.getMaterialsPurchased());
        target.setEquipmentPurchased(source.getEquipmentPurchased());
        target.setChestLocked(source.isChestLocked());
        target.setInvitationLocked(source.isInvitationLocked());
    }

    @Override
    public void handleLogout() {
        if (user.getState() == UserState.FIGHTING || user.getState() == UserState.WAIT_FIGHT) {
            user.getFightWait().leaveTeam(user.getPlayerId());
        }

        user.setLogged(false);
        userDao.update(user);
    }

    public void sendCharacterData(IServerConfig config) {
        try {
            List<CharacterEntry> characterEntries = CharacterData.CHARACTER_ENTRIES;
            int characterCount = characterEntries.size();
            Message ms = new Message(Cmd.SKIP_2);
            DataOutputStream ds = ms.writer();
            ds.writeByte(characterCount);
            for (CharacterEntry characterEntry : characterEntries) {
                ds.writeByte(characterEntry.getWindResistance());
            }
            ds.writeByte(characterCount);
            for (CharacterEntry characterEntry : characterEntries) {
                ds.writeShort(characterEntry.getMinAngle());
            }
            ds.writeByte(characterCount);
            for (CharacterEntry characterEntry : characterEntries) {
                ds.writeByte(characterEntry.getBulletDamage());
            }
            ds.writeByte(characterCount);
            for (CharacterEntry characterEntry : characterEntries) {
                ds.writeByte(characterEntry.getBulletCount());
            }
            ds.writeByte(config.getMaxElementFight());
            ds.writeByte(config.getBossRoomMapId().length);
            for (byte mapId : config.getBossRoomMapId()) {
                ds.writeByte(mapId);
            }
            for (byte bossId : config.getBossRoomBossId()) {
                ds.writeByte(bossId);
            }
            ds.writeByte(config.getNumPlayer());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendRoomName() {
        IServerConfig config = ServerManager.getInstance().config();
        String[] names = config.getBossRoomName();
        int startMapBoss = config.getStartMapBoss();
        try {
            Message ms = new Message(Cmd.CHANGE_ROOM_NAME);
            DataOutputStream ds = ms.writer();
            ds.writeByte(names.length);
            for (int i = 0; i < names.length; i++) {
                ds.writeByte(startMapBoss + i);
                ds.writeUTF(String.format("Phòng %d: %s", startMapBoss + i, names[i]));
                ds.writeByte(5);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRoomCaption(IServerConfig config) {
        String[] names = config.getRoomNameVi();
        try {
            Message ms = new Message(Cmd.ROOM_CAPTION);
            DataOutputStream ds = ms.writer();
            ds.writeByte(names.length);
            for (int i = 0; i < names.length; i++) {
                ds.writeUTF(names[i]);
                ds.writeUTF(config.getRoomNameEn()[i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMapCollisionInfo() {
        try {
            Message ms = new Message(92);
            DataOutputStream ds = ms.writer();
            ds.writeShort(MapData.idNotCollisions.length);
            for (short i : MapData.idNotCollisions) {
                ds.writeShort(i);
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
    public void extendItemDuration(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int key = dis.readInt();
            EquipmentChestEntry equip = user.getEquipmentByKey(key);
            if (equip == null) {
                return;
            }
            int gia = 0;
            for (byte itemId : equip.getSlots()) {
                SpecialItemEntry item = SpecialItemData.getSpecialItemById(itemId);
                if (item != null) {
                    gia += item.getPriceXu();
                }
            }
            gia /= 20;
            if (equip.getEquipEntry().getPriceXu() > 0) {
                gia += equip.getEquipEntry().getPriceXu();
            } else if (equip.getEquipEntry().getPriceLuong() > 0) {
                gia += equip.getEquipEntry().getPriceLuong() * 1000;
            }
            if (gia <= 0) {
                return;
            }
            if (action == 0) {
                ms = new Message(Cmd.GET_MORE_DAY);
                DataOutputStream ds = ms.writer();
                ds.writeInt(equip.getKey());
                ds.writeUTF(GameString.giaHanRequest(gia));
                ds.flush();
                user.sendMessage(ms);
            } else {
                if (user.getXu() < gia) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateXu(-gia);
                equip.setPurchaseDate(LocalDateTime.now());
                user.updateInventory(equip, null, null, null);
                sendServerMessage(GameString.giaHanSucess());
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
        MissionEntry missionEntry = MissionData.getMissionById(missionId);
        if (missionEntry == null) {
            message = GameString.missionError1();
        } else {
            byte missionType = missionEntry.getType();
            byte missionLevel = user.getMissionLevel()[missionType];
            byte requiredLevel = missionEntry.getLevel();

            if (user.getMission()[missionType] < missionEntry.getRequirement()) {
                message = GameString.missionError2();
            } else if (missionLevel == requiredLevel) {
                user.getMissionLevel()[missionEntry.getType()]++;
                if (missionEntry.getRewardXu() > 0) {
                    user.updateXu(missionEntry.getRewardXu());
                }
                if (missionEntry.getRewardLuong() > 0) {
                    user.updateLuong(missionEntry.getRewardLuong());
                }
                if (missionEntry.getRewardXp() > 0) {
                    user.updateXp(missionEntry.getRewardXp());
                }
                if (missionEntry.getRewardCup() > 0) {
                    user.updateCup(missionEntry.getRewardCup());
                }
                sendMissionInfo();
                message = GameString.missionComplete(missionEntry.getReward());
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
        for (List<MissionEntry> missionEntryList : MissionData.MISSION_LIST.values()) {
            int index = user.getMissionLevel()[i] - 1;// Subtracting 1 to access the correct index
            if (index >= missionEntryList.size()) {
                index = missionEntryList.size() - 1;
            }
            MissionEntry missionEntry = missionEntryList.get(index);
            ds.writeByte(missionEntry.getId());
            ds.writeByte(missionEntry.getLevel());
            ds.writeUTF(missionEntry.getName());
            ds.writeUTF(missionEntry.getReward());
            ds.writeInt(missionEntry.getRequirement());
            ds.writeInt(Math.min(user.getMission()[i], missionEntry.getRequirement()));
            ds.writeBoolean(user.getMission()[i] >= missionEntry.getRequirement());
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
            ds.writeByte(user.getActiveCharacterId());
            ds.writeShort(user.getClanId());
            ds.writeByte(0);

            // Trang bị
            for (int i = 0; i < 10; i++) {
                EquipmentChestEntry caiTrang = user.getNvEquip()[i][5];
                if (caiTrang != null) {
                    ds.writeBoolean(true);
                    for (short s : caiTrang.getEquipEntry().getDisguiseEquippedIndexes()) {
                        ds.writeShort(s);
                    }
                } else {
                    ds.writeBoolean(false);
                }

                for (int j = 0; j < 5; j++) {
                    if (user.getNvEquip()[i][j] != null) {
                        ds.writeShort(user.getNvEquip()[i][j].getEquipEntry().getEquipIndex());
                    } else if (User.equipDefault[i][j] != null) {
                        ds.writeShort(User.equipDefault[i][j].getEquipIndex());
                    } else {
                        ds.writeShort(-1);
                    }
                }
            }

            //Item
            for (int i = 0; i < FightItemData.FIGHT_ITEM_ENTRIES.size(); i++) {
                ds.writeByte(user.getItems()[i]);
                FightItemEntry fightItemEntry = FightItemData.FIGHT_ITEM_ENTRIES.get(i);
                // Gia xu
                ds.writeInt(fightItemEntry.getBuyXu());
                // Gia luong
                ds.writeInt(fightItemEntry.getBuyLuong());
            }

            //Nhan vat
            for (int i = 0; i < 10; i++) {
                if (i > 2) {
                    ds.writeByte(user.getOwnedCharacters()[i] ? 1 : 0);
                    CharacterEntry characterEntry = CharacterData.CHARACTER_ENTRIES.get(i);
                    ds.writeShort(characterEntry.getPriceXu() / 1000);
                    ds.writeShort(characterEntry.getPriceLuong());
                }
            }

            IServerConfig config = ServerManager.getInstance().config();
            // Thong tin them
            ds.writeUTF(config.getAddInfo());
            // Dia chi cua About me
            ds.writeUTF(config.getAddInfoUrl());
            // Dia chi dang ki doi
            ds.writeUTF(config.getRegTeamUrl());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void contributeToClan(Message ms) {
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
    public void handleMergeEquipments(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte id = dis.readByte();
            byte action = dis.readByte();
            if (action == 1) {
                sendFormulaInfo(id);
            } else if (action == 2) {
                byte level = dis.readByte();
                processFormulaCrafting(id, level);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processFormulaCrafting(byte id, byte level) {
        Map<Byte, List<FormulaEntry>> formulaMap = FormulaData.FORMULA.get(id);
        if (formulaMap == null) {
            return;
        }
        List<FormulaEntry> formulas = formulaMap.get(user.getActiveCharacterId());
        if (formulas == null) {
            return;
        }
        FormulaEntry formula = formulas.get(level);
        if (formula == null) {
            return;
        }

        //Kiểm tra có đủ level chế đồ yêu cầu không
        if (user.getCurrentLevel() < formula.getLevelRequired()) {
            sendFormulaProcessingResult(GameString.cheDoFail());
            return;
        }

        //Kiểm tra có trang bị yêu cầu không
        EquipmentChestEntry requiredEquip = user.getEquipment(formula.getRequiredEquip().getEquipIndex(), user.getActiveCharacterId(), formula.getLevel());
        if (requiredEquip == null) {
            sendFormulaProcessingResult(GameString.cheDoFail());
            return;
        }

        //Tạo một danh sách item cần xóa
        List<SpecialItemChestEntry> itemsToRemove = new ArrayList<>();

        //Kiểm tra có đủ item yêu cầu không
        for (SpecialItemChestEntry item : formula.getRequiredItems()) {
            short itemCountInInventory = user.getInventorySpecialItemCount(item.getItem().getId());
            if (itemCountInInventory < item.getQuantity()) {
                sendFormulaProcessingResult(GameString.cheDoFail());
                return;
            }
            itemsToRemove.add(item);
        }

        //Kiểm tra có công thức hoặc đủ xu không
        SpecialItemChestEntry material = user.getSpecialItemById(formula.getMaterial().getId());
        if (material == null && user.getXu() < formula.getMaterial().getPriceXu()) {
            sendFormulaProcessingResult(GameString.cheDoFail());
            return;
        } else {
            if (material != null) {//Nếu có công thức thì thêm vào danh sách item xóa
                itemsToRemove.add(new SpecialItemChestEntry((short) 1, material.getItem()));
            } else {//Nếu chưu có thì trừ xu
                user.updateXu(-formula.getMaterial().getPriceXu());
            }
        }

        //Xoá trang bị và item yêu cầu
        user.updateInventory(null, requiredEquip, null, itemsToRemove);

        //Random chỉ số
        byte[] addPoints = new byte[5];
        byte[] addPercents = new byte[5];
        for (int i = 0; i < 5; i++) {
            addPoints[i] = (byte) Utils.nextInt(formula.getAddPointsMax()[i], formula.getAddPointsMin()[i]);
            addPercents[i] = (byte) Utils.nextInt(formula.getAddPercentsMax()[i], formula.getAddPercentsMin()[i]);
        }

        //Tạo trang bị mới
        EquipmentChestEntry newEquip = new EquipmentChestEntry();
        newEquip.setEquipEntry(formula.getResultEquip());
        newEquip.setVipLevel((byte) (formula.getLevel() + 1));
        newEquip.setAddPoints(addPoints);
        newEquip.setAddPercents(addPercents);

        //Thêm trang bị vào rương
        user.addEquipment(newEquip);

        //Gửi thông báo
        sendFormulaProcessingResult(GameString.cheDoSuccess());
    }

    private void sendFormulaProcessingResult(String message) {
        try {
            Message ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendFormulaInfo(byte id) {
        try {
            Map<Byte, List<FormulaEntry>> formulaMap = FormulaData.FORMULA.get(id);
            if (formulaMap == null) {
                return;
            }
            List<FormulaEntry> formulaEntries = formulaMap.get(user.getActiveCharacterId());
            if (formulaEntries == null) {
                return;
            }
            Message ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(id);
            ds.writeByte(formulaEntries.size());
            for (FormulaEntry formula : formulaEntries) {
                boolean hasRequiredItem = true;
                boolean hasRequiredEquip = user.hasEquipment(formula.getRequiredEquip().getEquipIndex(), formula.getLevel());
                boolean hasRequiredLevel = user.getCurrentLevel() >= formula.getLevelRequired();

                ds.writeByte(formula.getResultEquip().getEquipIndex());
                ds.writeUTF("%s cấp %d".formatted(formula.getResultEquip().getName(), (formula.getLevel() + 1)));
                ds.writeByte(formula.getLevelRequired());
                ds.writeByte(formula.getCharacterId());
                ds.writeByte(formula.getEquipType());
                ds.writeByte(formula.getRequiredItems().size());
                for (SpecialItemChestEntry item : formula.getRequiredItems()) {
                    short itemCountInInventory = user.getInventorySpecialItemCount(item.getItem().getId());
                    ds.writeByte(item.getItem().getId());
                    ds.writeUTF(item.getItem().getName());
                    ds.writeByte(item.getQuantity());
                    if (itemCountInInventory < item.getQuantity()) {
                        hasRequiredItem = false;
                        ds.writeByte(itemCountInInventory);
                    } else {
                        ds.writeByte(item.getQuantity());
                    }
                }
                ds.writeByte(formula.getRequiredEquip().getEquipIndex());
                ds.writeUTF(formula.getRequiredEquip().getName());
                ds.writeByte(formula.getLevel());
                ds.writeBoolean(hasRequiredEquip);
                ds.writeBoolean(hasRequiredEquip && hasRequiredItem && hasRequiredLevel);
                ds.writeByte(formula.getDetails().length);
                for (String detail : formula.getDetails()) {
                    ds.writeUTF(detail);
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void openLuckyGift(Message ms) {
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
    public void viewLeaderboard(Message ms) {
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
                        ds.writeByte(pl.getActiveCharacter());
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
        ClanItemEntry clanItemEntry = ItemClanData.getItemClanById(itemId);

        if (clanItemEntry == null || clanItemEntry.getOnSale() != 1) {
            return;
        }

        byte currentLevel = clanManager.getClanLevel(user.getClanId());
        if (currentLevel < clanItemEntry.getLevel()) {
            sendServerMessage(GameString.clanLevelNotEnought());
            return;
        }

        if (unit == 0) {//Xu
            if (clanItemEntry.getXu() < 0) {
                return;
            }
            int xuClan = clanManager.getClanXu(user.getClanId());
            if (xuClan < clanItemEntry.getXu()) {
                sendServerMessage(GameString.clanXuNotEnought());
                return;
            }

            clanManager.updateItemClan(user.getClanId(), user.getPlayerId(), clanItemEntry, true);
        } else if (unit == 1) {//Luong
            if (clanItemEntry.getLuong() < 0) {
                return;
            }
            int luongClan = clanManager.getClanLuong(user.getClanId());
            if (luongClan < clanItemEntry.getLuong()) {
                sendServerMessage(GameString.clanLuongNotEnought());
                return;
            }

            clanManager.updateItemClan(user.getClanId(), user.getPlayerId(), clanItemEntry, false);
        }
        sendServerMessage(GameString.buySuccess());
    }

    private void sendClanShop() {
        try {
            Message ms = new Message(Cmd.SHOP_BIETDOI);
            DataOutputStream ds = ms.writer();
            ds.writeByte(ItemClanData.CLAN_ITEM_ENTRY_MAP.size());
            for (ClanItemEntry clanItemEntry : ItemClanData.CLAN_ITEM_ENTRY_MAP.values()) {
                ds.writeByte(clanItemEntry.getId());
                ds.writeUTF(clanItemEntry.getName());
                ds.writeInt(clanItemEntry.getXu());
                ds.writeInt(clanItemEntry.getLuong());
                ds.writeByte(clanItemEntry.getTime());
                ds.writeByte(clanItemEntry.getLevel());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void enterTrainingMap() {
        try {
            Message ms = new Message(Cmd.TRAINING_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(ServerManager.getInstance().config().getTrainingMapId());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleLogout(Message ms) {
        user.getSession().close();
    }

    @Override
    public void handleSpecialItemShop(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {
                sendSpecialItem();
            } else if (type == 1) {
                byte unit = dis.readByte();
                byte itemId = dis.readByte();
                byte quantity = dis.readByte();
                purchaseSpecialItem(unit, itemId, quantity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void purchaseSpecialItem(byte unit, byte itemId, byte quantity) {
        //Kiểm tra số lượng mua hợp lệ
        if (quantity < 1) {
            return;
        }

        //Kiểm tra số lượng đang có trong rương
        if (user.getInventorySpecialItemCount(itemId) + quantity > ServerManager.getInstance().config().getMaxSpecialItemSlots()) {
            sendServerMessage(GameString.ruongMaxSlot());
            return;
        }

        SpecialItemEntry item = SpecialItemData.getSpecialItemById(itemId);
        if (!item.isOnSale() || (unit == 0 ? item.getPriceXu() : item.getPriceLuong()) < 0) {
            return;
        }

        //Giới hạn số lần mua vật liệu
        if (item.isMaterial()) {
            if (user.getMaterialsPurchased() >= 20) {
                sendServerMessage(GameString.materialLimit());
                return;
            } else if (user.getMaterialsPurchased() + quantity > 20) {
                sendServerMessage(GameString.materialLimit1(20 - user.getMaterialsPurchased()));
                return;
            }
        }

        if (unit == 0) {// Mua bằng xu
            int totalPrice = quantity * item.getPriceXu();
            if (user.getXu() < totalPrice) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateXu(-totalPrice);
        } else {// Mua bằng lượng
            int totalPrice = quantity * item.getPriceLuong();
            if (user.getLuong() < totalPrice) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateLuong(-totalPrice);
        }

        //Xử lý khi mua item đặc biệt
        boolean saveItem = handleSpecialItemPurchase(itemId);

        if (saveItem) {
            //Tạo item mới
            SpecialItemChestEntry newItem = new SpecialItemChestEntry(quantity, item);

            //Thêm vào rương đồ
            user.updateInventory(null, null, List.of(newItem), null);
        }

        //Cập nhật số lượng mua nếu là vật liệu
        if (item.isMaterial()) {
            user.incrementMaterialsPurchased(quantity);
        }

        //Gửi thông báo mua thành công
        sendServerMessage(GameString.buySuccess());
    }

    private boolean handleSpecialItemPurchase(byte itemId) {
        if (itemId == 50) {
            user.resetPoints();
            sendCharacterInfo();
            return false;
        }

        return true;
    }

    private void sendSpecialItem() {
        try {
            Message ms = new Message(Cmd.SHOP_LINHTINH);
            DataOutputStream ds = ms.writer();
            for (SpecialItemEntry spEntry : SpecialItemData.SPECIAL_ITEM_ENTRIES) {
                if (!spEntry.isOnSale()) {
                    continue;
                }
                ds.writeByte(spEntry.getId());
                ds.writeUTF(spEntry.getName());
                ds.writeUTF(spEntry.getDetail());
                ds.writeInt(spEntry.getPriceXu());
                ds.writeInt(spEntry.getPriceLuong());
                ds.writeByte(spEntry.getExpirationDays());
                ds.writeByte(spEntry.isShowSelection() ? 0 : 1);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void equipVipItems(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int key = dis.readInt();
            EquipmentChestEntry equip = user.getEquipmentByKey(key);
            if (equip == null ||
                    equip.isExpired() ||
                    !equip.getEquipEntry().isDisguise() ||
                    equip.getEquipEntry().getLevelRequirement() > user.getCurrentLevel() ||
                    equip.getEquipEntry().getCharacterId() != user.getActiveCharacterId()
            ) {
                return;
            }
            EquipmentChestEntry oldEquip = user.getNvEquip()[user.getActiveCharacterId()][5];
            if (oldEquip != null) {
                oldEquip.setInUse(false);
            }
            ms = new Message(Cmd.VIP_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(action);
            if (action == 0) {
                user.getEquipData()[user.getActiveCharacterId()][5] = -1;
                user.getNvEquip()[user.getActiveCharacterId()][5] = null;
            } else {
                equip.setInUse(true);
                user.getEquipData()[user.getActiveCharacterId()][5] = equip.getKey();
                user.getNvEquip()[user.getActiveCharacterId()][5] = equip;
                for (short a : equip.getEquipEntry().getDisguiseEquippedIndexes()) {
                    ds.writeShort(a);
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
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
                sendServerInfoToServer(GameString.mssTGString(user.getUsername(), content));
                return;
            }
            User receiver = ServerManager.getInstance().getUserByPlayerId(playerId);
            if (receiver == null) {
                return;
            }
            sendMessageToUser(receiver, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendServerInfo(String message, boolean toServer) {
        if (message == null || message.isEmpty()) {
            return;
        }
        try {
            Message ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();

            if (toServer) {
                ServerManager.getInstance().sendToServer(ms);
            } else {
                user.sendMessage(ms);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendServerInfoToServer(String message) {
        sendServerInfo(message, true);
    }

    private void sendServerInfoToUser(String message) {
        sendServerInfo(message, false);
    }

    public void sendMessageToUser(String message) {
        sendMessageToUser(null, message);
    }

    private void sendMessageToUser(User userSend, String message) {
        if (message.isEmpty()) {
            return;
        }
        try {
            Message ms = new Message(Cmd.CHAT_TO);
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
    public void handleSendRoomList() {
        if (user.isNotWaiting()) {
            return;
        }
        ServerManager server = ServerManager.getInstance();
        try {
            Message ms = new Message(Cmd.ROOM_LIST);
            DataOutputStream ds = ms.writer();
            for (Room room : server.getRooms()) {
                ds.writeByte(room.getIndex());
                ds.writeByte(room.getStatus());
                ds.writeByte(room.getFightWaitsAvailable());
                ds.writeByte(room.getType());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEnteringRoom(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        Room[] rooms = ServerManager.getInstance().getRooms();
        try {
            byte roomNumber = ms.reader().readByte();
            if (roomNumber < 0 || roomNumber >= rooms.length) {
                return;
            }
            Room room = rooms[roomNumber];
            if (room.getType() == 6 && user.getClanId() == 0) {
                sendServerMessage(GameString.notClan());
                return;
            }
            ms = new Message(Cmd.BOARD_LIST);
            DataOutputStream ds = ms.writer();
            ds.writeByte(roomNumber);
            for (FightWait fightWait : room.getFightWaits()) {
                if (fightWait.isFightWaitInvalid()) {
                    continue;
                }
                ds.writeByte(fightWait.getId());
                ds.writeByte(fightWait.getNumPlayers());
                ds.writeByte(fightWait.getMaxSetPlayers());
                ds.writeBoolean(fightWait.isPassSet());
                ds.writeInt(fightWait.getMoney());
                ds.writeBoolean(false);
                ds.writeUTF(fightWait.getName());
                ds.writeByte(fightWait.getRoom().getIconType());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleJoinBoard(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        Room[] rooms = ServerManager.getInstance().getRooms();
        try {
            DataInputStream dis = ms.reader();
            byte roomNumber = dis.readByte();
            byte areaNumber = dis.readByte();
            String password = dis.readUTF().trim();
            if (roomNumber < 0 || roomNumber >= rooms.length) {
                return;
            }
            FightWait[] fightWaits = rooms[roomNumber].getFightWaits();
            if (areaNumber < 0 || areaNumber >= fightWaits.length) {
                return;
            }
            FightWait fightWait = fightWaits[areaNumber];
            if (fightWait.isPassSet() && !fightWait.getPassword().equals(password)) {
                sendServerMessage(GameString.joinKVError1());
                return;
            }
            fightWait.joinBattleRoom(user);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChatMessage(Message ms) {
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
    public void handleKickPlayer(Message ms) {
        try {
            int playerId = ms.reader().readInt();
            user.getFightWait().kickPlayer(user.getPlayerId(), playerId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleLeaveBoard(Message ms) {
        if (user.getState() == UserState.WAITING) {
            return;
        }
        user.getFightWait().leaveTeam(user.getPlayerId());
    }

    @Override
    public void setReady(Message ms) {
        try {
            boolean ready = ms.reader().readBoolean();
            user.getFightWait().setReady(ready, user.getPlayerId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void imbueGem(Message ms) {
        List<EquipmentChestEntry> equipList = getSelectedEquips();
        List<SpecialItemChestEntry> specialItemList = getSelectedSpecialItems();

        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            if (action == 0) {

                //Đặt lại dữ liệu
                userAction = null;
                fabricateItemEntry = null;
                equipList.clear();
                specialItemList.clear();

                byte size = dis.readByte();
                for (byte i = 0; i < size; i++) {
                    int id = dis.readInt();
                    short quantity = (short) dis.readUnsignedByte();

                    //Kiểm tra dữ liệu hợp lệ
                    if (quantity == 0 || id < 0) {
                        continue;
                    }

                    //Lấy thông tin vật phẩm từ rương người chơi
                    if (id > Byte.MAX_VALUE) {//Trường hợp trang bị
                        EquipmentChestEntry equip = user.getEquipmentByKey(id);
                        if (equip != null && !equipList.contains(equip)) {
                            equipList.add(equip);
                        }
                    } else {//Trường hợp là ngọc
                        SpecialItemChestEntry item = user.getSpecialItemById((byte) id);
                        if (item != null &&
                                item.getItem() != null &&
                                item.getQuantity() >= quantity &&
                                !specialItemList.contains(item)
                        ) {
                            specialItemList.add(new SpecialItemChestEntry(quantity, item.getItem()));
                        }
                    }
                }

                //Thoát nếu không tồn tại trong rương
                if (equipList.isEmpty() && specialItemList.isEmpty()) {
                    return;
                }

                //Ghép ngọc vào trang bị
                if (!equipList.isEmpty() && !specialItemList.isEmpty()) {
                    if (equipList.size() == 1 &&
                            specialItemList.size() == 1 &&
                            specialItemList.get(0).getItem().isGem()
                    ) {
                        userAction = UserAction.GHEP_NGOC_VAO_TRANG_BI;
                        sendMessageConfirm(GameString.hopNgocRequest());
                    } else {
                        sendServerMessage(GameString.hopNgocError());
                    }
                    return;
                }

                if (!specialItemList.isEmpty()) {
                    fabricateItemEntry = FabricateItemData.getFabricateItem(specialItemList);
                    if (fabricateItemEntry != null) {
                        userAction = UserAction.GHEP_SPEC_ITEM;
                        sendMessageConfirm(fabricateItemEntry.getConfirmationMessage());
                        return;
                    }

                    if (specialItemList.size() == 1) {
                        SpecialItemChestEntry itemChestEntry = specialItemList.get(0);
                        if (itemChestEntry.getItem().isGem()) {
                            if (itemChestEntry.getQuantity() == 5 && ((itemChestEntry.getItem().getId() + 1) % 10 != 0)) {
                                userAction = UserAction.NANG_CAP_NGOC;
                                sendMessageConfirm(GameString.hopNgocNC((90 - (itemChestEntry.getItem().getId() % 10) * 10)));
                            } else {
                                userAction = UserAction.BAN_NGOC;
                                totalTransactionAmount = itemChestEntry.getSellPrice();
                                sendMessageConfirm(GameString.hopNgocSell(itemChestEntry.getQuantity(), totalTransactionAmount));
                            }
                            return;
                        }

                        if (itemChestEntry.getItem().isUsable()) {
                            userAction = UserAction.DUNG_SPEC_ITEM;
                            confirmSpecialItemUse(itemChestEntry);
                            return;
                        }
                    }
                }
                sendServerMessage(GameString.hopNgocError());
            } else if (action == 1) {
                switch (userAction) {
                    case GHEP_NGOC_VAO_TRANG_BI -> {
                        EquipmentChestEntry equip = equipList.get(0);
                        SpecialItemChestEntry specialItem = specialItemList.get(0);
                        if (equip.getEmptySlot() >= specialItem.getQuantity()) {
                            for (int i = 0; i < specialItem.getQuantity(); i++) {
                                equip.setNewSlot(specialItem.getItem().getId());
                                equip.decrementEmptySlot();
                                equip.addPoints(specialItem.getItem().getAbility());
                            }
                            user.updateInventory(equip, null, null, specialItemList);
                            sendServerMessage(GameString.hopNgocSuccess());
                        } else {
                            sendServerMessage(GameString.hopNgocNoSlot());
                        }
                    }
                    case NANG_CAP_NGOC -> {
                        SpecialItemChestEntry specialItemChestEntry = specialItemList.get(0);
                        int successRate = (90 - (specialItemChestEntry.getItem().getId() % 10) * 10);
                        int randomNumber = Utils.nextInt(100);
                        if (randomNumber < successRate) {
                            SpecialItemChestEntry newItem = new SpecialItemChestEntry();
                            newItem.setQuantity((short) 1);
                            newItem.setItem(SpecialItemData.getSpecialItemById((byte) (specialItemChestEntry.getItem().getId() + 1)));

                            user.updateInventory(null, null, List.of(newItem), List.of(specialItemChestEntry));
                            sendServerMessage(GameString.nangNgocSuccess(newItem.getQuantity(), newItem.getItem().getName()));
                        } else {
                            specialItemChestEntry.setQuantity((short) 1);
                            user.updateInventory(null, null, null, List.of(specialItemChestEntry));
                            sendServerMessage(GameString.hopNgocFail());
                        }
                    }

                    case BAN_NGOC -> {
                        if (user.isChestLocked()) {
                            sendServerMessage(GameString.chestLocked());
                            return;
                        }
                        user.updateInventory(null, null, null, specialItemList);
                        user.updateXu(totalTransactionAmount);
                        sendServerMessage(GameString.buySuccess());
                    }

                    case DUNG_SPEC_ITEM -> handleUseSpecialItem(specialItemList.get(0));

                    case GHEP_SPEC_ITEM -> {
                        if (fabricateItemEntry.getRewardXu() > 0) {
                            user.updateXu(fabricateItemEntry.getRewardXu());
                        }
                        if (fabricateItemEntry.getRewardLuong() > 0) {
                            user.updateLuong(fabricateItemEntry.getRewardLuong());
                        }
                        if (fabricateItemEntry.getRewardCup() > 0) {
                            user.updateCup(fabricateItemEntry.getRewardCup());
                        }
                        if (fabricateItemEntry.getRewardExp() > 0) {
                            user.updateXp(fabricateItemEntry.getRewardExp());
                        }

                        List<SpecialItemChestEntry> addItems = fabricateItemEntry.getRewardItem()
                                .stream()
                                .map(SpecialItemChestEntry::new)
                                .collect(Collectors.toCollection(ArrayList::new));

                        user.updateInventory(null, null, addItems, specialItemList);

                        if (!fabricateItemEntry.getCompletionMessage().isEmpty()) {
                            sendServerMessage(fabricateItemEntry.getCompletionMessage());
                        }
                    }
                }

                //Đặt lại dữ liệu
                userAction = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUseSpecialItem(SpecialItemChestEntry itemChestEntry) {
        switch (itemChestEntry.getItem().getId()) {
            case 54 -> {
                user.addDaysToXpX2Time(1);
                user.updateInventory(null, null, null, List.of(itemChestEntry));
                sendServerMessage(GameString.specialItemId54Success);
            }
            case 86 -> {
                if (itemChestEntry.getQuantity() == 50) {
                    System.out.println("Cong trang bi vang 1");
                } else if (itemChestEntry.getQuantity() == 100) {
                    System.out.println("Cong trang bi vang 2");
                } else if (itemChestEntry.getQuantity() == 150) {
                    System.out.println("Cong trang bi vang 3");
                } else {
                    user.updateXp(1000 * itemChestEntry.getQuantity());
                    user.updateInventory(null, null, null, List.of(itemChestEntry));
                    sendServerMessage(GameString.specialItemId86Success);
                }
            }
            case 87 -> {
                if (itemChestEntry.getQuantity() == 50) {
                    System.out.println("Cong trang bi bac 1");
                } else if (itemChestEntry.getQuantity() == 100) {
                    System.out.println("Cong trang bi bac 2");
                } else if (itemChestEntry.getQuantity() == 150) {
                    System.out.println("Cong trang bi bac 3");
                } else {
                    user.updateXp(500 * itemChestEntry.getQuantity());
                    user.updateInventory(null, null, null, List.of(itemChestEntry));
                    sendServerMessage(GameString.specialItemId87Success);
                }
            }
        }

    }

    private void confirmSpecialItemUse(SpecialItemChestEntry itemChestEntry) {
        switch (itemChestEntry.getItem().getId()) {
            case 54 -> {
                if (itemChestEntry.getQuantity() == 1) {
                    sendMessageConfirm(GameString.specialItemId54Request);
                } else {
                    sendServerMessage(GameString.hopNgocError());
                }
            }
            case 86 -> {
                if (itemChestEntry.getQuantity() == 50) {
                    sendMessageConfirm(GameString.specialItemId86Request1);
                } else if (itemChestEntry.getQuantity() == 100) {
                    sendMessageConfirm(GameString.specialItemId86Request2);
                } else if (itemChestEntry.getQuantity() == 150) {
                    sendMessageConfirm(GameString.specialItemId86Request3);
                } else {
                    sendMessageConfirm(GameString.specialItemId86Request);
                }
            }
            case 87 -> {
                if (itemChestEntry.getQuantity() == 50) {
                    sendMessageConfirm(GameString.specialItemId87Request1);
                } else if (itemChestEntry.getQuantity() == 100) {
                    sendMessageConfirm(GameString.specialItemId87Request2);
                } else if (itemChestEntry.getQuantity() == 150) {
                    sendMessageConfirm(GameString.specialItemId87Request3);
                } else {
                    sendMessageConfirm(GameString.specialItemId87Request);
                }
            }
        }
    }

    private void sendMessageConfirm(String message) {
        try {
            Message ms = new Message(17);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSetPasswordFightWait(Message ms) {
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
    public void handleSetMoneyFightWait(Message ms) {
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
    public void handleStartGame() {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().startGame(user.getPlayerId());
    }

    @Override
    public void movePlayer(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            short x = dis.readShort();
            short y = dis.readShort();
            user.getFightWait().getFightManager().changeLocationMessage(user, x, y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shoot(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            byte bullId = dis.readByte();
            short x = dis.readShort();
            short y = dis.readShort();
            short angle = dis.readShort();
            byte force = dis.readByte();
            byte force2 = 0;
            if (bullId == 19) {
                force2 = dis.readByte();
            }
            byte numShoot = dis.readByte();

            if (angle < -360) {
                angle = -360;
            } else if (angle > 360) {
                angle = 360;
            }

            if (force < 0) {
                force = 0;
            } else if (force > 30) {
                force = 30;
            }

            if (force2 < 0) {
                force2 = 0;
            } else if (force2 > 30) {
                force2 = 30;
            }

            user.getFightWait().getFightManager().newShoot(user, bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processShootingResult(Message ms) {

    }

    @Override
    public void handleUseItem(Message ms) {
        try {
            byte itemIndex = ms.reader().readByte();

            sendServerMessage(GameString.unauthorized_Item());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleJoinAnyBoard(Message ms) {
        ServerManager server = ServerManager.getInstance();
        Room[] rooms = server.getRooms();
        try {
            byte type = ms.reader().readByte();
            FightWait fightWait = null;
            if (type == 5) {// Đấu trùm
                int start = server.config().getStartMapBoss();
                int end = start + server.config().getRoomQuantity()[5];
                for (int i = start; i < end; i++) {
                    Room room = rooms[i];
                    for (FightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted() &&
                                !fight.isPassSet() &&
                                fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                                fight.getMoney() <= user.getXu()
                        ) {
                            fightWait = fight;
                            break;
                        }
                    }
                }
            } else if (type <= 4 && type >= 1) {//1vs1->4vs4
                int index = Utils.nextInt(0, rooms.length);
                Room room = rooms[index];
                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted() &&
                            !fight.isPassSet() &&
                            fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                            fight.getMoney() <= user.getXu() &&
                            fight.getMaxSetPlayers() == type * 2
                    ) {
                        fightWait = fight;
                        break;
                    }
                }
            } else if (type == 0) {//Khu vực trống
                int index = Utils.nextInt(0, rooms.length);
                Room room = rooms[index];
                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted() &&
                            !fight.isPassSet() &&
                            fight.getMoney() <= user.getXu() &&
                            fight.getNumPlayers() == 0
                    ) {
                        fightWait = fight;
                        break;
                    }
                }
            } else if (type == -1) {//Ngẫu nhiên
                int index = Utils.nextInt(0, rooms.length);
                Room room = rooms[index];
                for (FightWait fight : room.getFightWaits()) {
                    if (!fight.isStarted() &&
                            !fight.isPassSet() &&
                            fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                            fight.getMoney() <= user.getXu()
                    ) {
                        fightWait = fight;
                        break;
                    }
                }
            }

            if (fightWait == null) {
                sendServerMessage(GameString.findKVError1());
            } else {
                fightWait.sendInfo(user);
                fightWait.joinBattleRoom(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleViewFriendList() {
        try {
            Message ms = new Message(Cmd.FRIENDLIST);
            DataOutputStream ds = ms.writer();

            if (!user.getFriends().isEmpty()) {
                List<FriendEntry> friends = userDao.getFriendsList(user.getPlayerId(), user.getFriends());
                for (FriendEntry friend : friends) {
                    ds.writeInt(friend.getId());
                    ds.writeUTF(friend.getName());
                    ds.writeInt(friend.getXu());
                    ds.writeByte(friend.getActiveCharacterId());
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
            if (user.getFriends().size() > ServerManager.getInstance().config().getMaxFriends()) {
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
                ds.writeInt(user.getCup());
                ds.writeUTF(GameString.notRanking());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleFindPlayer(Message ms) {
        try {
            String username = ms.reader().readUTF().trim();
            if (username.isEmpty()) {
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
    public void skipTurn() {
        user.getFightWait().getFightManager().skipTurn(user);
    }

    @Override
    public void updateCoordinates(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            short x = dis.readShort();
            short y = dis.readShort();

            System.out.println(x + " " + y);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSetFightWaitName(Message ms) {
        try {
            String name = ms.reader().readUTF().trim();
            user.getFightWait().setRoomName(user.getPlayerId(), name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSetMaxPlayerFightWait(Message ms) {
        try {
            byte maxPlayers = ms.reader().readByte();
            user.getFightWait().setMaxPlayers(user.getPlayerId(), maxPlayers);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChoseItemFight(Message ms) {
        DataInputStream dis = ms.reader();
        byte[] items = new byte[8];

        try {
            for (int i = 0; i < items.length; i++) {
                byte index = dis.readByte();
                if (user.getItemFightQuantity(index) > 0) {
                    items[i] = index;
                } else {
                    items[i] = -1;
                }
            }
            user.getFightWait().setItems(user.getPlayerId(), items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChoseCharacter(Message ms) {
        try {
            byte characterId = ms.reader().readByte();
            if (characterId >= CharacterData.CHARACTER_ENTRIES.size() || characterId < 0 || !user.getOwnedCharacters()[characterId]) {
                return;
            }
            user.setActiveCharacterId(characterId);

            ms = new Message(Cmd.CHOOSE_GUN);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
            ds.writeByte(characterId);
            ds.flush();
            user.sendMessage(ms);

            sendCharacterInfo();
            sendEquipInfo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEquipInfo() {
        try {
            Message ms = new Message(Cmd.CURR_EQUIP_DBKEY);
            DataOutputStream ds = ms.writer();
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChangeTeam(Message ms) {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().changeTeam(user);
    }

    @Override
    public void handlePurchaseItem(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte unit = dis.readByte();
            byte itemIndex = dis.readByte();
            byte quantity = dis.readByte();
            if (itemIndex < 0 || itemIndex >= FightItemData.FIGHT_ITEM_ENTRIES.size()) {
                return;
            }
            if (user.getItems()[itemIndex] + quantity > ServerManager.getInstance().config().getMaxItem()) {
                return;
            }
            if (unit == 0) {
                int total = FightItemData.FIGHT_ITEM_ENTRIES.get(itemIndex).getBuyXu() * quantity;
                if (user.getXu() < total || total < 0) {
                    return;
                }
                user.updateXu(-total);
            } else {
                int total = FightItemData.FIGHT_ITEM_ENTRIES.get(itemIndex).getBuyLuong() * quantity;
                if (user.getLuong() < total || total < 0) {
                    return;
                }
                user.updateLuong(-total);
            }
            user.updateItems(itemIndex, quantity);
            ms = new Message(72);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(itemIndex);
            ds.writeByte(user.getItems()[itemIndex]);
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            user.sendMessage(ms);
            sendServerMessage(GameString.buySuccess());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleBuyCharacter(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte index = dis.readByte();
            byte unit = dis.readByte();
            if (index < 0 || index >= user.getOwnedCharacters().length - 3) {
                return;
            }
            index += 3;
            if (user.getOwnedCharacters()[index]) {
                return;
            }
            CharacterEntry characterEntry = CharacterData.CHARACTER_ENTRIES.get(index);
            if (unit == 0) {
                if (characterEntry.getPriceXu() <= 0) {
                    return;
                }
                if (user.getXu() < characterEntry.getPriceXu()) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateXu(-characterEntry.getPriceXu());
            } else {
                if (characterEntry.getPriceLuong() <= 0) {
                    return;
                }
                if (user.getLuong() < characterEntry.getPriceLuong()) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateLuong(-characterEntry.getPriceLuong());
            }

            if (userDao.createPlayerCharacter(user.getPlayerId(), index) != 0) {
                PlayerCharacterEntry character = userDao.getPlayerCharacter(user.getPlayerId(), index);
                if (character != null) {
                    user.getLevels()[index] = character.getLevel();
                    user.getXps()[index] = character.getXp();
                    user.getPoints()[index] = character.getPoints();
                    user.getPointAdd()[index] = character.getAdditionalPoints();
                    user.getPlayerCharacterIds()[index] = character.getId();
                    user.getOwnedCharacters()[index] = true;
                    user.getEquipData()[index] = character.getData();

                    ms = new Message(Cmd.BUY_GUN);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(index - 3);
                    ds.flush();
                    user.sendMessage(ms);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSelectMap(Message ms) {
        try {
            byte mapId = ms.reader().readByte();
            user.getFightWait().setMap(user.getPlayerId(), mapId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleCardRecharge(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            String type = dis.readUTF().trim();
            String serial = dis.readUTF().trim();
            String pin = dis.readUTF().trim();

            if (type.equals("giftcode") && !serial.isEmpty()) {
                handleGiftCode(serial);
                return;
            }
            sendServerMessage(serial + " " + pin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleGiftCode(String code) {
        IGiftCodeDao dao = getGiftCodeDao();

        GiftCodeEntry giftCode = dao.getGiftCode(code);
        if (giftCode == null) {
            sendServerMessage(GameString.giftCodeError1());
            return;
        }
        if (giftCode.getLimit() <= 0) {
            sendServerMessage(GameString.giftCodeError2());
            return;
        }
        if (giftCode.getExpiryDate() != null && LocalDateTime.now().isAfter(giftCode.getExpiryDate())) {
            sendServerMessage(GameString.giftCodeError3(giftCode.getExpiryDate()));
            return;
        }
        for (int i = 0; i < giftCode.getUsedPlayerIds().length; i++) {
            if (giftCode.getUsedPlayerIds()[i] == user.getPlayerId()) {
                sendServerMessage(GameString.giftCodeError4());
                return;
            }
        }
        dao.updateGiftCode(code, user.getPlayerId());
        GiftCodeRewardJson rewardData = GsonUtil.GSON.fromJson(giftCode.getReward(), GiftCodeRewardJson.class);
        if (rewardData.getXu() > 0) {
            user.updateXu(rewardData.getXu());
            sendMessageToUser(GameString.giftCodeReward(code, Utils.getStringNumber(rewardData.getXu()) + " xu"));
        }
        if (rewardData.getLuong() > 0) {
            user.updateLuong(rewardData.getLuong());
            sendMessageToUser(GameString.giftCodeReward(code, Utils.getStringNumber(rewardData.getLuong()) + " lượng"));
        }
        if (rewardData.getExp() > 0) {
            user.updateXp(rewardData.getExp());
            sendMessageToUser(GameString.giftCodeReward(code, Utils.getStringNumber(rewardData.getExp()) + " exp"));
        }
        if (rewardData.getItems() != null) {
            List<SpecialItemChestEntry> additionalItems = new ArrayList<>();
            for (SpecialItemChestJson item : rewardData.getItems()) {
                SpecialItemChestEntry newItem = new SpecialItemChestEntry();
                newItem.setItem(SpecialItemData.getSpecialItemById(item.getId()));
                if (newItem.getItem() == null) {
                    continue;
                }
                newItem.setQuantity(item.getQuantity());
                additionalItems.add(newItem);
                sendMessageToUser(GameString.giftCodeReward(code, newItem.getQuantity(), newItem.getItem().getName()));
            }
            user.updateInventory(null, null, additionalItems, null);
        }
        if (rewardData.getEquips() != null) {
            for (EquipmentChestJson json : rewardData.getEquips()) {
                EquipmentChestEntry addEquip = new EquipmentChestEntry();
                addEquip.setEquipEntry(CharacterData.getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex()));
                if (addEquip.getEquipEntry() == null) {
                    continue;
                }
                addEquip.setVipLevel(json.getVipLevel());
                addEquip.setAddPoints(json.getAddPoints());
                addEquip.setAddPercents(json.getAddPercents());
                user.addEquipment(addEquip);
                sendMessageToUser(GameString.giftCodeReward(code, addEquip.getEquipEntry().getName()));
            }
        }

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
    public void clearBullet(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            int size = dis.readByte();
            int[] x = new int[size];
            int[] y = new int[size];
            for (byte i = 0; i < size; i++) {
                x[i] = dis.readInt();
                y[i] = dis.readInt();
            }
            System.out.println(Arrays.toString(x) + " : " + Arrays.toString(y));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChangePassword(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            String oldPass = dis.readUTF().trim();
            String newPass = dis.readUTF().trim();

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
                    ds.writeByte(config.getIconVersion2());
                    if (version != config.getIconVersion2()) {
                        byte[] ab = Utils.getFile(CommonConstant.iconCacheName);
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
                    ds.writeByte(config.getValuesVersion2());
                    if (version != config.getValuesVersion2()) {
                        byte[] ab = Utils.getFile(CommonConstant.mapCacheName);
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
                        byte[] ab = Utils.getFile(CommonConstant.playerCacheName);
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
                        byte[] ab = Utils.getFile(CommonConstant.equipCacheName);
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
                        byte[] ab = Utils.getFile(CommonConstant.levelCacheName);
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
                    sendInventoryInfo();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendInventoryInfo() {
        try {
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getEquipmentChest().size());
            for (EquipmentChestEntry entry : user.getEquipmentChest()) {
                ds.writeInt(entry.getKey());
                ds.writeByte(entry.getEquipEntry().getCharacterId());
                ds.writeByte(entry.getEquipEntry().getEquipType());
                ds.writeShort(entry.getEquipEntry().getEquipIndex());
                ds.writeUTF(entry.getEquipEntry().getName());
                ds.writeByte(entry.getAddPoints().length * 2);
                for (int j = 0; j < entry.getAddPoints().length; j++) {
                    ds.writeByte(entry.getAddPoints()[j]);
                    ds.writeByte(entry.getAddPercents()[j]);
                }
                ds.writeByte(entry.getRemainingDays());
                ds.writeByte(entry.getEmptySlot());
                ds.writeByte(entry.getEquipEntry().isDisguise() ? 1 : 0);
                ds.writeByte(entry.getVipLevel());
            }
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            user.sendMessage(ms);

            ms = new Message(Cmd.MATERIAL);
            ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(user.getSpecialItemChest().size());
            for (SpecialItemChestEntry item : user.getSpecialItemChest()) {
                ds.writeByte(item.getItem().getId());
                ds.writeShort(item.getQuantity());
                ds.writeUTF(item.getItem().getName());
                ds.writeUTF(item.getItem().getDetail());
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
            int totalPoints = 0;
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
            ds.writeInt(user.getCup());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleChangeEquipment(Message ms) {
        try {
            boolean changeSuccessful = false;
            for (int i = 0; i < 5; i++) {
                int key = ms.reader().readInt();
                EquipmentChestEntry equip = user.getEquipmentByKey(key);
                if (equip == null ||
                        equip.isInUse() ||
                        equip.isExpired() ||
                        equip.getEquipEntry().isDisguise() ||
                        equip.getEquipEntry().getLevelRequirement() > user.getCurrentLevel() ||
                        equip.getEquipEntry().getCharacterId() != user.getActiveCharacterId() || equip.getEquipEntry().getEquipType() != i
                ) {
                    continue;
                }
                EquipmentChestEntry oldEquip = user.getNvEquip()[user.getActiveCharacterId()][i];
                if (oldEquip != null) {
                    oldEquip.setInUse(false);
                }
                equip.setInUse(true);
                user.getNvEquip()[user.getActiveCharacterId()][i] = equip;
                user.getEquipData()[user.getActiveCharacterId()][i] = equip.getKey();
                changeSuccessful = true;
            }
            ms = new Message(Cmd.CHANGE_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(changeSuccessful ? 1 : 0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleSendShopEquipments() {
        try {
            Message ms = new Message(Cmd.SHOP_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeShort(CharacterData.totalSaleEquipments);
            for (EquipmentEntry equip : CharacterData.EQUIPMENT_ENTRIES) {
                if (!equip.isOnSale()) {
                    continue;
                }
                ds.writeByte(equip.getCharacterId());
                ds.writeByte(equip.getEquipType());
                ds.writeShort(equip.getEquipIndex());
                ds.writeUTF(equip.getName());
                ds.writeInt(equip.getPriceXu());
                ds.writeInt(equip.getPriceLuong());
                ds.writeByte(equip.getExpirationDays());
                ds.writeByte(equip.getLevelRequirement());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEquipmentTransactions(Message ms) {
        List<EquipmentChestEntry> equipList = getSelectedEquips();
        DataInputStream dis = ms.reader();

        try {
            byte type = dis.readByte();
            switch (type) {
                case 0 -> {//Mua trang bị
                    short saleIndex = dis.readShort();
                    byte unit = dis.readByte();
                    purchaseEquipment(saleIndex, unit);
                }
                case 1 -> {//Gửi lệnh bán trang bị
                    // Đặt lại giá trị
                    userAction = null;
                    totalTransactionAmount = 0;
                    equipList.clear();

                    //Lấy dữ liệu và tính tiền
                    byte size = dis.readByte();
                    for (int i = 0; i < size; i++) {
                        int key = dis.readInt();
                        EquipmentChestEntry equip = user.getEquipmentByKey(key);
                        if (equip == null || equipList.contains(equip)) {
                            continue;
                        }
                        int remainingDays = equip.getRemainingDays();
                        if (remainingDays > 0) {
                            if (equip.getEquipEntry().getPriceXu() > 0) {
                                totalTransactionAmount += Math.round((float) (equip.getEquipEntry().getPriceXu() * remainingDays) / (equip.getEquipEntry().getExpirationDays() * 2));
                            } else if (equip.getEquipEntry().getPriceLuong() > 0) {
                                totalTransactionAmount += Math.round((float) (equip.getEquipEntry().getPriceLuong() * 1000 * remainingDays) / (equip.getEquipEntry().getExpirationDays() * 2));
                            }
                        }
                        equipList.add(equip);
                    }

                    //Gửi thông báo
                    ms = new Message(104);
                    DataOutputStream ds = ms.writer();
                    if (!equipList.isEmpty()) {//Trường hợp có trang bị hợp lệ
                        ds.writeByte(1);
                        if (equipList.size() == 1 && equipList.get(0).getEmptySlot() < 3) {//Tháo ngọc
                            userAction = UserAction.THAO_NGOC;
                            totalTransactionAmount = 0;

                            //Tính tiền gia hạn theo 25% giá ngọc
                            for (byte slotItemId : equipList.get(0).getSlots()) {
                                SpecialItemEntry item = SpecialItemData.getSpecialItemById(slotItemId);
                                if (item != null) {
                                    totalTransactionAmount += (int) (item.getPriceXu() * 0.25);
                                }
                            }
                            ds.writeUTF(GameString.thaoNgocRequest(totalTransactionAmount));
                        } else {//Bán trang bị
                            userAction = UserAction.BAN_TRANG_BI;
                            ds.writeUTF(GameString.sellTBRequest(equipList.size(), totalTransactionAmount));
                        }
                    } else {//Trường hợp không trang bị nào hợp lệ
                        ds.writeByte(0);
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 2 -> {//Xác nhận bán trang bị
                    if (userAction == UserAction.THAO_NGOC) {//Xác nhận tháo ngọc
                        if (user.getXu() < totalTransactionAmount) {
                            sendServerMessage(GameString.xuNotEnought());
                            return;
                        }

                        // Trừ phí tháo ngọc
                        user.updateXu(-totalTransactionAmount);

                        EquipmentChestEntry selectedEquipment = equipList.get(0);
                        if (selectedEquipment == null) {
                            return;
                        }

                        // Lấy lại ngọc đã ghép
                        List<SpecialItemChestEntry> recoveredGems = new ArrayList<>();
                        for (byte slotItemId : selectedEquipment.getSlots()) {
                            if (slotItemId > -1) {
                                SpecialItemChestEntry gem = new SpecialItemChestEntry((short) 1, SpecialItemData.getSpecialItemById(slotItemId));
                                if (gem.getItem() != null) {
                                    recoveredGems.add(gem);

                                    //Trừ điểm đã cộng vào trang bị
                                    selectedEquipment.subtractPoints(gem.getItem().getAbility());
                                }
                            }
                        }

                        // Đặt các slot ngọc thành trống
                        selectedEquipment.setSlots(new byte[]{-1, -1, -1});
                        selectedEquipment.setEmptySlot((byte) 3);

                        // Cập nhật rương
                        user.updateInventory(selectedEquipment, null, recoveredGems, null);

                        // Gửi thông báo thành công
                        sendServerMessage(GameString.thaoNgocSuccess());
                    } else if (userAction == UserAction.BAN_TRANG_BI) {//Xác nhận bán trang bị
                        //Kiểm tra có khóa rương không
                        if (user.isChestLocked()) {
                            sendServerMessage(GameString.chestLocked());
                            return;
                        }

                        for (EquipmentChestEntry equipment : equipList) {
                            if (equipment.isInUse()) {
                                sendServerMessage(GameString.sellTBError1());
                                return;
                            }
                            if (equipment.getEmptySlot() < 3) {
                                sendServerMessage(GameString.sellTBError2());
                                return;
                            }
                        }
                        for (EquipmentChestEntry validEquipment : equipList) {
                            user.updateInventory(null, validEquipment, null, null);
                        }
                        user.updateXu(totalTransactionAmount);
                        sendServerMessage(GameString.buySuccess());
                    }
                    userAction = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void purchaseEquipment(short saleIndex, byte unit) {
        if (user.getEquipmentChest().size() >= ServerManager.getInstance().config().getMaxEquipmentSlots()) {
            sendServerMessage(GameString.ruongNoSlot());
            return;
        }
        EquipmentEntry equipmentEntry = CharacterData.getEquipEntryBySaleIndex(saleIndex);
        if (equipmentEntry == null || (unit == 0 ? equipmentEntry.getPriceXu() : equipmentEntry.getPriceLuong()) < 0) {
            return;
        }
        if (unit == 0) {
            if (user.getXu() < equipmentEntry.getPriceXu()) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateXu(-equipmentEntry.getPriceXu());
        } else {
            if (user.getLuong() < equipmentEntry.getPriceLuong()) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateLuong(-equipmentEntry.getPriceLuong());
        }
        EquipmentChestEntry newEquip = new EquipmentChestEntry();
        newEquip.setEquipEntry(equipmentEntry);
        user.addEquipment(newEquip);
        sendServerMessage(GameString.buySuccess());
    }

    @Override
    public void handleSpinWheel(Message ms) {
        try {
            byte unit = ms.reader().readByte();
            if (unit == 0) {
                if (user.getXu() < SpinWheelConstants.XU_COST) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateXu(-SpinWheelConstants.XU_COST);
            } else {
                if (user.getLuong() < SpinWheelConstants.LUONG_COST) {
                    sendServerMessage(GameString.xuNotEnought());
                    return;
                }
                user.updateLuong(-SpinWheelConstants.LUONG_COST);
            }
            ms = new Message(Cmd.RULET);
            DataOutputStream ds = ms.writer();

            int luckyIndex = Utils.nextInt(10);
            for (byte i = 0; i < 10; i++) {
                byte type = Utils.nextByte(SpinWheelConstants.TYPE_PROBABILITIES);
                byte itemId = 0;
                int quantity = 0;

                switch (type) {
                    case 0 -> {
                        itemId = FightItemData.getRandomItem();
                        quantity = SpinWheelConstants.ITEM_COUNTS[Utils.nextInt(SpinWheelConstants.ITEM_PROBABILITIES)];
                        if (i == luckyIndex) {
                            user.updateItems(itemId, quantity);
                        }
                    }
                    case 1 -> {
                        quantity = SpinWheelConstants.XU_COUNTS[Utils.nextInt(SpinWheelConstants.XU_PROBABILITIES)];
                        if (i == luckyIndex) {
                            user.updateXu(quantity);
                        }
                    }
                    case 2 -> {
                        quantity = SpinWheelConstants.XP_COUNTS[Utils.nextInt(SpinWheelConstants.XP_PROBABILITIES)];
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
    public void getClanIcon(Message ms) {
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
            ds.writeUTF(clanDetails.getCreatedDate());
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
                ds.writeByte(memClan.getActiveCharacter());
                ds.writeByte(memClan.getOnline());
                ds.writeByte(memClan.getLever());
                ds.writeByte(memClan.getLevelPt());
                ds.writeByte(memClan.getIndex());
                ds.writeInt(memClan.getCup());
                for (int j = 0; j < 5; j++) {
                    ds.writeShort(memClan.getDataEquip()[j]);
                }
                ds.writeUTF(memClan.getContributeText());
                ds.writeUTF(memClan.getContributeCount());
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
            byte[] ab1 = Utils.getFile("res/bigImage/bigImage" + idS + ".png");
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
    public void rechargeMoney(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            switch (type) {
                case 0 -> {
                    ms = new Message(Cmd.CHARGE_MONEY_2);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(0);
                    for (PaymentEntry paymentEntry : PaymentData.PAYMENT_ENTRY_MAP.values()) {
                        ds.writeUTF(paymentEntry.getId());
                        ds.writeUTF(paymentEntry.getInfo());
                        ds.writeUTF(paymentEntry.getUrl());
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 1 -> {
                    String id = dis.readUTF();
                    PaymentEntry paymentEntry = PaymentData.PAYMENT_ENTRY_MAP.get(id);
                    if (paymentEntry != null) {
                        ms = new Message(Cmd.CHARGE_MONEY_2);
                        DataOutputStream ds = ms.writer();
                        ds.writeByte(2);
                        ds.writeUTF(paymentEntry.getMssTo());
                        ds.writeUTF(paymentEntry.getMssContent());
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
                case 0, 1 -> data = Utils.getFile("res/icon/item/" + iconId + ".png");
                case 2 -> data = Utils.getFile("res/icon/map/" + iconId + ".png");
                case 3, 4 -> {
                    indexIcon = dis.readByte();
                    data = Utils.getFile("res/icon/item/" + iconId + ".png");
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
    public void startTraining(Message ms) {
        try {
            byte type = ms.reader().readByte();
            sendServerMessage("To be continued " + type);
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
    public void sendUpdateCup(int cupUp) {
        try {
            Message ms = new Message(Cmd.CUP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(cupUp);
            ds.writeInt(user.getCup());
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

    @Override
    public void getMoreGame() {
        IServerConfig config = ServerManager.getInstance().config();
        try {
            Message ms = new Message(Cmd.MORE_GAME);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(config.getDownloadTitle());
            ds.writeUTF(config.getDownloadInfo());
            ds.writeUTF(config.getDownloadUrl());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
