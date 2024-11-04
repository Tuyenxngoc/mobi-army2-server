package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.*;
import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.GiftCodeDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.fight.impl.FightWait;
import com.teamobi.mobiarmy2.fight.impl.TrainingManager;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.*;
import com.teamobi.mobiarmy2.model.clan.ClanEntry;
import com.teamobi.mobiarmy2.model.clan.ClanInfo;
import com.teamobi.mobiarmy2.model.clan.ClanItem;
import com.teamobi.mobiarmy2.model.clan.ClanMemEntry;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.item.ClanItemEntry;
import com.teamobi.mobiarmy2.model.item.FightItemEntry;
import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;
import com.teamobi.mobiarmy2.model.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.user.FriendEntry;
import com.teamobi.mobiarmy2.model.user.PlayerLeaderboardEntry;
import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.repository.*;
import com.teamobi.mobiarmy2.server.ClanManager;
import com.teamobi.mobiarmy2.server.LeaderboardManager;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
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
    private static final int minimumWaitTime = 5000;

    private final User user;
    private final IUserDao userDao;
    private IGiftCodeDao giftCodeDao;

    private UserAction userAction;
    private int totalTransactionAmount;
    private List<EquipmentChestEntry> selectedEquips;
    private List<SpecialItemChestEntry> selectedSpecialItems;
    private FabricateItemEntry fabricateItemEntry;

    private long timeSinceLeftRoom;

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
            IMessage ms = new Message(4);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleLogin(IMessage ms) {
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
                byte indexItem = FightItemRepository.getRandomItem();
                byte quantity = 1;
                user.updateItems(indexItem, quantity);
                sendMessageToUser(GameString.dailyReward(quantity, FightItemRepository.FIGHT_ITEM_ENTRIES.get(indexItem).getName()));

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
            ServerManager.getInstance().logger().logException(UserService.class, e);
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
        target.setPointEvent(source.getPointEvent());
        target.setClanId(source.getClanId());
        target.setLevels(source.getLevels());
        target.setLevelPercents(source.getLevelPercents());
        target.setActiveCharacterId(source.getActiveCharacterId());
        target.setPlayerCharacterIds(source.getPlayerCharacterIds());
        target.setOwnedCharacters(source.getOwnedCharacters());
        target.setXps(source.getXps());
        target.setPoints(source.getPoints());
        target.setAddedPoints(source.getAddedPoints());
        target.setEquipData(source.getEquipData());
        target.setCharacterEquips(source.getCharacterEquips());
        target.setFriends(source.getFriends());
        target.setMission(source.getMission());
        target.setMissionLevel(source.getMissionLevel());
        target.setSpecialItemChest(source.getSpecialItemChest());
        target.setEquipmentChest(source.getEquipmentChest());
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
            List<CharacterEntry> characterEntries = CharacterRepository.CHARACTER_ENTRIES;
            int characterCount = characterEntries.size();
            IMessage ms = new Message(Cmd.SKIP_2);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    public void sendRoomName() {
        IServerConfig config = ServerManager.getInstance().config();
        String[] names = config.getBossRoomName();
        int startMapBoss = config.getStartMapBoss();
        try {
            IMessage ms = new Message(Cmd.CHANGE_ROOM_NAME);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void sendRoomCaption(IServerConfig config) {
        String[] names = config.getRoomNameVi();
        try {
            IMessage ms = new Message(Cmd.ROOM_CAPTION);
            DataOutputStream ds = ms.writer();
            ds.writeByte(names.length);
            for (int i = 0; i < names.length; i++) {
                ds.writeUTF(names[i]);
                ds.writeUTF(config.getRoomNameEn()[i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    public void sendMapCollisionInfo() {
        try {
            IMessage ms = new Message(92);
            DataOutputStream ds = ms.writer();
            ds.writeShort(MapRepository.idNotCollisions.length);
            for (short i : MapRepository.idNotCollisions) {
                ds.writeShort(i);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void sendServerMessage(String message) {
        try {
            IMessage ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void sendServerMessage2(String message) {
        try {
            IMessage ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleHandshakeMessage() {
        user.getSession().sendKeys();
    }

    @Override
    public void extendItemDuration(IMessage ms) {
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
                SpecialItemEntry item = SpecialItemRepository.getSpecialItemById(itemId);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleGetMissions(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void missionComplete(byte missionId) throws IOException {
        String message;
        MissionEntry missionEntry = MissionRepository.getMissionById(missionId);
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
        IMessage ms = new Message(Cmd.MISSISON);
        DataOutputStream ds = ms.writer();
        int i = 0;
        for (List<MissionEntry> missionEntryList : MissionRepository.MISSION_LIST.values()) {
            int index = user.getMissionLevel()[i] - 1;//Subtracting 1 to access the correct index
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
            IMessage ms = new Message(Cmd.LOGIN_SUCESS);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getPlayerId());
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.writeByte(user.getActiveCharacterId());
            ds.writeShort(user.getClanId() != null ? user.getClanId() : 0);
            ds.writeByte(0);

            for (int i = 0; i < 10; i++) {
                EquipmentChestEntry equip = user.getCharacterEquips()[i][5];
                if (equip != null) {
                    ds.writeBoolean(true);
                    for (short s : equip.getEquipEntry().getDisguiseEquippedIndexes()) {
                        ds.writeShort(s);
                    }
                } else {
                    ds.writeBoolean(false);
                }

                for (int j = 0; j < 5; j++) {
                    if (user.getCharacterEquips()[i][j] != null) {
                        ds.writeShort(user.getCharacterEquips()[i][j].getEquipEntry().getEquipIndex());
                    } else if (User.equipDefault[i][j] != null) {
                        ds.writeShort(User.equipDefault[i][j].getEquipIndex());
                    } else {
                        ds.writeShort(-1);
                    }
                }
            }

            for (int i = 0; i < FightItemRepository.FIGHT_ITEM_ENTRIES.size(); i++) {
                ds.writeByte(user.getItems()[i]);
                FightItemEntry fightItemEntry = FightItemRepository.FIGHT_ITEM_ENTRIES.get(i);
                ds.writeInt(fightItemEntry.getBuyXu());
                ds.writeInt(fightItemEntry.getBuyLuong());
            }

            for (int i = 0; i < 10; i++) {
                if (i > 2) {
                    ds.writeByte(user.getOwnedCharacters()[i] ? 1 : 0);
                    CharacterEntry characterEntry = CharacterRepository.CHARACTER_ENTRIES.get(i);
                    ds.writeShort(characterEntry.getPriceXu() / 1000);
                    ds.writeShort(characterEntry.getPriceLuong());
                }
            }

            IServerConfig config = ServerManager.getInstance().config();
            ds.writeUTF(config.getAddInfo());
            ds.writeUTF(config.getAddInfoUrl());
            ds.writeUTF(config.getRegTeamUrl());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void contributeToClan(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }
        if (user.getClanId() == null) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getVersionCode(IMessage ms) {
        try {
            String platform = ms.reader().readUTF();
            user.getSession().setPlatform(platform);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getProvider(IMessage ms) {
        try {
            byte provider = ms.reader().readByte();
            user.getSession().setProvider(provider);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleMergeEquipments(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void processFormulaCrafting(byte id, byte level) {
        Map<Byte, List<FormulaEntry>> formulaMap = FormulaRepository.FORMULA.get(id);
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
            IMessage ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void sendFormulaInfo(byte id) {
        try {
            Map<Byte, List<FormulaEntry>> formulaMap = FormulaRepository.FORMULA.get(id);
            if (formulaMap == null) {
                return;
            }
            List<FormulaEntry> formulaEntries = formulaMap.get(user.getActiveCharacterId());
            if (formulaEntries == null) {
                return;
            }
            IMessage ms = new Message(Cmd.FOMULA);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void openLuckyGift(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void viewLeaderboard(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handlePurchaseClanItem(IMessage ms) {
        if (user.getClanId() == null) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void buyClanShop(byte unit, byte itemId) {
        ClanManager clanManager = ClanManager.getInstance();
        ClanItemEntry clanItemEntry = ClanItemRepository.getItemClanById(itemId);

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
            IMessage ms = new Message(Cmd.SHOP_BIETDOI);
            DataOutputStream ds = ms.writer();
            ds.writeByte(ClanItemRepository.CLAN_ITEM_ENTRY_MAP.size());
            for (ClanItemEntry clanItemEntry : ClanItemRepository.CLAN_ITEM_ENTRY_MAP.values()) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void enterTrainingMap() {
        try {
            initializeTrainingManager();
            IMessage ms = new Message(Cmd.TRAINING_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getTrainingManager().getMapId());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleLogout(IMessage ms) {
        user.getSession().close();
    }

    @Override
    public void handleSpecialItemShop(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
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

        SpecialItemEntry item = SpecialItemRepository.getSpecialItemById(itemId);
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

        if (unit == 0) {//Mua bằng xu
            int totalPrice = quantity * item.getPriceXu();
            if (user.getXu() < totalPrice) {
                sendServerMessage(GameString.xuNotEnought());
                return;
            }
            user.updateXu(-totalPrice);
        } else {//Mua bằng lượng
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
            IMessage ms = new Message(Cmd.SHOP_LINHTINH);
            DataOutputStream ds = ms.writer();
            for (SpecialItemEntry spEntry : SpecialItemRepository.SPECIAL_ITEM_ENTRIES) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void equipVipItems(IMessage ms) {
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
            EquipmentChestEntry oldEquip = user.getCharacterEquips()[user.getActiveCharacterId()][5];
            if (oldEquip != null) {
                oldEquip.setInUse(false);
            }
            ms = new Message(Cmd.VIP_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(action);
            if (action == 0) {
                user.getEquipData()[user.getActiveCharacterId()][5] = -1;
                user.getCharacterEquips()[user.getActiveCharacterId()][5] = null;
            } else {
                equip.setInUse(true);
                user.getEquipData()[user.getActiveCharacterId()][5] = equip.getKey();
                user.getCharacterEquips()[user.getActiveCharacterId()][5] = equip;
                for (short a : equip.getEquipEntry().getDisguiseEquippedIndexes()) {
                    ds.writeShort(a);
                }
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSendMessage(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            int playerId = dis.readInt();
            String content = dis.readUTF().trim();
            if (content.isEmpty() || content.length() > 100) {
                return;
            }
            //Neu la admin -> bo qua
            if (playerId == 1) {
                return;
            }
            //Neu la nguoi dua tin -> send Mss 46-> chat The gioi
            if (playerId == 2) {
                //10000xu/lan
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void sendServerInfo(String message, boolean toServer) {
        if (message == null || message.isEmpty()) {
            return;
        }
        try {
            IMessage ms = new Message(Cmd.SERVER_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();

            if (toServer) {
                ServerManager.getInstance().sendToServer(ms);
            } else {
                user.sendMessage(ms);
            }
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
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
            IMessage ms = new Message(Cmd.CHAT_TO);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSendRoomList() {
        if (user.isNotWaiting()) {
            return;
        }
        ServerManager server = ServerManager.getInstance();
        try {
            IMessage ms = new Message(Cmd.ROOM_LIST);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleEnteringRoom(IMessage ms) {
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
            if (room.getType() == 6 && user.getClanId() == null) {
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
                ds.writeBoolean(fightWait.isStarted());
                ds.writeUTF(fightWait.getName());
                ds.writeByte(fightWait.getRoom().getIconType());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleJoinBoard(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }

        long timeRemaining = minimumWaitTime - (System.currentTimeMillis() - timeSinceLeftRoom);
        if (timeRemaining > 0) {
            sendServerMessage(GameString.joinKVError4((int) (timeRemaining / 1000) + 1));
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
            fightWait.addUser(user);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleChatMessage(IMessage ms) {
        try {
            String message = ms.reader().readUTF().trim();
            if (message.isEmpty() || message.length() > 100) {
                return;
            }
            user.getFightWait().chatMessage(user.getPlayerId(), message);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleKickPlayer(IMessage ms) {
        try {
            int playerId = ms.reader().readInt();
            user.getFightWait().kickPlayer(user.getPlayerId(), playerId);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleLeaveBoard(IMessage ms) {
        if (user.getState() == UserState.WAITING) {
            return;
        }
        user.getFightWait().leaveTeam(user.getPlayerId());
        timeSinceLeftRoom = System.currentTimeMillis();
    }

    @Override
    public void setReady(IMessage ms) {
        try {
            boolean ready = ms.reader().readBoolean();
            user.getFightWait().setReady(ready, user.getPlayerId());
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void imbueGem(IMessage ms) {
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
                    fabricateItemEntry = FabricateItemRepository.getFabricateItem(specialItemList);
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
                            newItem.setItem(SpecialItemRepository.getSpecialItemById((byte) (specialItemChestEntry.getItem().getId() + 1)));

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
            ServerManager.getInstance().logger().logException(UserService.class, e);
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
            IMessage ms = new Message(17);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSetPasswordFightWait(IMessage ms) {
        try {
            String password = ms.reader().readUTF().trim();
            if (password.isEmpty() || password.length() > 10) {
                return;
            }
            user.getFightWait().setPassRoom(password, user.getPlayerId());
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSetMoneyFightWait(IMessage ms) {
        try {
            int xu = ms.reader().readInt();
            if (xu < 0) {
                return;
            }
            user.getFightWait().setMoney(xu, user.getPlayerId());
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
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
    public void movePlayer(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            short x = dis.readShort();
            short y = dis.readShort();

            if (user.getState() == UserState.FIGHTING) {
                user.getFightWait().getFightManager().changeLocation(user.getPlayerId(), x, y);
            } else if (user.getState() == UserState.TRAINING) {
                user.getTrainingManager().changeLocation(x, y);
            }
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void shoot(IMessage ms) {
        if (user.getState() != UserState.FIGHTING) {
            return;
        }
        DataInputStream dis = ms.reader();
        try {
            byte bullId = dis.readByte();
            short x = dis.readShort();
            short y = dis.readShort();
            short angle = (short) Utils.clamp(dis.readShort(), -360, 360);
            byte force = (byte) Utils.clamp(dis.readByte(), 0, 30);
            byte force2 = 0;
            if (bullId == 17 || bullId == 19) {
                force2 = (byte) Utils.clamp(dis.readByte(), 0, 30);
            }
            byte numShoot = dis.readByte();

            user.getFightWait().getFightManager().addShoot(user.getPlayerId(), bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void processShootingResult(IMessage ms) {

    }

    @Override
    public void handleUseItem(IMessage ms) {
        try {
            byte itemIndex = ms.reader().readByte();
            if (itemIndex != 100) {
                if (itemIndex < 0 || itemIndex >= FightItemRepository.FIGHT_ITEM_ENTRIES.size()) {
                    return;
                }

                if (user.getItemFightQuantity(itemIndex) < 1) {
                    return;
                }
            }
            user.getFightWait().getFightManager().useItem(user.getPlayerId(), itemIndex);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleJoinAnyBoard(IMessage ms) {
        ServerManager server = ServerManager.getInstance();
        Room[] rooms = server.getRooms();
    }

    @Override
    public void handleViewFriendList() {
        try {
            IMessage ms = new Message(Cmd.FRIENDLIST);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleAddFriend(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
            sendMessageUpdateFriends(Boolean.FALSE, 1);
        }
    }

    @Override
    public void handleRemoveFriend(IMessage ms) {
        try {
            Integer id = ms.reader().readInt();
            user.getFriends().remove(id);
            sendMessageUpdateFriends(Boolean.TRUE, 0);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
            sendMessageUpdateFriends(Boolean.TRUE, 1);
        }
    }

    private void sendMessageUpdateFriends(boolean isDelete, int status) {
        try {
            IMessage ms;
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleGetFlayerDetail(IMessage ms) {
        try {
            int playerId = ms.reader().readInt();
            User us = null;
            if (playerId == user.getPlayerId()) {
                us = user;
            } else if (user.isNotWaiting()) {
                us = user.getFightWait().getUserByPlayerId(playerId);
            }
            ms = new Message(Cmd.PLAYER_DETAIL);
            DataOutputStream ds = ms.writer();
            if (us == null) {
                ds.writeInt(-1);
            } else {
                ds.writeInt(us.getPlayerId());
                ds.writeUTF(us.getUsername());
                ds.writeInt(us.getXu());
                ds.writeByte(us.getCurrentLevel());
                ds.writeByte(us.getCurrentLevelPercent());
                ds.writeInt(us.getLuong());
                ds.writeInt(us.getCurrentXp());
                ds.writeInt(us.getCurrentRequiredXp());
                ds.writeInt(us.getCup());
                ds.writeUTF(GameString.notRanking());
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleFindPlayer(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void skipTurn() {
        user.getFightWait().getFightManager().skipTurn(user.getPlayerId());
    }

    @Override
    public void updateCoordinates(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            short x = dis.readShort();
            short y = dis.readShort();

            ServerManager.getInstance().logger().log("Update coordinates: x=" + x + "  y=" + y);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSetFightWaitName(IMessage ms) {
        try {
            String name = ms.reader().readUTF().trim();
            user.getFightWait().setRoomName(user.getPlayerId(), name);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSetMaxPlayerFightWait(IMessage ms) {
        try {
            byte maxPlayers = ms.reader().readByte();
            user.getFightWait().setMaxPlayers(user.getPlayerId(), maxPlayers);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleChoseItemFight(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleChoseCharacter(IMessage ms) {
        try {
            byte characterId = ms.reader().readByte();
            if (characterId >= CharacterRepository.CHARACTER_ENTRIES.size() || characterId < 0 || !user.getOwnedCharacters()[characterId]) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void sendEquipInfo() {
        try {
            IMessage ms = new Message(Cmd.CURR_EQUIP_DBKEY);
            DataOutputStream ds = ms.writer();
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleChangeTeam(IMessage ms) {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().changeTeam(user);
    }

    @Override
    public void handlePurchaseItem(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte unit = dis.readByte();
            byte itemIndex = dis.readByte();
            byte quantity = dis.readByte();
            if (itemIndex < 0 || itemIndex >= FightItemRepository.FIGHT_ITEM_ENTRIES.size()) {
                return;
            }
            if (user.getItems()[itemIndex] + quantity > ServerManager.getInstance().config().getMaxItem()) {
                return;
            }
            if (unit == 0) {
                int total = FightItemRepository.FIGHT_ITEM_ENTRIES.get(itemIndex).getBuyXu() * quantity;
                if (user.getXu() < total || total < 0) {
                    return;
                }
                user.updateXu(-total);
            } else {
                int total = FightItemRepository.FIGHT_ITEM_ENTRIES.get(itemIndex).getBuyLuong() * quantity;
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleBuyCharacter(IMessage ms) {
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
            CharacterEntry characterEntry = CharacterRepository.CHARACTER_ENTRIES.get(index);
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
                    user.getAddedPoints()[index] = character.getAdditionalPoints();
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSelectMap(IMessage ms) {
        try {
            byte mapId = ms.reader().readByte();
            user.getFightWait().setMap(user.getPlayerId(), mapId);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleCardRecharge(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void handleGiftCode(String code) {
        IGiftCodeDao dao = getGiftCodeDao();

        GiftCodeEntry giftCode = dao.getGiftCode(code, user.getPlayerId());
        if (giftCode == null) {
            sendServerMessage(GameString.giftCodeError1());
            return;
        }
        if (giftCode.isUsed()) {
            sendServerMessage(GameString.giftCodeError4());
            return;
        }
        if (giftCode.getLimit() <= 0) {
            sendServerMessage(GameString.giftCodeError2());
            return;
        }
        if (giftCode.getExpiryDate() != null && LocalDateTime.now().isAfter(giftCode.getExpiryDate())) {
            String formattedDate = Utils.formatLocalDateTime(giftCode.getExpiryDate());
            sendServerMessage(GameString.giftCodeError3(formattedDate));
            return;
        }

        dao.decrementGiftCodeUsageLimit(giftCode.getId());
        dao.logGiftCodeRedemption(giftCode.getId(), user.getPlayerId());

        if (giftCode.getXu() > 0) {
            user.updateXu(giftCode.getXu());
            sendMessageToUser(GameString.giftCodeReward(code, Utils.getStringNumber(giftCode.getXu()) + " xu"));
        }
        if (giftCode.getLuong() > 0) {
            user.updateLuong(giftCode.getLuong());
            sendMessageToUser(GameString.giftCodeReward(code, Utils.getStringNumber(giftCode.getLuong()) + " lượng"));
        }
        if (giftCode.getExp() > 0) {
            user.updateXp(giftCode.getExp());
            sendMessageToUser(GameString.giftCodeReward(code, Utils.getStringNumber(giftCode.getExp()) + " exp"));
        }
        if (giftCode.getItems() != null) {
            List<SpecialItemChestEntry> additionalItems = new ArrayList<>();
            for (SpecialItemChestJson item : giftCode.getItems()) {
                SpecialItemChestEntry newItem = new SpecialItemChestEntry();
                newItem.setItem(SpecialItemRepository.getSpecialItemById(item.getId()));
                if (newItem.getItem() == null) {
                    continue;
                }
                newItem.setQuantity(item.getQuantity());
                additionalItems.add(newItem);
                sendMessageToUser(GameString.giftCodeReward(code, newItem.getQuantity(), newItem.getItem().getName()));
            }
            user.updateInventory(null, null, additionalItems, null);
        }
        if (giftCode.getEquips() != null) {
            for (EquipmentChestJson json : giftCode.getEquips()) {
                EquipmentChestEntry addEquip = new EquipmentChestEntry();
                addEquip.setEquipEntry(CharacterRepository.getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex()));
                if (addEquip.getEquipEntry() == null) {
                    continue;
                }
                addEquip.setAddPoints(json.getAddPoints());
                addEquip.setAddPercents(json.getAddPercents());
                user.addEquipment(addEquip);
                sendMessageToUser(GameString.giftCodeReward(code, addEquip.getEquipEntry().getName()));
            }
        }

        sendServerMessage(GameString.giftCodeSuccess());
    }

    @Override
    public void handleFindPlayerWait(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void clearBullet(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            int size = dis.readByte();
            int[] x = new int[size];
            int[] y = new int[size];
            for (byte i = 0; i < size; i++) {
                x[i] = dis.readInt();
                y[i] = dis.readInt();
            }

            ServerManager.getInstance().logger().log("Clear bullets: x=" + Arrays.toString(x) + "  y=" + Arrays.toString(y));
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleChangePassword(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getFilePack(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void sendInventoryInfo() {
        try {
            IMessage ms = new Message(Cmd.INVENTORY);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleAddPoints(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
        sendCharacterInfo();
    }

    @Override
    public void sendCharacterInfo() {
        try {
            IMessage ms = new Message(Cmd.CHARACTOR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getCurrentLevel());
            ds.writeByte(user.getCurrentLevelPercent());
            ds.writeShort(user.getCurrentPoint());
            for (short point : user.getCurrentAddedPoints()) {
                ds.writeShort(point);
            }
            ds.writeInt(user.getCurrentXp());
            ds.writeInt(user.getCurrentRequiredXp());
            ds.writeInt(user.getCup());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleChangeEquipment(IMessage ms) {
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
                EquipmentChestEntry oldEquip = user.getCharacterEquips()[user.getActiveCharacterId()][i];
                if (oldEquip != null) {
                    oldEquip.setInUse(false);
                }
                equip.setInUse(true);
                user.getCharacterEquips()[user.getActiveCharacterId()][i] = equip;
                user.getEquipData()[user.getActiveCharacterId()][i] = equip.getKey();
                changeSuccessful = true;
            }
            ms = new Message(Cmd.CHANGE_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(changeSuccessful ? 1 : 0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSendShopEquipments() {
        try {
            IMessage ms = new Message(Cmd.SHOP_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeShort(CharacterRepository.totalSaleEquipments);
            for (EquipmentEntry equip : CharacterRepository.EQUIPMENT_ENTRIES) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleEquipmentTransactions(IMessage ms) {
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
                    //Đặt lại giá trị
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
                                SpecialItemEntry item = SpecialItemRepository.getSpecialItemById(slotItemId);
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

                        //Trừ phí tháo ngọc
                        user.updateXu(-totalTransactionAmount);

                        EquipmentChestEntry selectedEquipment = equipList.get(0);
                        if (selectedEquipment == null) {
                            return;
                        }

                        //Lấy lại ngọc đã ghép
                        List<SpecialItemChestEntry> recoveredGems = new ArrayList<>();
                        for (byte slotItemId : selectedEquipment.getSlots()) {
                            if (slotItemId > -1) {
                                SpecialItemChestEntry gem = new SpecialItemChestEntry((short) 1, SpecialItemRepository.getSpecialItemById(slotItemId));
                                if (gem.getItem() != null) {
                                    recoveredGems.add(gem);

                                    //Trừ điểm đã cộng vào trang bị
                                    selectedEquipment.subtractPoints(gem.getItem().getAbility());
                                }
                            }
                        }

                        //Đặt các slot ngọc thành trống
                        selectedEquipment.setSlots(new byte[]{-1, -1, -1});
                        selectedEquipment.setEmptySlot((byte) 3);

                        //Cập nhật rương
                        user.updateInventory(selectedEquipment, null, recoveredGems, null);

                        //Gửi thông báo thành công
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void purchaseEquipment(short saleIndex, byte unit) {
        if (user.getEquipmentChest().size() >= ServerManager.getInstance().config().getMaxEquipmentSlots()) {
            sendServerMessage(GameString.ruongNoSlot());
            return;
        }
        EquipmentEntry equipmentEntry = CharacterRepository.getEquipEntryBySaleIndex(saleIndex);
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
    public void handleSpinWheel(IMessage ms) {
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
                        itemId = FightItemRepository.getRandomItem();
                        quantity = SpinWheelConstants.ITEM_COUNTS[Utils.nextInt(SpinWheelConstants.ITEM_PROBABILITIES)];
                        if (i == luckyIndex) {
                            user.updateItems(itemId, (byte) quantity);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getClanIcon(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getTopClan(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getInfoClan(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getClanMember(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getBigImage(IMessage ms) {
        try {
            int id = ms.reader().readByte();
            ms = new Message(Cmd.GET_BIG_IMAGE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(id);
            byte[] file = Utils.getFile("res/bigImage/bigImage" + id + ".png");
            if (file != null) {
                ds.writeShort(file.length);
                ds.write(file);
            } else {
                ds.writeShort(0);
            }
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleRegister(IMessage ms) {
        sendMessageLoginFail(GameString.reg_Error6());
    }

    @Override
    public void rechargeMoney(IMessage ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            switch (type) {
                case 0 -> {
                    ms = new Message(Cmd.CHARGE_MONEY_2);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(0);
                    for (PaymentEntry paymentEntry : PaymentRepository.PAYMENT_ENTRY_MAP.values()) {
                        ds.writeUTF(paymentEntry.getId());
                        ds.writeUTF(paymentEntry.getInfo());
                        ds.writeUTF(paymentEntry.getUrl());
                    }
                    ds.flush();
                    user.sendMessage(ms);
                }
                case 1 -> {
                    String id = dis.readUTF();
                    PaymentEntry paymentEntry = PaymentRepository.PAYMENT_ENTRY_MAP.get(id);
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getMaterialIconMessage(IMessage ms) {
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void startTraining(IMessage ms) {
        try {
            byte type = ms.reader().readByte();

            initializeTrainingManager();

            if (type == 0) {//Start game
                if (user.isNotWaiting()) {
                    return;
                }

                user.setState(UserState.TRAINING);
                user.getTrainingManager().startTraining();
            } else {//Out game
                if (user.getState() != UserState.TRAINING) {
                    return;
                }

                user.setState(UserState.WAITING);
                user.getTrainingManager().stopTraining();

                ms = new Message(Cmd.TRAINING);
                DataOutputStream ds = ms.writer();
                ds.writeByte(1);
                ds.flush();
                user.sendMessage(ms);
            }
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    private void initializeTrainingManager() {
        if (user.getTrainingManager() == null) {
            user.setTrainingManager(new TrainingManager(user, ServerManager.getInstance().config().getTrainingMapId()));
        }
    }

    @Override
    public void trainShooting(IMessage ms) {
        if (user.getState() != UserState.TRAINING) {
            return;
        }

        DataInputStream dis = ms.reader();
        try {
            byte bullId = dis.readByte();
            short x = dis.readShort();
            short y = dis.readShort();
            short angle = (short) Utils.clamp(dis.readShort(), -360, 360);
            byte force = (byte) Utils.clamp(dis.readByte(), 0, 30);
            byte force2 = 0;
            if (bullId == 17 || bullId == 19) {
                force2 = (byte) Utils.clamp(dis.readByte(), 0, 30);
            }
            byte numShoot = dis.readByte();

            user.getTrainingManager().addShoot(user, bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void sendUpdateMoney() {
        try {
            IMessage ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void sendUpdateCup(int cupUp) {
        try {
            IMessage ms = new Message(Cmd.CUP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(cupUp);
            ds.writeInt(user.getCup());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void sendUpdateXp(int xpUp, boolean updateLevel) {
        try {
            IMessage ms = new Message(Cmd.UPDATE_EXP);
            DataOutputStream ds = ms.writer();
            ds.writeInt(xpUp);
            ds.writeInt(user.getCurrentXp());
            ds.writeInt(user.getCurrentRequiredXp());
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
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void ping(IMessage ms) {
    }

    @Override
    public void getMoreGame() {
        IServerConfig config = ServerManager.getInstance().config();
        try {
            IMessage ms = new Message(Cmd.MORE_GAME);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(config.getDownloadTitle());
            ds.writeUTF(config.getDownloadInfo());
            ds.writeUTF(config.getDownloadUrl());
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void handleSendAgentAndProviders() {
        try {
            IMessage ms = new Message(Cmd.GET_AGENT_PROVIDER);
            DataOutputStream ds = ms.writer();
            ds.writeUTF("none");
            ds.writeByte(0);
            ds.flush();
            user.sendMessage(ms);
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }

    @Override
    public void getStringMessage(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            String str = dis.readUTF();
        } catch (IOException e) {
            ServerManager.getInstance().logger().logException(UserService.class, e);
        }
    }
}
