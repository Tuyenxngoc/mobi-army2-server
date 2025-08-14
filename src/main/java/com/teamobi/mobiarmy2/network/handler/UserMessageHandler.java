package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.common.config.ServerConfig;
import com.teamobi.mobiarmy2.common.constant.Cmd;
import com.teamobi.mobiarmy2.common.constant.GameString;
import com.teamobi.mobiarmy2.common.constant.UserAction;
import com.teamobi.mobiarmy2.common.constant.UserState;
import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.dto.GiftCodeDTO;
import com.teamobi.mobiarmy2.dto.UserCharacterDTO;
import com.teamobi.mobiarmy2.dto.UserLeaderboardDTO;
import com.teamobi.mobiarmy2.entity.*;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.service.LeaderboardService;
import com.teamobi.mobiarmy2.service.LoginRateLimiterService;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class UserMessageHandler extends BaseMessageHandler {
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

    private long lastSpinTime;

    public UserMessageHandler(
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

    public void extendItemDuration(Message ms) throws IOException {
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
    }

    public void openLuckyGift(Message ms) throws IOException {
        byte index = ms.reader().readByte();
        user.getGiftBoxService().openGiftBoxAfterFight(index);
    }

    public void viewLeaderboard(Message ms) throws IOException {
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
    }

    public void equipVipItems(Message ms) throws IOException {
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
    }

    public void imbueGem(Message ms) throws IOException {
        List<EquipmentChest> equipList = getSelectedEquips();
        List<SpecialItemChest> specialItemList = getSelectedSpecialItems();

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

    private void confirmSpecialItemUse(SpecialItemChest specialItemChest) throws IOException {
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

    private void sendMessageConfirm(String message) throws IOException {
        Message ms = new Message(Cmd.IMBUE);
        DataOutputStream ds = ms.writer();
        ds.writeByte(0);
        ds.writeUTF(message);
        ds.flush();
        sendMessage(ms);
    }

    public void handleChoseCharacter(Message ms) throws IOException {
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
    }

    private void sendEquipInfo() throws IOException {
        Message ms = new Message(Cmd.CURR_EQUIP_DBKEY);
        DataOutputStream ds = ms.writer();
        for (int i = 0; i < 5; i++) {
            ds.writeInt(user.getEquipData()[user.getActiveCharacterId()][i]);
        }
        ds.flush();
        sendMessage(ms);
    }

    public void handleCardRecharge(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        String type = dis.readUTF().trim();
        String serial = dis.readUTF().trim();
        String pin = dis.readUTF().trim();

        if (type.equals("giftcode") && !serial.isEmpty()) {
            handleGiftCode(serial);
            return;
        }
        sendServerMessage(serial + " " + pin);
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

    public void handleChangePassword(Message ms) throws IOException {
        DataInputStream dis = ms.reader();

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
    }

    public void handleAddPoints(Message ms) throws IOException {
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

        sendCharacterInfo();
    }

    public void handleChangeEquipment(Message ms) throws IOException {
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
    }

    public void handleEquipmentTransactions(Message ms) throws IOException {
        List<EquipmentChest> equipList = getSelectedEquips();

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

    public void handleSpinWheel(Message ms) throws IOException {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSpinTime < 5000) {
            sendServerMessage(GameString.SPIN_WAIT_TIME);
            return;
        }

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
    }

    public void rechargeMoney(Message ms) throws IOException {
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
    }

    public void sendUpdateMoney() throws IOException {
        Message ms = new Message(Cmd.UPDATE_MONEY);
        DataOutputStream ds = ms.writer();
        ds.writeInt(user.getXu());
        ds.writeInt(user.getLuong());
        ds.flush();
        sendMessage(ms);
    }

    public void sendUpdateCup(int cupUp) throws IOException {
        Message ms = new Message(Cmd.CUP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(cupUp);
        ds.writeInt(user.getCup());
        ds.flush();
        sendMessage(ms);
    }

    public void sendUpdateXp(int xpUp, boolean updateLevel) throws IOException {
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
    }

    public void ping(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        int type = dis.readInt();
        log.info("Ping received from user {} with type {}", user.getUserId(), type);
    }

    public void getMoreGame() throws IOException {
        Message ms = new Message(Cmd.MORE_GAME);
        DataOutputStream ds = ms.writer();
        ds.writeUTF(serverConfig.getDownloadTitle());
        ds.writeUTF(serverConfig.getDownloadInfo());
        ds.writeUTF(serverConfig.getDownloadUrl());
        ds.flush();
        sendMessage(ms);
    }

    public void handleSendAgentAndProviders() throws IOException {
        Message ms = new Message(Cmd.GET_AGENT_PROVIDER);
        DataOutputStream ds = ms.writer();
        ds.writeUTF("none");
        ds.writeByte(0);
        ds.flush();
        sendMessage(ms);
    }
}
