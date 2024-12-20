package com.teamobi.mobiarmy2.service.impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.*;
import com.teamobi.mobiarmy2.dao.IGiftCodeDao;
import com.teamobi.mobiarmy2.dao.IUserDao;
import com.teamobi.mobiarmy2.dao.impl.GiftCodeDao;
import com.teamobi.mobiarmy2.dao.impl.UserDao;
import com.teamobi.mobiarmy2.fight.IFightWait;
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
import java.util.List;
import java.util.Map;

/**
 * @author tuyen
 */
public class UserService implements IUserService {
    private static final int minimumWaitTime = 5000;

    private final User user;
    private final IUserDao userDao;
    private final IGiftCodeDao giftCodeDao;

    private UserAction userAction;
    private int totalTransactionAmount;
    private List<EquipmentChestEntry> selectedEquips;
    private List<SpecialItemChestEntry> selectedSpecialItems;
    private FabricateItemEntry fabricateItemEntry;

    private long timeSinceLeftRoom;
    private long lastSpinTime;

    public UserService(User user) {
        this.user = user;
        this.userDao = new UserDao();
        this.giftCodeDao = new GiftCodeDao();
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

    private void sendMessage(IMessage ms) {
        user.sendMessage(ms);
    }

    private boolean isInvalidInput(String input) {
        return !input.matches(CommonConstant.ALPHANUMERIC_PATTERN);
    }

    private void sendMessageLoginFail(String message) {
        try {
            IMessage ms = new Message(Cmd.LOGIN_FAIL);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleLogin(IMessage ms) {
        if (user.isLogged()) {
            return;
        }

        if (!LeaderboardManager.getInstance().isComplete()) {
            sendMessageLoginFail(GameString.RANKING_NOT_LOADED);
            return;
        }

        ServerManager serverManager = ServerManager.getInstance();
        if (serverManager.isMaintenanceMode()) {
            sendMessageLoginFail(GameString.MAINTENANCE_MODE);
            return;
        }

        try {
            DataInputStream dis = ms.reader();
            String username = dis.readUTF();
            String password = dis.readUTF();
            String version = dis.readUTF();

            if (isInvalidInput(username) || isInvalidInput(password)) {
                sendMessageLoginFail(GameString.INVALID_ACCOUNT_PASSWORD);
                return;
            }

            User userFound = userDao.findByUsernameAndPassword(username, password);
            if (userFound == null) {
                sendMessageLoginFail(GameString.LOGIN_FAILED);
                return;
            }
            if (userFound.isLock()) {
                sendMessageLoginFail(GameString.ACCOUNT_LOCKED);
                return;
            }
            if (!userFound.isActive()) {
                sendMessageLoginFail(GameString.ACCOUNT_INACTIVE);
                return;
            }

            //Kiểm tra có đang đăng nhập hay không
            User userLogin = serverManager.getUserByPlayerId(userFound.getPlayerId());
            if (userLogin != null) {
                userLogin.getUserService().sendServerMessage2(GameString.ACCOUNT_OTHER_LOGIN);
                userLogin.getSession().close();

                sendMessageLoginFail(GameString.LOGIN_ANOTHER_DEVICE);
                return;
            }

            copyUserData(user, userFound);//todo
            user.getSession().setVersion(version);
            user.setLogged(true);

            //Kiểm tra chưa online hơn 1 ngày;
            LocalDateTime now = LocalDateTime.now();
            if (Utils.hasLoggedInOnNewDay(user.getLastOnline(), now)) {
                //Gửi item
                byte indexItem = FightItemRepository.getRandomItem();
                byte quantity = 1;
                user.updateItems(indexItem, quantity);
                sendMessageToUser(GameString.createDailyRewardMessage(quantity, FightItemRepository.FIGHT_ITEM_ENTRIES.get(indexItem).getName()));

                //Cập nhật quà top
                if (user.getTopEarningsXu() > 0) {
                    user.updateXu(user.getTopEarningsXu());
                    sendMessageToUser(GameString.createDailyTopRewardMessage(user.getTopEarningsXu()));
                    user.setTopEarningsXu(0);
                }

                //Đặt lại số lần mua nguyên liệu
                user.setMaterialsPurchased((byte) 0);

                //Gửi messeage khi login
                for (String msg : serverManager.getConfig().getMessage()) {
                    sendMessageToUser(msg);
                }

                //Cập nhật nhiệm vụ
                user.updateMission(16, 1);

                //Lưu lại lần đăng nhập
                userDao.updateLastOnline(now, user.getPlayerId());
            }

            userDao.updateOnline(true, userFound.getPlayerId());

            sendLoginSuccess();
            IServerConfig config = serverManager.getConfig();
            sendCharacterData(config);
            sendRoomCaption(config);
            sendMapCollisionInfo();
            sendServerInfoToUser(config.getMessageLogin());

            serverManager.notifyListeners();
        } catch (IOException ignored) {
        }
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendRoomName() {
        IServerConfig config = ServerManager.getInstance().getConfig();
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendMapCollisionInfo() {
        try {
            IMessage ms = new Message(Cmd.UNDESTROYTILE);
            DataOutputStream ds = ms.writer();
            ds.writeShort(MapRepository.idNotCollisions.length);
            for (short i : MapRepository.idNotCollisions) {
                ds.writeShort(i);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendServerMessage(String message) {
        try {
            IMessage ms = new Message(Cmd.SERVER_MESSAGE);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void sendServerMessage2(String message) {
        try {
            IMessage ms = new Message(Cmd.SET_MONEY_ERROR);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
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
                ds.writeUTF(GameString.createEquipmentRenewalRequestMessage(gia));
                ds.flush();
                sendMessage(ms);
            } else {
                if (user.getXu() < gia) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-gia);
                equip.setPurchaseDate(LocalDateTime.now());
                user.updateInventory(equip, null, null, null);
                sendServerMessage(GameString.EXTEND_SUCCESS);
            }
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }
    }

    private void missionComplete(byte missionId) throws IOException {
        String message;
        MissionEntry missionEntry = MissionRepository.getMissionById(missionId);
        if (missionEntry == null) {
            message = GameString.MISSION_NOT_FOUND;
        } else {
            byte missionType = missionEntry.getType();
            byte missionLevel = user.getMissionLevel()[missionType];
            byte requiredLevel = missionEntry.getLevel();

            if (user.getMission()[missionType] < missionEntry.getRequirement()) {
                message = GameString.MISSION_NOT_COMPLETED;
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
                message = GameString.createMissionCompleteMessage(missionEntry.getReward());
            } else if (missionLevel < requiredLevel) {
                message = GameString.MISSION_NOT_COMPLETED;
            } else {
                message = GameString.MISSION_COMPLETED;
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
        sendMessage(ms);
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
            ds.writeByte(0);//clan rights

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

            IServerConfig config = ServerManager.getInstance().getConfig();
            ds.writeUTF(config.getAddInfo());
            ds.writeUTF(config.getAddInfoUrl());
            ds.writeUTF(config.getRegTeamUrl());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
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

                int minXuContributeClan = ServerManager.getInstance().getConfig().getMinXuContributeClan();
                if (quantity < minXuContributeClan) {
                    sendServerMessage(GameString.createClanContributionMinXuMessage(minXuContributeClan));
                    return;
                }
                //Update xu user
                user.updateXu(-quantity);
                //Update xu clan
                ClanManager.getInstance().contributeClan(user.getClanId(), user.getPlayerId(), quantity, Boolean.TRUE);
                sendServerMessage(GameString.CONTRIBUTION_SUCCESS);
            } else if (type == 1) {
                if (quantity > user.getLuong()) {
                    return;
                }
                //Update lg user
                user.updateLuong(-quantity);
                //Update lg clan
                ClanManager.getInstance().contributeClan(user.getClanId(), user.getPlayerId(), quantity, Boolean.FALSE);
                sendServerMessage(GameString.CONTRIBUTION_SUCCESS);
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getVersionCode(IMessage ms) {
        try {
            String platform = ms.reader().readUTF();
            user.getSession().setPlatform(platform);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getProvider(IMessage ms) {
        try {
            byte provider = ms.reader().readByte();
            user.getSession().setProvider(provider);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Kiểm tra có trang bị yêu cầu không
        EquipmentChestEntry requiredEquip = user.getEquipment(formula.getRequiredEquip().getEquipIndex(), user.getActiveCharacterId(), formula.getLevel());
        if (requiredEquip == null) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Tạo một danh sách item cần xóa
        List<SpecialItemChestEntry> itemsToRemove = new ArrayList<>();

        //Kiểm tra có đủ item yêu cầu không
        for (SpecialItemChestEntry item : formula.getRequiredItems()) {
            short itemCountInInventory = user.getInventorySpecialItemCount(item.getItem().getId());
            if (itemCountInInventory < item.getQuantity()) {
                sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
                return;
            }
            itemsToRemove.add(item);
        }

        //Kiểm tra có công thức hoặc đủ xu không
        SpecialItemChestEntry material = user.getSpecialItemById(formula.getMaterial().getId());
        if (material == null && user.getXu() < formula.getMaterial().getPriceXu()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
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
            addPoints[i] = (byte) Utils.nextInt(formula.getAddPointsMin()[i], formula.getAddPointsMax()[i]);
            addPercents[i] = (byte) Utils.nextInt(formula.getAddPercentsMin()[i], formula.getAddPercentsMax()[i]);
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
        sendFormulaProcessingResult(GameString.ITEM_CRAFT_SUCCESS);
    }

    private void sendFormulaProcessingResult(String message) {
        try {
            IMessage ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void openLuckyGift(IMessage ms) {
        try {
            byte index = ms.reader().readByte();
            user.getGiftBoxManager().openGiftBoxAfterFight(index);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handlePurchaseClanItem(IMessage ms) {
        if (user.getClanId() == null) {
            sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
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
        } catch (IOException ignored) {
        }
    }

    private void buyClanShop(byte unit, byte itemId) {
        ClanManager clanManager = ClanManager.getInstance();
        ClanItemEntry clanItemEntry = ClanItemRepository.getItemClanById(itemId);

        if (clanItemEntry == null || clanItemEntry.getOnSale() != 1) {
            return;
        }

        int currentLevel = clanManager.getClanLevel(user.getClanId());
        if (currentLevel < clanItemEntry.getLevel()) {
            sendServerMessage(GameString.CLAN_LEVEL_INSUFFICIENT);
            return;
        }

        if (unit == 0) {//Xu
            if (clanItemEntry.getXu() < 0) {
                return;
            }
            int xuClan = clanManager.getClanXu(user.getClanId());
            if (xuClan < clanItemEntry.getXu()) {
                sendServerMessage(GameString.CLAN_NOT_ENOUGH_XU);
                return;
            }

            clanManager.updateItemClan(user.getClanId(), user.getPlayerId(), clanItemEntry, true);
        } else if (unit == 1) {//Luong
            if (clanItemEntry.getLuong() < 0) {
                return;
            }
            int luongClan = clanManager.getClanLuong(user.getClanId());
            if (luongClan < clanItemEntry.getLuong()) {
                sendServerMessage(GameString.CLAN_NOT_ENOUGH_LUONG);
                return;
            }

            clanManager.updateItemClan(user.getClanId(), user.getPlayerId(), clanItemEntry, false);
        }
        sendServerMessage(GameString.PURCHASE_SUCCESS);
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }
    }

    private void purchaseSpecialItem(byte unit, byte itemId, byte quantity) {
        //Kiểm tra số lượng mua hợp lệ
        if (quantity < 1) {
            return;
        }

        //Kiểm tra số lượng đang có trong rương
        if (user.getInventorySpecialItemCount(itemId) + quantity > ServerManager.getInstance().getConfig().getMaxSpecialItemSlots()) {
            sendServerMessage(GameString.CHEST_MAXIMUM_REACHED);
            return;
        }

        SpecialItemEntry item = SpecialItemRepository.getSpecialItemById(itemId);
        if (item == null || !item.isOnSale() || (unit == 0 ? item.getPriceXu() : item.getPriceLuong()) < 0) {
            return;
        }

        //Giới hạn số lần mua vật liệu
        if (item.isMaterial()) {
            if (user.getMaterialsPurchased() >= GameConstants.MAX_MATERIAL_PURCHASE_LIMIT) {
                sendServerMessage(GameString.MATERIAL_PURCHASE_LIMIT);
                return;
            } else if (user.getMaterialsPurchased() + quantity > GameConstants.MAX_MATERIAL_PURCHASE_LIMIT) {
                sendServerMessage(GameString.createMaterialPurchaseLimitMessage(GameConstants.MAX_MATERIAL_PURCHASE_LIMIT - user.getMaterialsPurchased()));
                return;
            }
        }

        if (unit == 0) {//Mua bằng xu
            int totalPrice = quantity * item.getPriceXu();
            if (user.getXu() < totalPrice) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-totalPrice);
        } else {//Mua bằng lượng
            int totalPrice = quantity * item.getPriceLuong();
            if (user.getLuong() < totalPrice) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
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
        sendServerMessage(GameString.PURCHASE_SUCCESS);
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
            for (Map.Entry<Byte, SpecialItemEntry> entry : SpecialItemRepository.getSpecialItems().entrySet()) {
                SpecialItemEntry spEntry = entry.getValue();
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
                int priceChatServer = ServerManager.getInstance().getConfig().getPriceChatServer();

                if (user.getXu() < priceChatServer) {
                    return;
                }
                user.updateXu(-priceChatServer);
                sendServerInfoToServer(GameString.createMessageFromSender(user.getUsername(), content));
                return;
            }
            User receiver = ServerManager.getInstance().getUserByPlayerId(playerId);
            if (receiver == null) {
                return;
            }
            sendMessageToUser(receiver, content);
        } catch (IOException ignored) {
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
                sendMessage(ms);
            }
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
                sendServerMessage(GameString.NO_CLAN_MEMBERSHIP);
                return;
            }
            ms = new Message(Cmd.BOARD_LIST);
            DataOutputStream ds = ms.writer();
            ds.writeByte(roomNumber);
            for (IFightWait fightWait : room.getFightWaits()) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleJoinBoard(IMessage ms) {
        if (user.isNotWaiting()) {
            return;
        }

        long timeRemaining = minimumWaitTime - (System.currentTimeMillis() - timeSinceLeftRoom);
        if (timeRemaining > 0) {
            sendServerMessage(GameString.createJoinAreaErrorMessage((int) (timeRemaining / 1000) + 1));
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
            IFightWait[] fightWaits = rooms[roomNumber].getFightWaits();
            if (areaNumber < 0 || areaNumber >= fightWaits.length) {
                return;
            }
            IFightWait fightWait = fightWaits[areaNumber];
            if (fightWait.isPassSet() && !fightWait.getPassword().equals(password)) {
                sendServerMessage(GameString.AREA_INCORRECT_PASSWORD);
                return;
            }
            fightWait.addUser(user);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleKickPlayer(IMessage ms) {
        try {
            int playerId = ms.reader().readInt();
            user.getFightWait().kickPlayer(user.getPlayerId(), playerId);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
                            specialItemList.getFirst().getItem().isGem()
                    ) {
                        userAction = UserAction.INSERT_GEM_INTO_EQUIPMENT;
                        sendMessageConfirm(GameString.GEM_COMBINE_REQUEST);
                    } else {
                        sendServerMessage(GameString.COMBINE_ERROR);
                    }
                    return;
                }

                if (!specialItemList.isEmpty()) {
                    fabricateItemEntry = FabricateItemRepository.getFabricateItem(specialItemList);
                    if (fabricateItemEntry != null) {
                        userAction = UserAction.COMBINE_SPECIAL_ITEM;
                        sendMessageConfirm(fabricateItemEntry.getConfirmationMessage());
                        return;
                    }

                    if (specialItemList.size() == 1) {
                        SpecialItemChestEntry itemChestEntry = specialItemList.getFirst();
                        if (itemChestEntry.getItem().isGem()) {
                            if (itemChestEntry.getQuantity() == 5 && ((itemChestEntry.getItem().getId() + 1) % 10 != 0)) {
                                userAction = UserAction.UPGRADE_GEM;
                                sendMessageConfirm(GameString.createGemFusionRequestMessage((90 - (itemChestEntry.getItem().getId() % 10) * 10)));
                            } else {
                                userAction = UserAction.SELL_GEM;
                                totalTransactionAmount = itemChestEntry.getSellPrice();
                                sendMessageConfirm(GameString.createGemSellRequestMessage(itemChestEntry.getQuantity(), totalTransactionAmount));
                            }
                            return;
                        }

                        if (itemChestEntry.getItem().isUsable()) {
                            userAction = UserAction.USE_SPECIAL_ITEM;
                            confirmSpecialItemUse(itemChestEntry);
                            return;
                        }
                    }
                }
                sendServerMessage(GameString.COMBINE_ERROR);
            } else if (action == 1) {
                switch (userAction) {
                    case INSERT_GEM_INTO_EQUIPMENT -> {
                        EquipmentChestEntry equip = equipList.getFirst();
                        SpecialItemChestEntry specialItem = specialItemList.getFirst();
                        if (equip.getEmptySlot() >= specialItem.getQuantity()) {
                            for (int i = 0; i < specialItem.getQuantity(); i++) {
                                equip.setNewSlot(specialItem.getItem().getId());
                                equip.decrementEmptySlot();
                                equip.addPoints(specialItem.getItem().getAbility());
                            }
                            user.updateInventory(equip, null, null, specialItemList);
                            sendServerMessage(GameString.GEM_COMBINE_SUCCESS);
                        } else {
                            sendServerMessage(GameString.GEM_COMBINE_NO_SLOT);
                        }
                    }
                    case UPGRADE_GEM -> {
                        SpecialItemChestEntry specialItemChestEntry = specialItemList.getFirst();
                        int successRate = (90 - (specialItemChestEntry.getItem().getId() % 10) * 10);
                        int randomNumber = Utils.nextInt(100);
                        if (randomNumber < successRate) {
                            SpecialItemChestEntry newItem = new SpecialItemChestEntry();
                            newItem.setQuantity((short) 1);
                            newItem.setItem(SpecialItemRepository.getSpecialItemById((byte) (specialItemChestEntry.getItem().getId() + 1)));

                            user.updateInventory(null, null, List.of(newItem), List.of(specialItemChestEntry));
                            sendServerMessage(GameString.createGemUpgradeSuccessMessage(newItem.getQuantity(), newItem.getItem().getName()));
                        } else {
                            specialItemChestEntry.setQuantity((short) 1);
                            user.updateInventory(null, null, null, List.of(specialItemChestEntry));
                            sendServerMessage(GameString.COMBINE_FAILURE);
                        }
                    }

                    case SELL_GEM -> {
                        if (user.isChestLocked()) {
                            sendServerMessage(GameString.CHEST_LOCKED_NO_SELL);
                            return;
                        }
                        user.updateInventory(null, null, null, specialItemList);
                        user.updateXu(totalTransactionAmount);
                        sendServerMessage(GameString.PURCHASE_SUCCESS);
                    }

                    case USE_SPECIAL_ITEM -> handleUseSpecialItem(specialItemList.getFirst());

                    case COMBINE_SPECIAL_ITEM -> {
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
                                .toList();

                        user.updateInventory(null, null, addItems, specialItemList);

                        if (!fabricateItemEntry.getCompletionMessage().isEmpty()) {
                            sendServerMessage(fabricateItemEntry.getCompletionMessage());
                        }
                    }
                }

                //Đặt lại dữ liệu
                userAction = null;
            }
        } catch (IOException ignored) {
        }
    }

    private void handleUseSpecialItem(SpecialItemChestEntry itemChestEntry) {
        switch (itemChestEntry.getItem().getId()) {
            case 54 -> {
                user.addDaysToXpX2Time(1);
                user.updateInventory(null, null, null, List.of(itemChestEntry));
                sendServerMessage(GameString.ITEM_X2_XP_USAGE_SUCCESS);
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
                    sendServerMessage(GameString.USE_BANH_TRUNG_SUCCESS);
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
                    sendServerMessage(GameString.USE_BANH_TET_SUCCESS);
                }
            }
        }
    }

    private void confirmSpecialItemUse(SpecialItemChestEntry itemChestEntry) {
        switch (itemChestEntry.getItem().getId()) {
            case 54 -> {
                if (itemChestEntry.getQuantity() == 1) {
                    sendMessageConfirm(GameString.ITEM_X2_XP_USAGE_REQUEST);
                }
            }
            case 86 -> {
                if (itemChestEntry.getQuantity() == 50) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_1);
                } else if (itemChestEntry.getQuantity() == 100) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_2);
                } else if (itemChestEntry.getQuantity() == 150) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_3);
                } else {
                    sendMessageConfirm(GameString.USE_BANH_TRUNG_REQUEST);
                }
            }
            case 87 -> {
                if (itemChestEntry.getQuantity() == 50) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_1);
                } else if (itemChestEntry.getQuantity() == 100) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_2);
                } else if (itemChestEntry.getQuantity() == 150) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_3);
                } else {
                    sendMessageConfirm(GameString.USE_BANH_TET_REQUEST);
                }
            }
            default -> sendServerMessage(GameString.COMBINE_ERROR);
        }
    }

    private void sendMessageConfirm(String message) {
        try {
            IMessage ms = new Message(Cmd.IMBUE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void processShootingResult(IMessage ms) {
        //todo
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleJoinAnyBoard(IMessage ms) {
        ServerManager server = ServerManager.getInstance();
        Room[] rooms = server.getRooms();
        IFightWait fightWait = null;
        try {
            int type = ms.reader().readByte();
            switch (type) {
                // Đấu trùm
                case 5 -> {
                    int start = server.getConfig().getStartMapBoss();
                    int end = start + server.getConfig().getRoomQuantity()[5];

                    outerLoop:
                    for (int i = start; i < end; i++) {
                        Room room = rooms[i];
                        for (IFightWait fight : room.getFightWaits()) {
                            if (!fight.isStarted() &&
                                    !fight.isPassSet() &&
                                    !fight.isContinuous() &&
                                    fight.getNumPlayers() < fight.getMaxSetPlayers() &&
                                    fight.getMoney() <= user.getXu()
                            ) {
                                fightWait = fight;
                                break outerLoop;
                            }
                        }
                    }
                }

                //4vs4->1vs1
                case 4, 3, 2, 1 -> {
                    int end = server.getConfig().getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
                    Room room = rooms[index];
                    for (IFightWait fight : room.getFightWaits()) {
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
                }

                //Khu vực trống
                case 0 -> {
                    int end = server.getConfig().getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
                    Room room = rooms[index];
                    for (IFightWait fight : room.getFightWaits()) {
                        if (!fight.isStarted() &&
                                !fight.isPassSet() &&
                                fight.getMoney() <= user.getXu() &&
                                fight.getNumPlayers() == 0
                        ) {
                            fightWait = fight;
                            break;
                        }
                    }
                }

                //Ngẫu nhiên
                case -1 -> {
                    int end = server.getConfig().getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
                    Room room = rooms[index];
                    for (IFightWait fight : room.getFightWaits()) {
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
            }

            if (fightWait == null) {
                sendServerMessage2(GameString.AREA_NOT_FOUND);
            } else {
                fightWait.sendInfo(user);
                fightWait.addUser(user);
            }
        } catch (IOException ignored) {
        }
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleAddFriend(IMessage ms) {
        try {
            Integer id = ms.reader().readInt();
            if (user.getFriends().size() > ServerManager.getInstance().getConfig().getMaxFriends()) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
                ds.writeUTF(GameString.NO_RANKING);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleFindPlayer(IMessage ms) {
        try {
            String username = ms.reader().readUTF().trim();
            if (username.isEmpty()) {
                sendMessageLoginFail(GameString.FRIEND_ADD_MISSING_NAME);
                return;
            }
            if (isInvalidInput(username)) {
                sendMessageLoginFail(GameString.FRIEND_ADD_INVALID_NAME);
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSetFightWaitName(IMessage ms) {
        try {
            String name = ms.reader().readUTF().trim();
            user.getFightWait().setRoomName(user.getPlayerId(), name);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSetMaxPlayerFightWait(IMessage ms) {
        try {
            byte maxPlayers = ms.reader().readByte();
            user.getFightWait().setMaxPlayers(user.getPlayerId(), maxPlayers);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
            sendMessage(ms);

            sendCharacterInfo();
            sendEquipInfo();
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            if (user.getItems()[itemIndex] + quantity > ServerManager.getInstance().getConfig().getMaxItem()) {
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
            ms = new Message(Cmd.BUY_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(itemIndex);
            ds.writeByte(user.getItems()[itemIndex]);
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            sendMessage(ms);
            sendServerMessage(GameString.PURCHASE_SUCCESS);
        } catch (IOException ignored) {
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
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-characterEntry.getPriceXu());
            } else {
                if (characterEntry.getPriceLuong() <= 0) {
                    return;
                }
                if (user.getLuong() < characterEntry.getPriceLuong()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateLuong(-characterEntry.getPriceLuong());
            }

            if (userDao.createPlayerCharacter(user.getPlayerId(), index)) {
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
                    sendMessage(ms);
                }
            }
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleSelectMap(IMessage ms) {
        try {
            byte mapId = ms.reader().readByte();
            user.getFightWait().setMap(user.getPlayerId(), mapId);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }
    }

    private void handleGiftCode(String code) {
        GiftCodeEntry giftCode = giftCodeDao.getGiftCode(code, user.getPlayerId());
        if (giftCode == null) {
            sendServerMessage(GameString.GIFT_CODE_INVALID);
            return;
        }
        if (giftCode.isUsed()) {
            sendServerMessage(GameString.GIFT_CODE_ALREADY_USED);
            return;
        }
        if (giftCode.getLimit() <= 0) {
            sendServerMessage(GameString.GIFT_CODE_LIMIT_REACHED);
            return;
        }
        if (giftCode.getExpiryDate() != null && LocalDateTime.now().isAfter(giftCode.getExpiryDate())) {
            String formattedDate = Utils.formatLocalDateTime(giftCode.getExpiryDate());
            sendServerMessage(GameString.createGiftCodeExpiryMessage(formattedDate));
            return;
        }

        giftCodeDao.decrementGiftCodeUsageLimit(giftCode.getId());
        giftCodeDao.logGiftCodeRedemption(giftCode.getId(), user.getPlayerId());

        if (giftCode.getXu() > 0) {
            user.updateXu(giftCode.getXu());
            sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getXu()) + " xu"));
        }
        if (giftCode.getLuong() > 0) {
            user.updateLuong(giftCode.getLuong());
            sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getLuong()) + " lượng"));
        }
        if (giftCode.getExp() > 0) {
            user.updateXp(giftCode.getExp());
            sendMessageToUser(GameString.createGiftCodeRewardMessage(code, Utils.getStringNumber(giftCode.getExp()) + " exp"));
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
                sendMessageToUser(GameString.createGiftCodeRewardMessageWithQuantity(code, newItem.getQuantity(), newItem.getItem().getName()));
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
                sendMessageToUser(GameString.createGiftCodeRewardMessage(code, addEquip.getEquipEntry().getName()));
            }
        }

        sendServerMessage(GameString.GIFT_CODE_SUCCESS);
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
        } catch (IOException ignored) {
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
            //todo
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleChangePassword(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            String oldPass = dis.readUTF().trim();
            String newPass = dis.readUTF().trim();

            if (isInvalidInput(oldPass) || isInvalidInput(newPass)) {
                sendServerMessage(GameString.PASSWORD_INVALID_CHARACTER);
                return;
            }

            if (!userDao.existsByUserIdAndPassword(user.getUserId(), oldPass)) {
                sendServerMessage(GameString.PASSWORD_INCORRECT_OLD);
                return;
            }

            userDao.changePassword(user.getUserId(), newPass);
            sendServerMessage(GameString.PASSWORD_CHANGE_SUCCESS);
        } catch (IOException ignored) {
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
                    IServerConfig config = ServerManager.getInstance().getConfig();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getIconVersion2());
                    if (version != config.getIconVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.ICON_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }

                case 2 -> {
                    IServerConfig config = ServerManager.getInstance().getConfig();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getValuesVersion2());
                    if (version != config.getValuesVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.MAP_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 3 -> {
                    IServerConfig config = ServerManager.getInstance().getConfig();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getPlayerVersion2());
                    if (version != config.getPlayerVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.PLAYER_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 4 -> {
                    IServerConfig config = ServerManager.getInstance().getConfig();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getEquipVersion2());
                    if (version != config.getEquipVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.EQUIP_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeInt(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 5 -> {
                    IServerConfig config = ServerManager.getInstance().getConfig();
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(config.getLevelCVersion2());
                    if (version != config.getLevelCVersion2()) {
                        byte[] ab = Utils.getFile(GameConstants.LEVEL_CACHE_NAME);
                        if (ab == null) {
                            return;
                        }
                        ds.writeShort(ab.length);
                        ds.write(ab);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 6 -> {
                    sendCharacterInfo();
                    sendInventoryInfo();
                }
            }
        } catch (IOException ignored) {
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
            sendMessage(ms);

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
            sendMessage(ms);
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
                    ms = new Message(Cmd.BUY_EQUIP);
                    DataOutputStream ds = ms.writer();
                    if (!equipList.isEmpty()) {//Trường hợp có trang bị hợp lệ
                        ds.writeByte(1);
                        if (equipList.size() == 1 && equipList.getFirst().getEmptySlot() < 3) {//Tháo ngọc
                            userAction = UserAction.REMOVE_GEM_FROM_EQUIPMENT;
                            totalTransactionAmount = 0;

                            //Tính tiền gia hạn theo 25% giá ngọc
                            for (byte slotItemId : equipList.getFirst().getSlots()) {
                                SpecialItemEntry item = SpecialItemRepository.getSpecialItemById(slotItemId);
                                if (item != null) {
                                    totalTransactionAmount += (int) (item.getPriceXu() * 0.25);
                                }
                            }
                            ds.writeUTF(GameString.createGemRemovalRequestMessage(totalTransactionAmount));
                        } else {//Bán trang bị
                            userAction = UserAction.SELL_EQUIPMENT;
                            ds.writeUTF(GameString.createEquipmentSellRequestMessage(equipList.size(), totalTransactionAmount));
                        }
                    } else {//Trường hợp không trang bị nào hợp lệ
                        ds.writeByte(0);
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 2 -> {//Xác nhận bán trang bị
                    if (userAction == UserAction.REMOVE_GEM_FROM_EQUIPMENT) {//Xác nhận tháo ngọc
                        if (user.getXu() < totalTransactionAmount) {
                            sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                            return;
                        }

                        //Trừ phí tháo ngọc
                        user.updateXu(-totalTransactionAmount);

                        EquipmentChestEntry selectedEquipment = equipList.getFirst();
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
                        sendServerMessage(GameString.GEM_REMOVAL_SUCCESS);
                    } else if (userAction == UserAction.SELL_EQUIPMENT) {//Xác nhận bán trang bị
                        //Kiểm tra có khóa rương không
                        if (user.isChestLocked()) {
                            sendServerMessage(GameString.CHEST_LOCKED_NO_SELL);
                            return;
                        }

                        for (EquipmentChestEntry equipment : equipList) {
                            if (equipment.isInUse()) {
                                sendServerMessage(GameString.EQUIP_SELL_ERROR_IN_USE);
                                return;
                            }
                            if (equipment.getEmptySlot() < 3) {
                                sendServerMessage(GameString.EQUIP_SELL_ERROR_REMOVE_GEMS);
                                return;
                            }
                        }
                        for (EquipmentChestEntry validEquipment : equipList) {
                            user.updateInventory(null, validEquipment, null, null);
                        }
                        user.updateXu(totalTransactionAmount);
                        sendServerMessage(GameString.PURCHASE_SUCCESS);
                    }
                    userAction = null;
                }
            }
        } catch (IOException ignored) {
        }
    }

    private void purchaseEquipment(short saleIndex, byte unit) {
        if (user.getEquipmentChest().size() >= ServerManager.getInstance().getConfig().getMaxEquipmentSlots()) {
            sendServerMessage(GameString.CHEST_NO_SPACE);
            return;
        }
        EquipmentEntry equipmentEntry = CharacterRepository.getEquipEntryBySaleIndex(saleIndex);
        if (equipmentEntry == null || (unit == 0 ? equipmentEntry.getPriceXu() : equipmentEntry.getPriceLuong()) < 0) {
            return;
        }
        if (unit == 0) {
            if (user.getXu() < equipmentEntry.getPriceXu()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-equipmentEntry.getPriceXu());
        } else {
            if (user.getLuong() < equipmentEntry.getPriceLuong()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-equipmentEntry.getPriceLuong());
        }
        EquipmentChestEntry newEquip = new EquipmentChestEntry();
        newEquip.setEquipEntry(equipmentEntry);
        user.addEquipment(newEquip);
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    @Override
    public void handleSpinWheel(IMessage ms) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < 5000) {
            sendServerMessage(GameString.SPIN_WAIT_TIME);
            return;
        }
        IServerConfig config = ServerManager.getInstance().getConfig();
        try {
            byte unit = ms.reader().readByte();
            if (unit == 0) {
                if (user.getXu() < config.getSpinXuCost()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-config.getSpinXuCost());
            } else {
                if (user.getLuong() < config.getSpinLuongCost()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateLuong(-config.getSpinLuongCost());
            }
            ms = new Message(Cmd.RULET);
            DataOutputStream ds = ms.writer();
            int luckyIndex = Utils.nextInt(10);
            for (byte i = 0; i < 10; i++) {
                byte type = (byte) Utils.nextInt(config.getSpinTypeProbabilities());
                byte itemId = 0;
                int quantity = 0;

                switch (type) {
                    case 0 -> {
                        itemId = FightItemRepository.getRandomItem();
                        quantity = config.getSpinItemCounts()[0][Utils.nextInt(config.getSpinItemCounts()[1])];
                        if (i == luckyIndex) {
                            user.updateItems(itemId, (byte) quantity);
                        }
                    }
                    case 1 -> {
                        quantity = config.getSpinXuCounts()[0][Utils.nextInt(config.getSpinXuCounts()[1])];
                        if (i == luckyIndex) {
                            user.updateXu(quantity);
                        }
                    }
                    case 2 -> {
                        quantity = config.getSpinXpCounts()[0][Utils.nextInt(config.getSpinXpCounts()[1])];
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
            sendMessage(ms);

            lastSpinTime = currentTime;
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getInfoClan(IMessage ms) {
        try {
            short clanId = ms.reader().readShort();
            ClanInfo clanDetails = ClanManager.getInstance().getClanInfo(clanId);
            if (clanDetails == null) {
                sendMessageLoginFail(GameString.CLAN_NOT_FOUND);
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
                ds.writeByte(memClan.getLevel());
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getBigImage(IMessage ms) {
        try {
            int id = ms.reader().readByte();
            ms = new Message(Cmd.GET_BIG_IMAGE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(id);
            byte[] file = Utils.getFile(String.format(GameConstants.BIG_IMAGE_PATH, id));
            if (file != null) {
                ds.writeShort(file.length);
                ds.write(file);
            } else {
                ds.writeShort(0);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void handleRegister(IMessage ms) {
        sendMessageLoginFail(GameString.REGISTRATION_REQUIRED);
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
                    sendMessage(ms);
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
                        sendMessage(ms);
                    }
                }
            }
        } catch (IOException ignored) {
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
                case 0, 1 -> data = Utils.getFile(String.format(GameConstants.ITEM_ICON_PATH, iconId));
                case 2 -> data = Utils.getFile(String.format(GameConstants.MAP_ICON_PATH, iconId));
                case 3, 4 -> {
                    indexIcon = dis.readByte();
                    data = Utils.getFile(String.format(GameConstants.ITEM_ICON_PATH, iconId));
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
                sendMessage(ms);
            }
        } catch (IOException ignored) {
        }
    }

    private void initializeTrainingManager() {
        if (user.getTrainingManager() == null) {
            user.setTrainingManager(new TrainingManager(user, ServerManager.getInstance().getConfig().getTrainingMapId()));
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
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void ping(IMessage ms) {
    }

    @Override
    public void getMoreGame() {
        IServerConfig config = ServerManager.getInstance().getConfig();
        try {
            IMessage ms = new Message(Cmd.MORE_GAME);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(config.getDownloadTitle());
            ds.writeUTF(config.getDownloadInfo());
            ds.writeUTF(config.getDownloadUrl());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void getStringMessage(IMessage ms) {
        DataInputStream dis = ms.reader();
        try {
            String str = dis.readUTF();
        } catch (IOException ignored) {
        }
    }
}
