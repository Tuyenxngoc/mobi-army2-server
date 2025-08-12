package com.teamobi.mobiarmy2.service;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.*;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dto.FriendDTO;
import com.teamobi.mobiarmy2.dto.GiftCodeDTO;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.entity.*;
import com.teamobi.mobiarmy2.entity.Character;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.fight.TrainingManager;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

public class UserService extends BaseService {
    private static final int minimumWaitTime = 5000;

    @Setter
    private User user;

    private final ServerConfig serverConfig;
    private final LeaderboardService leaderboardService;
    private final LoginRateLimiterService loginRateLimiterService;

    private final UserDAO userDAO;
    private final AccountDAO accountDAO;
    private final GiftCodeDAO giftCodeDAO;
    private final UserGiftCodeDAO userGiftCodeDAO;
    private final UserCharacterDAO userCharacterDAO;

    private UserAction userAction;
    private int totalTransactionAmount;
    private List<EquipmentChest> selectedEquips;
    private List<SpecialItemChest> selectedSpecialItems;
    private FabricateItem fabricateItem;

    private long timeSinceLeftRoom;
    private long lastSpinTime;

    public UserService(
            Session session,
            ServerConfig serverConfig,
            LeaderboardService leaderboardService,
            LoginRateLimiterService loginRateLimiterService,
            UserDAO userDAO, AccountDAO accountDAO,
            GiftCodeDAO giftCodeDAO, UserGiftCodeDAO userGiftCodeDAO,
            UserCharacterDAO userCharacterDAO) {
        super(session);
        this.serverConfig = serverConfig;
        this.leaderboardService = leaderboardService;
        this.loginRateLimiterService = loginRateLimiterService;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
        this.giftCodeDAO = giftCodeDAO;
        this.userGiftCodeDAO = userGiftCodeDAO;
        this.userCharacterDAO = userCharacterDAO;
    }

    private static String getFormattedRankDisplay(int rank) {
        if (rank < 10_000) {
            return String.format("Top %s", rank);
        } else if (rank < 100_000) {
            return String.format("Top %s+", Utils.getStringNumber(rank));
        } else {
            return "Top 100k+";
        }
    }

    private List<EquipmentChest> getSelectedEquips() {
        if (selectedEquips == null) {
            selectedEquips = new ArrayList<>();
        }
        return selectedEquips;
    }

    private List<SpecialItemChest> getSelectedSpecialItems() {
        if (selectedSpecialItems == null) {
            selectedSpecialItems = new ArrayList<>();
        }
        return selectedSpecialItems;
    }

    public void handleLogout() {
        if (user.getState() == UserState.FIGHTING || user.getState() == UserState.WAIT_FIGHT) {
            user.getFightWait().leaveTeam(user.getUserId());
        }

        //Cập nhật thông tin tài khoản
        userDAO.update(user);

        //Cập nhật thông tin nhân vật
        List<UserCharacterDTO> userCharacterDTOS = new ArrayList<>();
        for (byte i = 0; i < user.getOwnedCharacters().length; i++) {
            if (user.getOwnedCharacters()[i]) {
                UserCharacterDTO userCharacterDTO = getUserCharacterDTO(i);
                userCharacterDTOS.add(userCharacterDTO);
            }
        }
        userCharacterDAO.updateAll(userCharacterDTOS);

        user.setLogged(false);

        //Lưu thời gian đăng xuất gần nhất
        loginRateLimiterService.saveLogoutTime(user.getUsername());
    }

    private UserCharacterDTO getUserCharacterDTO(byte i) {
        UserCharacterDTO userCharacterDTO = new UserCharacterDTO();
        userCharacterDTO.setCharacterId(i);
        userCharacterDTO.setUserId(user.getUserId());
        userCharacterDTO.setLevel(user.getLevels()[i]);
        userCharacterDTO.setXp(user.getXps()[i]);
        userCharacterDTO.setPoints(user.getPoints()[i]);
        userCharacterDTO.setAdditionalPoints(user.getAddedPoints()[i]);
        userCharacterDTO.setData(user.getEquipData()[i]);
        return userCharacterDTO;
    }

    public void sendRoomName() {
        String[] names = serverConfig.getBossRoomName();
        int startMapBoss = serverConfig.getStartMapBoss();
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void extendItemDuration(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int key = dis.readInt();
            EquipmentChest equip = user.getEquipmentByKey(key);
            if (equip == null) {
                return;
            }
            int gia = 0;
            for (byte itemId : equip.getSlots()) {
                SpecialItem item = SpecialItemManager.getSpecialItemById(itemId);
                if (item != null) {
                    gia += item.getPriceXu();
                }
            }
            gia /= 20;
            if (equip.getEquipment().getPriceXu() > 0) {
                gia += equip.getEquipment().getPriceXu();
            } else if (equip.getEquipment().getPriceLuong() > 0) {
                gia += equip.getEquipment().getPriceLuong() * 1000;
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

    public void handleGetMissions(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            byte action = ms.reader().readByte();
            if (action == 0) {
                sendMissionInfo();
            } else {
                byte missionId = ms.reader().readByte();
                missionComplete(missionId);
            }
        } catch (IOException ignored) {
        }
    }

    private void missionComplete(byte missionId) throws IOException {
        String message;
        Mission mission = MissionManager.getMissionById(missionId);
        if (mission == null) {
            message = GameString.MISSION_NOT_FOUND;
        } else {
            byte missionType = mission.getType();
            byte missionLevel = user.getMissionLevel()[missionType];
            byte requiredLevel = mission.getLevel();

            if (user.getMission()[missionType] < mission.getRequirement()) {
                message = GameString.MISSION_NOT_COMPLETED;
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
                    user.updateCup(mission.getRewardCup());
                }
                sendMissionInfo();
                message = GameString.createMissionCompleteMessage(mission.getReward());
            } else if (missionLevel < requiredLevel) {
                message = GameString.MISSION_NOT_COMPLETED;
            } else {
                message = GameString.MISSION_COMPLETED;
            }
        }
        sendMoneyErrorMessage(message);
    }

    private void sendMissionInfo() throws IOException {
        Message ms = new Message(Cmd.MISSISON);
        DataOutputStream ds = ms.writer();
        int i = 0;
        for (List<Byte> missionIds : MissionManager.MISSIONS_BY_TYPE.values()) {
            int index = user.getMissionLevel()[i] - 1;
            if (index >= missionIds.size()) {
                index = missionIds.size() - 1;
            }
            Mission mission = MissionManager.getMissionById(missionIds.get(index));
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
        sendMessage(ms);
    }

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
        } catch (IOException ignored) {
        }
    }

    private void processFormulaCrafting(byte id, byte level) {
        Map<Byte, List<Formula>> formulaMap = FormulaManager.FORMULAS.get(id);
        if (formulaMap == null) {
            return;
        }
        List<Formula> formulas = formulaMap.get(user.getActiveCharacterId());
        if (formulas == null) {
            return;
        }
        Formula formula = formulas.get(level);
        if (formula == null) {
            return;
        }

        //Kiểm tra có đủ level chế đồ yêu cầu không
        if (user.getCurrentLevel() < formula.getLevelRequired()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Kiểm tra có trang bị yêu cầu không
        EquipmentChest requiredEquip = user.getEquipment(formula.getRequiredEquip().getEquipIndex(), user.getActiveCharacterId(), formula.getLevel());
        if (requiredEquip == null) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        }

        //Tạo một danh sách item cần xóa
        List<SpecialItemChest> itemsToRemove = new ArrayList<>();

        //Kiểm tra có đủ item yêu cầu không
        for (SpecialItemChest item : formula.getRequiredItems()) {
            short itemCountInInventory = user.getInventorySpecialItemCount(item.getItem().getId());
            if (itemCountInInventory < item.getQuantity()) {
                sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
                return;
            }
            itemsToRemove.add(item);
        }

        //Kiểm tra có công thức hoặc đủ xu không
        SpecialItemChest material = user.getSpecialItemById(formula.getMaterial().getId());
        if (material == null && user.getXu() < formula.getMaterial().getPriceXu()) {
            sendFormulaProcessingResult(GameString.ITEM_CRAFT_FAILURE);
            return;
        } else {
            if (material != null) {//Nếu có công thức thì thêm vào danh sách item xóa
                itemsToRemove.add(new SpecialItemChest((short) 1, material.getItem()));
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
        EquipmentChest newEquip = new EquipmentChest();
        newEquip.setEquipment(formula.getResultEquip());
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
            Message ms = new Message(Cmd.FOMULA);
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
            Map<Byte, List<Formula>> formulaMap = FormulaManager.FORMULAS.get(id);
            if (formulaMap == null) {
                return;
            }
            List<Formula> formulaEntries = formulaMap.get(user.getActiveCharacterId());
            if (formulaEntries == null) {
                return;
            }
            Message ms = new Message(Cmd.FOMULA);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(id);
            ds.writeByte(formulaEntries.size());
            for (Formula formula : formulaEntries) {
                boolean hasRequiredItem = true;
                boolean hasRequiredEquip = user.hasEquipment(formula.getRequiredEquip().getEquipIndex(), formula.getLevel());
                boolean hasRequiredLevel = user.getCurrentLevel() >= formula.getLevelRequired();

                ds.writeByte(formula.getResultEquip().getEquipIndex());
                ds.writeUTF("%s cấp %d".formatted(formula.getResultEquip().getName(), (formula.getLevel() + 1)));
                ds.writeByte(formula.getLevelRequired());
                ds.writeByte(formula.getCharacterId());
                ds.writeByte(formula.getEquipType());
                ds.writeByte(formula.getRequiredItems().size());
                for (SpecialItemChest item : formula.getRequiredItems()) {
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

    public void openLuckyGift(Message ms) {
        try {
            byte index = ms.reader().readByte();
            user.getGiftBoxService().openGiftBoxAfterFight(index);
        } catch (IOException ignored) {
        }
    }

    public void viewLeaderboard(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            byte page = dis.readByte();

            if (type >= LeaderboardService.CATEGORIES.length) {
                return;
            }
            ms = new Message(Cmd.BANGTHANHTICH);
            DataOutputStream ds = ms.writer();
            ds.writeByte(type);
            if (type < 0) {
                ds.writeByte(LeaderboardService.CATEGORIES.length);
                for (String name : LeaderboardService.CATEGORIES) {
                    ds.writeUTF(name);
                }
            } else {
                //Kiểm tra page num
                int maxPage = leaderboardService.getTotalPageByType(type);
                if (page > maxPage || page >= 10) {
                    page = 0;
                }
                if (page < 0) {
                    page = (byte) maxPage;
                }
                //Gửi dữ liệu
                ds.writeByte(page);
                ds.writeUTF(LeaderboardService.LABELS[type]);
                List<UserLeaderboardDTO> bangXH = leaderboardService.getUsers(type, page, 10);
                if (bangXH != null) {
                    for (UserLeaderboardDTO pl : bangXH) {
                        ds.writeInt(pl.getUserId());
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

    public void enterTrainingMap() {
        try {
            initializeTrainingManager();
            Message ms = new Message(Cmd.TRAINING_MAP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(user.getTrainingManager().getMapId());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleLogout(Message ms) {
        user.getSession().close();
    }

    public void handleSpecialItemShop(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            if (type == 0) {
                sendSpecialItem();
            } else {
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
        if (user.getInventorySpecialItemCount(itemId) + quantity > serverConfig.getMaxSpecialItemSlots()) {
            sendServerMessage(GameString.CHEST_MAXIMUM_REACHED);
            return;
        }

        SpecialItem item = SpecialItemManager.getSpecialItemById(itemId);
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
            SpecialItemChest newItem = new SpecialItemChest(quantity, item);

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
        Message ms = CacheManager.cachedSpecialItemShop;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        try {
            ms = new Message(Cmd.SHOP_LINHTINH);
            DataOutputStream ds = ms.writer();
            Map<Byte, SpecialItem> sorted = new TreeMap<>(SpecialItemManager.SPECIAL_ITEMS);
            for (SpecialItem specialItem : sorted.values()) {
                if (!specialItem.isOnSale()) {
                    continue;
                }
                ds.writeByte(specialItem.getId());
                ds.writeUTF(specialItem.getName());
                ds.writeUTF(specialItem.getDetail());
                ds.writeInt(specialItem.getPriceXu());
                ds.writeInt(specialItem.getPriceLuong());
                ds.writeByte(specialItem.getExpirationDays());
                ds.writeByte(specialItem.isShowSelection() ? 0 : 1);
            }
            ds.flush();

            CacheManager.cachedSpecialItemShop = ms;

            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void equipVipItems(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            int key = dis.readInt();
            EquipmentChest equip = user.getEquipmentByKey(key);
            if (equip == null ||
                    equip.isExpired() ||
                    !equip.getEquipment().isDisguise() ||
                    equip.getEquipment().getLevelRequirement() > user.getCurrentLevel() ||
                    equip.getEquipment().getCharacterId() != user.getActiveCharacterId()
            ) {
                return;
            }
            EquipmentChest oldEquip = user.getCharacterEquips()[user.getActiveCharacterId()][5];
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
                for (short a : equip.getEquipment().getDisguiseEquippedIndexes()) {
                    ds.writeShort(a);
                }
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleSendMessage(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            int userId = dis.readInt();
            String content = dis.readUTF().trim();
            if (content.isEmpty() || content.length() > 100) {
                return;
            }
            //Neu la admin -> bo qua
            if (userId == 1) {
                return;
            }
            //Neu la nguoi dua tin -> chat The gioi
            if (userId == 2) {
                int priceChatServer = serverConfig.getPriceChatServer();
                if (user.getXu() < priceChatServer) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-priceChatServer);
                sendServerInfo(GameString.createMessageFromSender(user.getUsername(), content), true);
                return;
            }
            User receiver = ApplicationContext.getInstance()
                    .getBean(ServerManager.class).getUserByUserId(userId);
            if (receiver == null) {
                sendServerMessage(GameString.INVITE_OFFLINE);
                return;
            }
            sendMessageToUser(false, receiver, content);
        } catch (IOException ignored) {
        }
    }

    public void handleSendRoomList() {
        if (user.isNotWaiting()) {
            return;
        }
        RoomManager roomManager = ApplicationContext.getInstance()
                .getBean(RoomManager.class);
        try {
            Message ms = new Message(Cmd.ROOM_LIST);
            DataOutputStream ds = ms.writer();
            for (Room room : roomManager.getRooms()) {
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

    public void handleEnteringRoom(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }
        Room[] rooms = ApplicationContext.getInstance()
                .getBean(RoomManager.class).getRooms();
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
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleJoinBoard(Message ms) {
        if (user.isNotWaiting()) {
            return;
        }

        long timeRemaining = minimumWaitTime - (System.currentTimeMillis() - timeSinceLeftRoom);
        if (timeRemaining > 0) {
            sendServerMessage(GameString.createJoinAreaErrorMessage((int) (timeRemaining / 1000) + 1));
            return;
        }

        Room[] rooms = ApplicationContext.getInstance()
                .getBean(RoomManager.class).getRooms();
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
                sendServerMessage(GameString.AREA_INCORRECT_PASSWORD);
                return;
            }
            fightWait.addUser(user);
        } catch (IOException ignored) {
        }
    }

    public void handleChatMessage(Message ms) {
        try {
            String message = ms.reader().readUTF().trim();
            if (message.isEmpty() || message.length() > 100) {
                return;
            }
            user.getFightWait().chatMessage(user.getUserId(), message);
        } catch (IOException ignored) {
        }
    }

    public void handleKickPlayer(Message ms) {
        try {
            int userId = ms.reader().readInt();
            user.getFightWait().kickPlayer(user.getUserId(), userId);
        } catch (IOException ignored) {
        }
    }

    public void handleLeaveBoard(Message ms) {
        if (user.getState() == UserState.WAITING) {
            return;
        }
        user.getFightWait().leaveTeam(user.getUserId());
        timeSinceLeftRoom = System.currentTimeMillis();
    }

    public void setReady(Message ms) {
        try {
            boolean ready = ms.reader().readBoolean();
            user.getFightWait().setReady(ready, user.getUserId());
        } catch (IOException ignored) {
        }
    }

    public void imbueGem(Message ms) {
        List<EquipmentChest> equipList = getSelectedEquips();
        List<SpecialItemChest> specialItemList = getSelectedSpecialItems();

        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            if (action == 0) {
                //Đặt lại dữ liệu
                userAction = null;
                fabricateItem = null;
                equipList.clear();
                specialItemList.clear();

                byte size = dis.readByte();
                if (size <= 0 || size > 100) {
                    return;
                }
                for (byte i = 0; i < size; i++) {
                    int id = dis.readInt();
                    short quantity = (short) dis.readUnsignedByte();

                    //Kiểm tra dữ liệu hợp lệ
                    if (quantity == 0 || id < 0) {
                        continue;
                    }

                    //Lấy thông tin vật phẩm từ rương người chơi
                    if (id >= Byte.MAX_VALUE) {//Trường hợp trang bị
                        EquipmentChest equipment = user.getEquipmentByKey(id);
                        if (equipment == null || equipList.contains(equipment)) {
                            continue;
                        }
                        equipList.add(equipment);
                    } else {//Trường hợp là ngọc
                        SpecialItemChest specialItem = user.getSpecialItemById((byte) id);
                        if (specialItem == null || specialItem.getItem() == null || specialItem.getQuantity() < quantity || specialItemList.contains(specialItem)) {
                            continue;
                        }
                        specialItemList.add(new SpecialItemChest(quantity, specialItem.getItem()));
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
                    fabricateItem = FabricateItemManager.getFabricateItem(specialItemList);
                    if (fabricateItem != null) {
                        userAction = UserAction.COMBINE_SPECIAL_ITEM;
                        sendMessageConfirm(fabricateItem.getConfirmationMessage());
                        return;
                    }

                    if (specialItemList.size() == 1) {
                        SpecialItemChest specialItemChest = specialItemList.getFirst();
                        if (specialItemChest.getItem().isGem()) {
                            if (specialItemChest.getQuantity() == 5 && ((specialItemChest.getItem().getId() + 1) % 10 != 0)) {
                                userAction = UserAction.UPGRADE_GEM;
                                sendMessageConfirm(GameString.createGemFusionRequestMessage((90 - (specialItemChest.getItem().getId() % 10) * 10)));
                            } else {
                                userAction = UserAction.SELL_GEM;
                                totalTransactionAmount = specialItemChest.getSellPrice();
                                sendMessageConfirm(GameString.createGemSellRequestMessage(specialItemChest.getQuantity(), totalTransactionAmount));
                            }
                            return;
                        }

                        if (specialItemChest.getItem().isUsable()) {
                            userAction = UserAction.USE_SPECIAL_ITEM;
                            confirmSpecialItemUse(specialItemChest);
                            return;
                        }
                    }
                }
                sendServerMessage(GameString.COMBINE_ERROR);
            } else {
                switch (userAction) {
                    case INSERT_GEM_INTO_EQUIPMENT -> {
                        EquipmentChest equip = equipList.getFirst();
                        SpecialItemChest specialItem = specialItemList.getFirst();
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
                        SpecialItemChest specialItemChest = specialItemList.getFirst();
                        int successRate = (90 - (specialItemChest.getItem().getId() % 10) * 10);
                        int randomNumber = Utils.nextInt(100);
                        if (randomNumber < successRate) {
                            SpecialItemChest newItem = new SpecialItemChest();
                            newItem.setQuantity((short) 1);
                            newItem.setItem(SpecialItemManager.getSpecialItemById((byte) (specialItemChest.getItem().getId() + 1)));

                            user.updateInventory(null, null, List.of(newItem), List.of(specialItemChest));
                            sendServerMessage(GameString.createGemUpgradeSuccessMessage(newItem.getQuantity(), newItem.getItem().getName()));
                        } else {
                            specialItemChest.setQuantity((short) 1);
                            user.updateInventory(null, null, null, List.of(specialItemChest));
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
                        if (fabricateItem.getRewardXu() > 0) {
                            user.updateXu(fabricateItem.getRewardXu());
                        }
                        if (fabricateItem.getRewardLuong() > 0) {
                            user.updateLuong(fabricateItem.getRewardLuong());
                        }
                        if (fabricateItem.getRewardCup() > 0) {
                            user.updateCup(fabricateItem.getRewardCup());
                        }
                        if (fabricateItem.getRewardExp() > 0) {
                            user.updateXp(fabricateItem.getRewardExp());
                        }

                        List<SpecialItemChest> addItems = fabricateItem.getRewardItem()
                                .stream()
                                .map(SpecialItemChest::new)
                                .toList();

                        user.updateInventory(null, null, addItems, specialItemList);

                        if (!fabricateItem.getCompletionMessage().isEmpty()) {
                            sendServerMessage(fabricateItem.getCompletionMessage());
                        }
                    }
                }

                //Đặt lại dữ liệu
                userAction = null;
            }
        } catch (IOException ignored) {
        }
    }

    private void handleUseSpecialItem(SpecialItemChest specialItemChest) {
        switch (specialItemChest.getItem().getId()) {
            case 54 -> {
                user.addDaysToXpX2Time(1);
                user.updateInventory(null, null, null, List.of(specialItemChest));
                sendServerMessage(GameString.ITEM_X2_XP_USAGE_SUCCESS);
            }

            case 86 -> {
                if (specialItemChest.getQuantity() == 50) {
                    if (ExchangeLimitManager.isGoldLimitReached(0)) {
                        sendServerMessage("Đã hết số lượng trang bị vàng cấp 1");
                        return;
                    }

                    for (byte i = 0; i < 5; i++) {
                        byte[] addPoints = new byte[5];
                        byte[] addPercents = new byte[5];
                        for (byte n = 0; n < 5; n++) {
                            addPoints[n] = (byte) Utils.nextInt(15, 20);
                            addPercents[n] = (byte) Utils.nextInt(8, 10);
                        }
                        EquipmentChest newEquip = new EquipmentChest();
                        newEquip.setEquipment(EquipmentManager.getEquipment(user.getActiveCharacterId(), i, (short) (30 + i)));
                        newEquip.setVipLevel((byte) 1);
                        newEquip.setAddPoints(addPoints);
                        newEquip.setAddPercents(addPercents);

                        user.addEquipment(newEquip);
                    }

                    ExchangeLimitManager.incrementGoldCount(0);

                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 100) {
                    if (ExchangeLimitManager.isGoldLimitReached(1)) {
                        sendServerMessage("Đã hết số lượng trang bị vàng cấp 2");
                        return;
                    }

                    for (byte i = 0; i < 5; i++) {
                        byte[] addPoints = new byte[5];
                        byte[] addPercents = new byte[5];
                        for (byte n = 0; n < 5; n++) {
                            addPoints[n] = (byte) Utils.nextInt(20, 25);
                            addPercents[n] = (byte) Utils.nextInt(10, 12);
                        }
                        EquipmentChest newEquip = new EquipmentChest();
                        newEquip.setEquipment(EquipmentManager.getEquipment(user.getActiveCharacterId(), i, (short) (30 + i)));
                        newEquip.setVipLevel((byte) 2);
                        newEquip.setAddPoints(addPoints);
                        newEquip.setAddPercents(addPercents);

                        user.addEquipment(newEquip);
                    }

                    ExchangeLimitManager.incrementGoldCount(1);

                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 150) {
                    if (ExchangeLimitManager.isGoldLimitReached(2)) {
                        sendServerMessage("Đã hết số lượng trang bị vàng cấp 3");
                        return;
                    }

                    for (byte i = 0; i < 5; i++) {
                        byte[] addPoints = new byte[5];
                        byte[] addPercents = new byte[5];
                        for (byte n = 0; n < 5; n++) {
                            addPoints[n] = (byte) Utils.nextInt(25, 30);
                            addPercents[n] = (byte) Utils.nextInt(12, 14);
                        }
                        EquipmentChest newEquip = new EquipmentChest();
                        newEquip.setEquipment(EquipmentManager.getEquipment(user.getActiveCharacterId(), i, (short) (30 + i)));
                        newEquip.setVipLevel((byte) 3);
                        newEquip.setAddPoints(addPoints);
                        newEquip.setAddPercents(addPercents);

                        user.addEquipment(newEquip);
                    }

                    ExchangeLimitManager.incrementGoldCount(2);

                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else {
                    user.updateXp(1000 * specialItemChest.getQuantity());
                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.USE_BANH_TRUNG_SUCCESS);
                }
            }

            case 87 -> {
                if (specialItemChest.getQuantity() == 50) {
                    if (ExchangeLimitManager.isSilverLimitReached(0)) {
                        sendServerMessage("Đã hết số lượng trang bị bạc cấp 1");
                        return;
                    }

                    byte[][] maxPoints = {{5, 15, 5, 5, 5}, {15, 5, 5, 5, 5}, {5, 5, 15, 5, 5}, {5, 5, 5, 15, 5}, {5, 5, 5, 5, 15}};
                    byte[][] minPoints = {{5, 10, 5, 5, 5}, {10, 5, 5, 5, 5}, {5, 5, 10, 5, 5}, {5, 5, 5, 10, 5}, {5, 5, 5, 5, 10}};
                    byte[][] maxPercents = {{0, 4, 0, 0, 4}, {4, 0, 0, 4, 0}, {0, 0, 4, 0, 4}, {4, 4, 0, 0, 0}, {0, 0, 4, 4, 0}};
                    byte[][] minPercents = {{0, 2, 0, 0, 2}, {2, 0, 0, 2, 0}, {0, 0, 2, 0, 2}, {2, 2, 0, 0, 0}, {0, 0, 2, 2, 0}};
                    for (byte i = 0; i < 5; i++) {
                        byte[] generatedPoints = new byte[5];
                        byte[] generatedPercents = new byte[5];

                        byte[] currentMaxPoints = maxPoints[i];
                        byte[] currentMinPoints = minPoints[i];
                        byte[] currentMaxPercents = maxPercents[i];
                        byte[] currentMinPercents = minPercents[i];

                        for (byte n = 0; n < 5; n++) {
                            generatedPoints[n] = (byte) Utils.nextInt(currentMinPoints[n], currentMaxPoints[n]);
                            generatedPercents[n] = (byte) Utils.nextInt(currentMinPercents[n], currentMaxPercents[n]);
                        }

                        EquipmentChest newEquipment = new EquipmentChest();
                        newEquipment.setEquipment(EquipmentManager.getEquipment(user.getActiveCharacterId(), i, (short) (25 + i)));
                        newEquipment.setVipLevel((byte) 1);
                        newEquipment.setAddPoints(generatedPoints);
                        newEquipment.setAddPercents(generatedPercents);

                        user.addEquipment(newEquipment);
                    }

                    ExchangeLimitManager.incrementSilverCount(0);

                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 100) {
                    if (ExchangeLimitManager.isSilverLimitReached(1)) {
                        sendServerMessage("Đã hết số lượng trang bị bạc cấp 2");
                        return;
                    }

                    byte[][] maxPoints = {{7, 20, 7, 7, 7}, {20, 7, 7, 7, 7}, {7, 7, 20, 7, 7}, {7, 7, 7, 20, 7}, {7, 7, 7, 7, 20}};
                    byte[][] minPoints = {{7, 15, 7, 7, 7}, {15, 7, 7, 7, 7}, {7, 7, 15, 7, 7}, {7, 7, 7, 15, 7}, {7, 7, 7, 7, 15}};
                    byte[][] maxPercents = {{0, 6, 0, 0, 6}, {6, 0, 0, 6, 0}, {0, 0, 6, 0, 6}, {6, 6, 0, 0, 0}, {0, 0, 6, 6, 0}};
                    byte[][] minPercents = {{0, 4, 0, 0, 4}, {4, 0, 0, 4, 0}, {0, 0, 4, 0, 4}, {4, 4, 0, 0, 0}, {0, 0, 4, 4, 0}};
                    for (byte i = 0; i < 5; i++) {
                        byte[] generatedPoints = new byte[5];
                        byte[] generatedPercents = new byte[5];

                        byte[] currentMaxPoints = maxPoints[i];
                        byte[] currentMinPoints = minPoints[i];
                        byte[] currentMaxPercents = maxPercents[i];
                        byte[] currentMinPercents = minPercents[i];

                        for (byte n = 0; n < 5; n++) {
                            generatedPoints[n] = (byte) Utils.nextInt(currentMinPoints[n], currentMaxPoints[n]);
                            generatedPercents[n] = (byte) Utils.nextInt(currentMinPercents[n], currentMaxPercents[n]);
                        }

                        EquipmentChest newEquipment = new EquipmentChest();
                        newEquipment.setEquipment(EquipmentManager.getEquipment(user.getActiveCharacterId(), i, (short) (25 + i)));
                        newEquipment.setVipLevel((byte) 2);
                        newEquipment.setAddPoints(generatedPoints);
                        newEquipment.setAddPercents(generatedPercents);

                        user.addEquipment(newEquipment);
                    }

                    ExchangeLimitManager.incrementSilverCount(1);

                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 150) {
                    if (ExchangeLimitManager.isSilverLimitReached(2)) {
                        sendServerMessage("Đã hết số lượng trang bị bạc cấp 3");
                        return;
                    }

                    byte[][] maxPoints = {{9, 25, 9, 9, 9}, {25, 9, 9, 9, 9}, {9, 9, 25, 9, 9}, {9, 9, 9, 25, 9}, {9, 9, 9, 9, 25}};
                    byte[][] minPoints = {{9, 20, 9, 9, 9}, {20, 9, 9, 9, 9}, {9, 9, 20, 9, 9}, {9, 9, 9, 20, 9}, {9, 9, 9, 9, 20}};
                    byte[][] maxPercents = {{0, 8, 0, 0, 8}, {8, 0, 0, 8, 0}, {0, 0, 8, 0, 8}, {8, 8, 0, 0, 0}, {0, 0, 8, 8, 0}};
                    byte[][] minPercents = {{0, 6, 0, 0, 6}, {6, 0, 0, 6, 0}, {0, 0, 6, 0, 6}, {6, 6, 0, 0, 0}, {0, 0, 6, 6, 0}};
                    for (byte i = 0; i < 5; i++) {
                        byte[] generatedPoints = new byte[5];
                        byte[] generatedPercents = new byte[5];

                        byte[] currentMaxPoints = maxPoints[i];
                        byte[] currentMinPoints = minPoints[i];
                        byte[] currentMaxPercents = maxPercents[i];
                        byte[] currentMinPercents = minPercents[i];

                        for (byte n = 0; n < 5; n++) {
                            generatedPoints[n] = (byte) Utils.nextInt(currentMinPoints[n], currentMaxPoints[n]);
                            generatedPercents[n] = (byte) Utils.nextInt(currentMinPercents[n], currentMaxPercents[n]);
                        }

                        EquipmentChest newEquipment = new EquipmentChest();
                        newEquipment.setEquipment(EquipmentManager.getEquipment(user.getActiveCharacterId(), i, (short) (25 + i)));
                        newEquipment.setVipLevel((byte) 3);
                        newEquipment.setAddPoints(generatedPoints);
                        newEquipment.setAddPercents(generatedPercents);

                        user.addEquipment(newEquipment);
                    }

                    ExchangeLimitManager.incrementSilverCount(2);

                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else {
                    user.updateXp(500 * specialItemChest.getQuantity());
                    user.updateInventory(null, null, null, List.of(specialItemChest));
                    sendServerMessage(GameString.USE_BANH_TET_SUCCESS);
                }
            }

            case 93 -> {
                List<SpecialItemChest> generatedItems = new ArrayList<>();
                StringBuilder rewardMessage = new StringBuilder("Sử dụng thành công, bạn nhận được: ");

                Map<Byte, Short> itemQuantityMap = new HashMap<>();
                for (int i = 0; i < specialItemChest.getQuantity(); i++) {
                    short randomQuantity = (short) Utils.nextInt(1, 5);
                    byte randomItemId = (byte) Utils.nextInt(62, 68);

                    if (itemQuantityMap.containsKey(randomItemId)) {
                        itemQuantityMap.put(randomItemId, (short) (itemQuantityMap.get(randomItemId) + randomQuantity));
                    } else {
                        itemQuantityMap.put(randomItemId, randomQuantity);
                    }
                }

                for (Map.Entry<Byte, Short> entry : itemQuantityMap.entrySet()) {
                    byte itemId = entry.getKey();
                    short totalQuantity = entry.getValue();
                    SpecialItemChest generatedItem = new SpecialItemChest(totalQuantity, SpecialItemManager.getSpecialItemById(itemId));
                    generatedItems.add(generatedItem);

                    rewardMessage.append(totalQuantity).append(" ").append(generatedItem.getItem().getName()).append(", ");
                }

                if (!rewardMessage.isEmpty()) {
                    rewardMessage.setLength(rewardMessage.length() - 2);
                }

                user.updateInventory(null, null, generatedItems, List.of(specialItemChest));
                sendServerMessage(rewardMessage.toString());
            }
        }
    }

    private void confirmSpecialItemUse(SpecialItemChest specialItemChest) {
        switch (specialItemChest.getItem().getId()) {
            case 54 -> {
                if (specialItemChest.getQuantity() == 1) {
                    sendMessageConfirm(GameString.ITEM_X2_XP_USAGE_REQUEST);
                }
            }

            case 86 -> {
                if (specialItemChest.getQuantity() == 50) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_1);
                } else if (specialItemChest.getQuantity() == 100) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_2);
                } else if (specialItemChest.getQuantity() == 150) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TRUNG_TO_GOLD_EQUIP_3);
                } else {
                    sendMessageConfirm(GameString.USE_BANH_TRUNG_REQUEST);
                }
            }

            case 87 -> {
                if (specialItemChest.getQuantity() == 50) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_1);
                } else if (specialItemChest.getQuantity() == 100) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_2);
                } else if (specialItemChest.getQuantity() == 150) {
                    sendMessageConfirm(GameString.EXCHANGE_BANH_TET_TO_SILVER_EQUIP_3);
                } else {
                    sendMessageConfirm(GameString.USE_BANH_TET_REQUEST);
                }
            }

            case 93 ->
                    sendMessageConfirm(GameString.createBlackFridayGiftBoxConfirmation(specialItemChest.getQuantity()));

            default -> sendServerMessage(GameString.COMBINE_ERROR);
        }
    }

    private void sendMessageConfirm(String message) {
        try {
            Message ms = new Message(Cmd.IMBUE);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeUTF(message);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleSetPasswordFightWait(Message ms) {
        try {
            String password = ms.reader().readUTF().trim();
            if (password.isEmpty() || password.length() > 10) {
                return;
            }
            user.getFightWait().setPassRoom(password, user.getUserId());
        } catch (IOException ignored) {
        }
    }

    public void handleSetMoneyFightWait(Message ms) {
        try {
            int xu = ms.reader().readInt();
            if (xu < 0) {
                return;
            }
            user.getFightWait().setMoney(xu, user.getUserId());
        } catch (IOException ignored) {
        }
    }

    public void handleStartGame() {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().startGame(user.getUserId());
    }

    public void movePlayer(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            short x = dis.readShort();
            short y = dis.readShort();

            if (user.getState() == UserState.FIGHTING) {
                user.getFightWait().getFightManager().changeLocation(user.getUserId(), x, y);
            } else if (user.getState() == UserState.TRAINING) {
                user.getTrainingManager().changeLocation(x, y);
            }
        } catch (IOException ignored) {
        }
    }

    public void shoot(Message ms) {
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

            user.getFightWait().getFightManager().addShoot(user.getUserId(), bullId, x, y, angle, force, force2, numShoot);
        } catch (IOException ignored) {
        }
    }

    public void processShootingResult(Message ms) {
        //todo
    }

    public void handleUseItem(Message ms) {
        try {
            byte itemIndex = ms.reader().readByte();
            if (itemIndex != 100) {
                if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
                    return;
                }

                if (user.getItemFightQuantity(itemIndex) < 1) {
                    return;
                }
            }
            user.getFightWait().getFightManager().useItem(user.getUserId(), itemIndex);
        } catch (IOException ignored) {
        }
    }

    public void handleJoinAnyBoard(Message ms) {
        Room[] rooms = ApplicationContext.getInstance()
                .getBean(RoomManager.class).getRooms();
        FightWait fightWait = null;
        try {
            int type = ms.reader().readByte();
            switch (type) {
                // Đấu trùm
                case 5 -> {
                    int start = serverConfig.getStartMapBoss();
                    int end = start + serverConfig.getRoomQuantity()[5];

                    outerLoop:
                    for (int i = start; i < end; i++) {
                        Room room = rooms[i];
                        for (FightWait fight : room.getFightWaits()) {
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
                    int end = serverConfig.getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
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
                }

                //Khu vực trống
                case 0 -> {
                    int end = serverConfig.getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
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
                }

                //Ngẫu nhiên
                case -1 -> {
                    int end = serverConfig.getStartMapBoss();
                    int index = Utils.nextInt(0, end - 1);
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
            }

            if (fightWait == null) {
                sendMoneyErrorMessage(GameString.AREA_NOT_FOUND);
            } else {
                fightWait.sendInfo(user);
                fightWait.addUser(user);
            }
        } catch (IOException ignored) {
        }
    }

    public void handleViewFriendList() {
        try {
            Message ms = new Message(Cmd.FRIENDLIST);
            DataOutputStream ds = ms.writer();
            if (!user.getFriends().isEmpty()) {
                List<FriendDTO> friends = userDAO.getFriendsList(user.getUserId(), user.getFriends());
                for (FriendDTO friend : friends) {
                    ds.writeInt(friend.getUserId());
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

    public void handleAddFriend(Message ms) {
        int maxFriends = serverConfig.getMaxFriends();
        Set<Integer> friends = user.getFriends();
        try {
            int id = ms.reader().readInt();

            // Kiểm tra số lượng bạn bè đã đạt giới hạn
            if (friends.size() >= maxFriends) {
                sendAddFriendMessage(2);
                return;
            }

            // Kiểm tra nếu bạn bè đã tồn tại
            if (!friends.add(id)) {
                sendAddFriendMessage(1);
                return;
            }

            sendAddFriendMessage(0);
        } catch (IOException e) {
            sendAddFriendMessage(1);
        }
    }

    public void handleRemoveFriend(Message ms) {
        try {
            Integer id = ms.reader().readInt();
            user.getFriends().remove(id);
            sendDeleteFriendMessage(0);
        } catch (IOException e) {
            sendDeleteFriendMessage(1);
        }
    }

    private void sendDeleteFriendMessage(int status) {
        try {
            Message ms = new Message(Cmd.DELETE_FRIEND_RESULT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    private void sendAddFriendMessage(int status) {
        try {
            Message ms = new Message(Cmd.ADD_FRIEND_RESULT);
            DataOutputStream ds = ms.writer();
            ds.writeByte(status);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleGetFlayerDetail(Message ms) {
        try {
            int userId = ms.reader().readInt();
            User us = null;
            if (userId == user.getUserId()) {
                us = user;
            } else if (user.isNotWaiting()) {
                us = user.getFightWait().getUserByUserId(userId);
            }
            ms = new Message(Cmd.PLAYER_DETAIL);
            DataOutputStream ds = ms.writer();
            if (us == null) {
                ds.writeInt(-1);
            } else {
                String rankDisplayText = GameString.NO_RANKING;
                if (us.getRank() == 0) {
                    Optional<Integer> userRankOptional = userDAO.getUserRankByCup(us.getCup());
                    if (userRankOptional.isPresent()) {
                        us.setRank(userRankOptional.get());

                        rankDisplayText = getFormattedRankDisplay(us.getRank());
                    }
                } else {
                    rankDisplayText = getFormattedRankDisplay(us.getRank());
                }

                ds.writeInt(us.getUserId());
                ds.writeUTF(us.getUsername());
                ds.writeInt(us.getXu());
                ds.writeByte(us.getCurrentLevel());
                ds.writeByte(us.getCurrentLevelPercent());
                ds.writeInt(us.getLuong());
                ds.writeInt(us.getCurrentXp());
                ds.writeInt(us.getCurrentRequiredXp());
                ds.writeInt(us.getCup());
                ds.writeUTF(rankDisplayText);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleFindPlayer(Message ms) {
        try {
            String username = ms.reader().readUTF().trim();
            if (username.isEmpty()) {
                sendMessageLoginFail(GameString.FRIEND_ADD_MISSING_NAME);
                return;
            }
            if (Utils.isAlphanumeric(username)) {
                sendMessageLoginFail(GameString.FRIEND_ADD_INVALID_NAME);
                return;
            }
            Optional<Integer> foundUserId = userDAO.findUserIdByUsername(username);
            ms = new Message(Cmd.SEARCH);
            DataOutputStream ds = ms.writer();
            if (foundUserId.isPresent()) {
                ds.writeInt(foundUserId.get());
                ds.writeUTF(username);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void skipTurn() {
        user.getFightWait().getFightManager().skipTurn(user.getUserId());
    }

    public void updateCoordinates(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            short x = dis.readShort();
            short y = dis.readShort();
        } catch (IOException ignored) {
        }
    }

    public void handleSetFightWaitName(Message ms) {
        try {
            String name = ms.reader().readUTF().trim();
            user.getFightWait().setRoomName(user.getUserId(), name);
        } catch (IOException ignored) {
        }
    }

    public void handleSetMaxPlayerFightWait(Message ms) {
        try {
            byte maxPlayers = ms.reader().readByte();
            user.getFightWait().setMaxPlayers(user.getUserId(), maxPlayers);
        } catch (IOException ignored) {
        }
    }

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
            user.getFightWait().setItems(user.getUserId(), items);
        } catch (IOException ignored) {
        }
    }

    public void handleChoseCharacter(Message ms) {
        try {
            byte characterId = ms.reader().readByte();
            if (characterId >= CharacterManager.CHARACTERS.size() || characterId < 0 || !user.getOwnedCharacters()[characterId]) {
                return;
            }
            user.setActiveCharacterId(characterId);

            ms = new Message(Cmd.CHOOSE_GUN);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getUserId());
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
            Message ms = new Message(Cmd.CURR_EQUIP_DBKEY);
            DataOutputStream ds = ms.writer();
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleChangeTeam(Message ms) {
        if (user.getState() != UserState.WAIT_FIGHT) {
            return;
        }
        user.getFightWait().changeTeam(user);
    }

    public void handlePurchaseItem(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte unit = dis.readByte();
            byte itemIndex = dis.readByte();
            byte quantity = dis.readByte();
            if (itemIndex < 0 || itemIndex >= FightItemManager.FIGHT_ITEMS.size()) {
                return;
            }
            if (user.getFightItems()[itemIndex] + quantity > serverConfig.getMaxItem()) {
                return;
            }
            if (unit == 0) {
                int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyXu() * quantity;
                if (user.getXu() < total || total < 0) {
                    return;
                }
                user.updateXu(-total);
            } else {
                int total = FightItemManager.FIGHT_ITEMS.get(itemIndex).getBuyLuong() * quantity;
                if (user.getLuong() < total || total < 0) {
                    return;
                }
                user.updateLuong(-total);
            }
            user.updateFightItems(itemIndex, quantity);
            ms = new Message(Cmd.BUY_ITEM);
            DataOutputStream ds = ms.writer();
            ds.writeByte(1);
            ds.writeByte(itemIndex);
            ds.writeByte(user.getFightItems()[itemIndex]);
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            sendMessage(ms);
            sendServerMessage(GameString.PURCHASE_SUCCESS);
        } catch (IOException ignored) {
        }
    }

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
            Character character = CharacterManager.CHARACTERS.get(index);
            if (unit == 0) {
                if (character.getPriceXu() <= 0) {
                    return;
                }
                if (user.getXu() < character.getPriceXu()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-character.getPriceXu());
            } else {
                if (character.getPriceLuong() <= 0) {
                    return;
                }
                if (user.getLuong() < character.getPriceLuong()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateLuong(-character.getPriceLuong());
            }

            Optional<Integer> result = userCharacterDAO.create(user.getUserId(), index);
            if (result.isPresent()) {
                UserCharacterDTO userCharacterDTO = userCharacterDAO.findByUserIdAndCharacterId(user.getUserId(), index);
                if (userCharacterDTO != null) {
                    user.getLevels()[index] = userCharacterDTO.getLevel();
                    user.getXps()[index] = userCharacterDTO.getXp();
                    user.getPoints()[index] = userCharacterDTO.getPoints();
                    user.getAddedPoints()[index] = userCharacterDTO.getAdditionalPoints();
                    user.getUserCharacterIds()[index] = userCharacterDTO.getUserCharacterId();
                    user.getOwnedCharacters()[index] = true;
                    user.getEquipData()[index] = userCharacterDTO.getData();

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

    public void handleSelectMap(Message ms) {
        try {
            byte mapId = ms.reader().readByte();
            user.getFightWait().setMap(user.getUserId(), mapId);
        } catch (IOException ignored) {
        }
    }

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
        } catch (IOException ignored) {
        }
    }

    private void handleGiftCode(String code) {
        GiftCodeDTO giftCode = giftCodeDAO.findById(code);
        if (giftCode == null) {
            sendServerMessage(GameString.GIFT_CODE_INVALID);
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

        boolean existsByUserId = userGiftCodeDAO.existsByUserId(user.getUserId());
        if (existsByUserId) {
            sendServerMessage(GameString.GIFT_CODE_ALREADY_USED);
            return;
        }

        giftCodeDAO.decrementUsageLimit(giftCode.getGiftCodeId());
        userGiftCodeDAO.create(giftCode.getGiftCodeId(), user.getUserId());

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
            List<SpecialItemChest> additionalItems = new ArrayList<>();
            for (SpecialItemChestJson item : giftCode.getItems()) {
                SpecialItemChest newItem = new SpecialItemChest();
                newItem.setItem(SpecialItemManager.getSpecialItemById(item.getId()));
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
                EquipmentChest addEquip = new EquipmentChest();
                addEquip.setEquipment(EquipmentManager.getEquipment(json.getEquipmentId()));
                if (addEquip.getEquipment() == null) {
                    continue;
                }
                addEquip.setAddPoints(json.getAddPoints());
                addEquip.setAddPercents(json.getAddPercents());
                user.addEquipment(addEquip);
                sendMessageToUser(GameString.createGiftCodeRewardMessage(code, addEquip.getEquipment().getName()));
            }
        }

        sendServerMessage(GameString.GIFT_CODE_SUCCESS);
    }

    public void handleFindPlayerWait(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            boolean find = dis.readBoolean();
            if (find) {
                user.getFightWait().findPlayer(user.getUserId());
            } else {
                int userId = dis.readInt();
                user.getFightWait().inviteToRoom(userId);
            }
        } catch (IOException ignored) {
        }
    }

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
            //todo
        } catch (IOException ignored) {
        }
    }

    public void handleChangePassword(Message ms) {
        DataInputStream dis = ms.reader();
        try {
            String oldPass = dis.readUTF().trim();
            String newPass = dis.readUTF().trim();

            if (Utils.isAlphanumeric(oldPass) || Utils.isAlphanumeric(newPass)) {
                sendServerMessage(GameString.PASSWORD_INVALID_CHARACTER);
                return;
            }

            if (!accountDAO.existsByAccountIdAndPassword(user.getAccountId(), oldPass)) {
                sendServerMessage(GameString.PASSWORD_INCORRECT_OLD);
                return;
            }

            accountDAO.changePassword(user.getAccountId(), newPass);
            sendServerMessage(GameString.PASSWORD_CHANGE_SUCCESS);
        } catch (IOException ignored) {
        }
    }

    public void getFilePack(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            byte version = dis.readByte();

            switch (type) {
                case 1 -> {
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getIconVersion2());
                    if (version != serverConfig.getIconVersion2()) {
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
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getValuesVersion2());
                    if (version != serverConfig.getValuesVersion2()) {
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
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getPlayerVersion2());
                    if (version != serverConfig.getPlayerVersion2()) {
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
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getEquipVersion2());
                    if (version != serverConfig.getEquipVersion2()) {
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
                    ms = new Message(Cmd.GET_FILEPACK);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(type);
                    ds.writeByte(serverConfig.getLevelCVersion2());
                    if (version != serverConfig.getLevelCVersion2()) {
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
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
            Map<Integer, EquipmentChest> equipmentChest = user.getEquipmentChest();
            ds.writeByte(equipmentChest.size());
            for (EquipmentChest equipment : equipmentChest.values()) {
                ds.writeInt(equipment.getKey());
                ds.writeByte(equipment.getEquipment().getCharacterId());
                ds.writeByte(equipment.getEquipment().getEquipType());
                ds.writeShort(equipment.getEquipment().getEquipIndex());
                ds.writeUTF(equipment.getEquipment().getName());
                ds.writeByte(equipment.getAddPoints().length * 2);
                for (int j = 0; j < equipment.getAddPoints().length; j++) {
                    ds.writeByte(equipment.getAddPoints()[j]);
                    ds.writeByte(equipment.getAddPercents()[j]);
                }
                ds.writeByte(equipment.getRemainingDays());
                ds.writeByte(equipment.getEmptySlot());
                ds.writeByte(equipment.getEquipment().isDisguise() ? 1 : 0);
                ds.writeByte(equipment.getVipLevel());
            }
            for (int i = 0; i < 5; i++) {
                ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
            }
            ds.flush();
            sendMessage(ms);

            ms = new Message(Cmd.MATERIAL);
            ds = ms.writer();
            ds.writeByte(0);
            Map<Byte, SpecialItemChest> specialItemChest = user.getSpecialItemChest();
            ds.writeByte(specialItemChest.size());
            for (SpecialItemChest item : specialItemChest.values()) {
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
        } catch (IOException ignored) {
        }
        sendCharacterInfo();
    }

    public void sendCharacterInfo() {
        try {
            Message ms = new Message(Cmd.CHARACTOR_INFO);
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

    public void handleChangeEquipment(Message ms) {
        try {
            boolean changeSuccessful = false;
            for (int i = 0; i < 5; i++) {
                int key = ms.reader().readInt();
                EquipmentChest equip = user.getEquipmentByKey(key);
                if (equip == null ||
                        equip.isInUse() ||
                        equip.isExpired() ||
                        equip.getEquipment().isDisguise() ||
                        equip.getEquipment().getLevelRequirement() > user.getCurrentLevel() ||
                        equip.getEquipment().getCharacterId() != user.getActiveCharacterId() || equip.getEquipment().getEquipType() != i
                ) {
                    continue;
                }
                EquipmentChest oldEquip = user.getCharacterEquips()[user.getActiveCharacterId()][i];
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

    public void handleSendShopEquipments() {
        Message ms = CacheManager.cachedShopEquipments;
        if (ms != null) {
            sendMessage(ms);
            return;
        }

        try {
            ms = new Message(Cmd.SHOP_EQUIP);
            DataOutputStream ds = ms.writer();
            List<Short> equipIds = EquipmentManager.SALE_INDEX_TO_ID;
            ds.writeShort(equipIds.size());
            for (Short id : equipIds) {
                Equipment equip = EquipmentManager.getEquipment(id);
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

            CacheManager.cachedShopEquipments = ms;

            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleEquipmentTransactions(Message ms) {
        List<EquipmentChest> equipList = getSelectedEquips();
        try {
            DataInputStream dis = ms.reader();
            byte action = dis.readByte();
            switch (action) {
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
                    if (size <= 0 || size > 100) {
                        return;
                    }
                    for (int i = 0; i < size; i++) {
                        int key = dis.readInt();
                        EquipmentChest equip = user.getEquipmentByKey(key);
                        if (equip == null || equipList.contains(equip)) {
                            continue;
                        }
                        int remainingDays = equip.getRemainingDays();
                        if (remainingDays > 0) {
                            if (equip.getEquipment().getPriceXu() > 0) {
                                totalTransactionAmount += Math.round((float) (equip.getEquipment().getPriceXu() * remainingDays) / (equip.getEquipment().getExpirationDays() * 2));
                            } else if (equip.getEquipment().getPriceLuong() > 0) {
                                totalTransactionAmount += Math.round((float) (equip.getEquipment().getPriceLuong() * 1000 * remainingDays) / (equip.getEquipment().getExpirationDays() * 2));
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
                                SpecialItem item = SpecialItemManager.getSpecialItemById(slotItemId);
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

                        EquipmentChest selectedEquipment = equipList.getFirst();
                        if (selectedEquipment == null) {
                            return;
                        }

                        //Lấy lại ngọc đã ghép
                        List<SpecialItemChest> recoveredGems = new ArrayList<>();
                        for (byte slotItemId : selectedEquipment.getSlots()) {
                            if (slotItemId > -1) {
                                SpecialItemChest gem = new SpecialItemChest((short) 1, SpecialItemManager.getSpecialItemById(slotItemId));
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

                        for (EquipmentChest equipment : equipList) {
                            if (equipment.isInUse()) {
                                sendServerMessage(GameString.EQUIP_SELL_ERROR_IN_USE);
                                return;
                            }
                            if (equipment.getEmptySlot() < 3) {
                                sendServerMessage(GameString.EQUIP_SELL_ERROR_REMOVE_GEMS);
                                return;
                            }
                        }
                        for (EquipmentChest validEquipment : equipList) {
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
        if (user.getEquipmentChest().size() >= serverConfig.getMaxEquipmentSlots()) {
            sendServerMessage(GameString.CHEST_NO_SPACE);
            return;
        }
        Equipment equipment = EquipmentManager.getEquipmentBySaleIndex(saleIndex);
        if (equipment == null || (unit == 0 ? equipment.getPriceXu() : equipment.getPriceLuong()) < 0) {
            return;
        }
        if (unit == 0) {
            if (user.getXu() < equipment.getPriceXu()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateXu(-equipment.getPriceXu());
        } else {
            if (user.getLuong() < equipment.getPriceLuong()) {
                sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                return;
            }
            user.updateLuong(-equipment.getPriceLuong());
        }
        EquipmentChest newEquip = new EquipmentChest();
        newEquip.setEquipment(equipment);
        user.addEquipment(newEquip);
        sendServerMessage(GameString.PURCHASE_SUCCESS);
    }

    public void handleSpinWheel(Message ms) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < 5000) {
            sendServerMessage(GameString.SPIN_WAIT_TIME);
            return;
        }
        try {
            byte unit = ms.reader().readByte();
            if (unit == 0) {
                if (user.getXu() < serverConfig.getSpinXuCost()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateXu(-serverConfig.getSpinXuCost());
            } else {
                if (user.getLuong() < serverConfig.getSpinLuongCost()) {
                    sendServerMessage(GameString.INSUFFICIENT_FUNDS);
                    return;
                }
                user.updateLuong(-serverConfig.getSpinLuongCost());
            }
            ms = new Message(Cmd.RULET);
            DataOutputStream ds = ms.writer();
            int luckyIndex = Utils.nextInt(10);
            for (byte i = 0; i < 10; i++) {
                byte type = (byte) Utils.nextInt(serverConfig.getSpinTypeProbabilities());
                byte itemId = 0;
                int quantity = 0;

                switch (type) {
                    case 0 -> {
                        itemId = FightItemManager.getRandomItem();
                        quantity = serverConfig.getSpinItemCounts()[0][Utils.nextInt(serverConfig.getSpinItemCounts()[1])];
                        if (i == luckyIndex) {
                            user.updateFightItems(itemId, (byte) quantity);
                        }
                    }
                    case 1 -> {
                        quantity = serverConfig.getSpinXuCounts()[0][Utils.nextInt(serverConfig.getSpinXuCounts()[1])];
                        if (i == luckyIndex) {
                            user.updateXu(quantity);
                        }
                    }
                    case 2 -> {
                        quantity = serverConfig.getSpinXpCounts()[0][Utils.nextInt(serverConfig.getSpinXpCounts()[1])];
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

    public void getBigImage(Message ms) {
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

    public void rechargeMoney(Message ms) {
        try {
            DataInputStream dis = ms.reader();
            byte type = dis.readByte();
            switch (type) {
                case 0 -> {
                    ms = new Message(Cmd.CHARGE_MONEY_2);
                    DataOutputStream ds = ms.writer();
                    ds.writeByte(0);
                    for (Payment payment : PaymentManager.PAYMENT_MAP.values()) {
                        ds.writeUTF(payment.getId());
                        ds.writeUTF(payment.getInfo());
                        ds.writeUTF(payment.getUrl());
                    }
                    ds.flush();
                    sendMessage(ms);
                }
                case 1 -> {
                    String id = dis.readUTF();
                    Payment payment = PaymentManager.PAYMENT_MAP.get(id);
                    if (payment != null) {
                        ms = new Message(Cmd.CHARGE_MONEY_2);
                        DataOutputStream ds = ms.writer();
                        ds.writeByte(2);
                        ds.writeUTF(payment.getMssTo());
                        ds.writeUTF(payment.getMssContent());
                        ds.flush();
                        sendMessage(ms);
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public void getMaterialIconMessage(Message ms) {
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

    public void startTraining(Message ms) {
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
            user.setTrainingManager(new TrainingManager(user, serverConfig.getTrainingMapId()));
        }
    }

    public void trainShooting(Message ms) {
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

    public void sendUpdateMoney() {
        try {
            Message ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(user.getXu());
            ds.writeInt(user.getLuong());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendUpdateCup(int cupUp) {
        try {
            Message ms = new Message(Cmd.CUP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(cupUp);
            ds.writeInt(user.getCup());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void sendUpdateXp(int xpUp, boolean updateLevel) {
        try {
            Message ms = new Message(Cmd.UPDATE_EXP);
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

    public void ping(Message ms) {
    }

    public void getMoreGame() {
        try {
            Message ms = new Message(Cmd.MORE_GAME);
            DataOutputStream ds = ms.writer();
            ds.writeUTF(serverConfig.getDownloadTitle());
            ds.writeUTF(serverConfig.getDownloadInfo());
            ds.writeUTF(serverConfig.getDownloadUrl());
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }

    public void handleSendAgentAndProviders() {
        try {
            Message ms = new Message(Cmd.GET_AGENT_PROVIDER);
            DataOutputStream ds = ms.writer();
            ds.writeUTF("none");
            ds.writeByte(0);
            ds.flush();
            sendMessage(ms);
        } catch (IOException ignored) {
        }
    }
}
