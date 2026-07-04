package com.teamobi.mobiarmy2.network.handler;

import com.teamobi.mobiarmy2.config.ServerConfig;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.constant.GameString;
import com.teamobi.mobiarmy2.constant.UserAction;
import com.teamobi.mobiarmy2.entity.*;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.ExchangeLimitManager;
import com.teamobi.mobiarmy2.server.FabricateItemManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.util.RandomUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InventoryMessageHandler extends BaseMessageHandler {
    private UserAction userAction;
    private int totalTransactionAmount;
    private FabricateItem fabricateItem;
    private List<EquipmentChest> selectedEquips;
    private List<SpecialItemChest> selectedSpecialItems;

    private final ServerConfig serverConfig;
    private final ExchangeLimitManager exchangeLimitManager;

    public InventoryMessageHandler(Session session, ServerConfig serverConfig, ExchangeLimitManager exchangeLimitManager) {
        super(session);
        this.serverConfig = serverConfig;
        this.exchangeLimitManager = exchangeLimitManager;
    }

    private List<SpecialItemChest> getSelectedSpecialItems() {
        if (selectedSpecialItems == null) {
            selectedSpecialItems = new ArrayList<>();
        }
        return selectedSpecialItems;
    }

    private List<EquipmentChest> getSelectedEquips() {
        if (selectedEquips == null) {
            selectedEquips = new ArrayList<>();
        }
        return selectedEquips;
    }

    public void extendItemDuration(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte action = dis.readByte();
        int key = dis.readInt();
        EquipmentChest equip = us().getEquipmentByKey(key);
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
            if (us().getXu() < gia) {
                messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-gia);
            equip.setPurchaseDate(LocalDateTime.now());
            us().updateInventory(equip, null, null, null);
            messageSender.sendServerMessage(us(), GameString.EXTEND_SUCCESS);
        }
    }

    public void equipVipItems(Message ms) throws IOException {
        DataInputStream dis = ms.reader();
        byte action = dis.readByte();
        int key = dis.readInt();
        EquipmentChest equip = us().getEquipmentByKey(key);
        if (equip == null ||
                equip.isExpired() ||
                !equip.getEquipment().isDisguise() ||
                equip.getEquipment().getLevelRequirement() > us().getCurrentLevel() ||
                equip.getEquipment().getCharacterId() != us().getActiveCharacterId()
        ) {
            return;
        }
        EquipmentChest oldEquip = us().getCharacterEquips()[us().getActiveCharacterId()][5];
        if (oldEquip != null) {
            oldEquip.setInUse(false);
        }
        ms = new Message(Cmd.VIP_EQUIP);
        DataOutputStream ds = ms.writer();
        ds.writeByte(action);
        if (action == 0) {
            us().getEquipData()[us().getActiveCharacterId()][5] = -1;
            us().getCharacterEquips()[us().getActiveCharacterId()][5] = null;
        } else {
            equip.setInUse(true);
            us().getEquipData()[us().getActiveCharacterId()][5] = equip.getKey();
            us().getCharacterEquips()[us().getActiveCharacterId()][5] = equip;
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
                    EquipmentChest equipment = us().getEquipmentByKey(id);
                    if (equipment == null || equipList.contains(equipment)) {
                        continue;
                    }
                    equipList.add(equipment);
                } else {//Trường hợp là ngọc
                    SpecialItemChest specialItem = us().getSpecialItemById((byte) id);
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
                    messageSender.sendServerMessage(us(), GameString.COMBINE_ERROR);
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
            messageSender.sendServerMessage(us(), GameString.COMBINE_ERROR);
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
                        us().updateInventory(equip, null, null, specialItemList);
                        messageSender.sendServerMessage(us(), GameString.GEM_COMBINE_SUCCESS);
                    } else {
                        messageSender.sendServerMessage(us(), GameString.GEM_COMBINE_NO_SLOT);
                    }
                }

                case UPGRADE_GEM -> {
                    SpecialItemChest specialItemChest = specialItemList.getFirst();
                    int successRate = (90 - (specialItemChest.getItem().getId() % 10) * 10);
                    int randomNumber = RandomUtil.nextInt(100);
                    if (randomNumber < successRate) {
                        SpecialItemChest newItem = new SpecialItemChest();
                        newItem.setQuantity((short) 1);
                        newItem.setItem(SpecialItemManager.getSpecialItemById((byte) (specialItemChest.getItem().getId() + 1)));

                        us().updateInventory(null, null, List.of(newItem), List.of(specialItemChest));
                        messageSender.sendServerMessage(us(), GameString.createGemUpgradeSuccessMessage(newItem.getQuantity(), newItem.getItem().getName()));
                    } else {
                        specialItemChest.setQuantity((short) 1);
                        us().updateInventory(null, null, null, List.of(specialItemChest));
                        messageSender.sendServerMessage(us(), GameString.COMBINE_FAILURE);
                    }
                }

                case SELL_GEM -> {
                    if (us().isChestLocked()) {
                        messageSender.sendServerMessage(us(), GameString.CHEST_LOCKED_NO_SELL);
                        return;
                    }
                    us().updateInventory(null, null, null, specialItemList);
                    us().updateXu(totalTransactionAmount);
                    messageSender.sendServerMessage(us(), GameString.PURCHASE_SUCCESS);
                }

                case USE_SPECIAL_ITEM -> handleUseSpecialItem(specialItemList.getFirst());

                case COMBINE_SPECIAL_ITEM -> {
                    if (fabricateItem.getRewardXu() > 0) {
                        us().updateXu(fabricateItem.getRewardXu());
                    }
                    if (fabricateItem.getRewardLuong() > 0) {
                        us().updateLuong(fabricateItem.getRewardLuong());
                    }
                    if (fabricateItem.getRewardCup() > 0) {
                        us().updateCup(fabricateItem.getRewardCup());
                    }
                    if (fabricateItem.getRewardExp() > 0) {
                        us().updateXp(fabricateItem.getRewardExp());
                    }

                    List<SpecialItemChest> addItems = fabricateItem.getRewardItem()
                            .stream()
                            .map(SpecialItemChest::new)
                            .toList();

                    us().updateInventory(null, null, addItems, specialItemList);

                    if (!fabricateItem.getCompletionMessage().isEmpty()) {
                        messageSender.sendServerMessage(us(), fabricateItem.getCompletionMessage());
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
                us().addDaysToXpX2Time(1);
                us().updateInventory(null, null, null, List.of(specialItemChest));
                messageSender.sendServerMessage(us(), GameString.ITEM_X2_XP_USAGE_SUCCESS);
            }

            case 86 -> {
                if (!serverConfig.isTet()) {
                    us().updateXp(1000 * specialItemChest.getQuantity());
                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), "Dùng bánh trưng thành công");
                    return;
                }
                if (specialItemChest.getQuantity() == 50) {
                    if (exchangeLimitManager.isGoldLimitReached(0)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị vàng cấp 1");
                        return;
                    }

                    for (byte i = 0; i < 5; i++) {
                        byte[] addPoints = new byte[5];
                        byte[] addPercents = new byte[5];
                        for (byte n = 0; n < 5; n++) {
                            addPoints[n] = (byte) RandomUtil.nextInt(15, 20);
                            addPercents[n] = (byte) RandomUtil.nextInt(8, 10);
                        }
                        EquipmentChest newEquip = new EquipmentChest();
                        newEquip.setEquipment(EquipmentManager.getEquipment(us().getActiveCharacterId(), i, (short) (30 + i)));
                        newEquip.setVipLevel((byte) 1);
                        newEquip.setAddPoints(addPoints);
                        newEquip.setAddPercents(addPercents);

                        us().addEquipment(newEquip);
                    }

                    exchangeLimitManager.incrementGoldCount(0);

                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 100) {
                    if (exchangeLimitManager.isGoldLimitReached(1)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị vàng cấp 2");
                        return;
                    }

                    for (byte i = 0; i < 5; i++) {
                        byte[] addPoints = new byte[5];
                        byte[] addPercents = new byte[5];
                        for (byte n = 0; n < 5; n++) {
                            addPoints[n] = (byte) RandomUtil.nextInt(20, 25);
                            addPercents[n] = (byte) RandomUtil.nextInt(10, 12);
                        }
                        EquipmentChest newEquip = new EquipmentChest();
                        newEquip.setEquipment(EquipmentManager.getEquipment(us().getActiveCharacterId(), i, (short) (30 + i)));
                        newEquip.setVipLevel((byte) 2);
                        newEquip.setAddPoints(addPoints);
                        newEquip.setAddPercents(addPercents);

                        us().addEquipment(newEquip);
                    }

                    exchangeLimitManager.incrementGoldCount(1);

                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 150) {
                    if (exchangeLimitManager.isGoldLimitReached(2)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị vàng cấp 3");
                        return;
                    }

                    for (byte i = 0; i < 5; i++) {
                        byte[] addPoints = new byte[5];
                        byte[] addPercents = new byte[5];
                        for (byte n = 0; n < 5; n++) {
                            addPoints[n] = (byte) RandomUtil.nextInt(25, 30);
                            addPercents[n] = (byte) RandomUtil.nextInt(12, 14);
                        }
                        EquipmentChest newEquip = new EquipmentChest();
                        newEquip.setEquipment(EquipmentManager.getEquipment(us().getActiveCharacterId(), i, (short) (30 + i)));
                        newEquip.setVipLevel((byte) 3);
                        newEquip.setAddPoints(addPoints);
                        newEquip.setAddPercents(addPercents);

                        us().addEquipment(newEquip);
                    }

                    exchangeLimitManager.incrementGoldCount(2);

                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                }
            }

            case 87 -> {
                if (!serverConfig.isTet()) {
                    us().updateXp(500 * specialItemChest.getQuantity());
                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), "Dùng bánh tét thành công");
                    return;
                }
                if (specialItemChest.getQuantity() == 50) {
                    if (exchangeLimitManager.isSilverLimitReached(0)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị bạc cấp 1");
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
                            generatedPoints[n] = (byte) RandomUtil.nextInt(currentMinPoints[n], currentMaxPoints[n]);
                            generatedPercents[n] = (byte) RandomUtil.nextInt(currentMinPercents[n], currentMaxPercents[n]);
                        }

                        EquipmentChest newEquipment = new EquipmentChest();
                        newEquipment.setEquipment(EquipmentManager.getEquipment(us().getActiveCharacterId(), i, (short) (25 + i)));
                        newEquipment.setVipLevel((byte) 1);
                        newEquipment.setAddPoints(generatedPoints);
                        newEquipment.setAddPercents(generatedPercents);

                        us().addEquipment(newEquipment);
                    }

                    exchangeLimitManager.incrementSilverCount(0);

                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 100) {
                    if (exchangeLimitManager.isSilverLimitReached(1)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị bạc cấp 2");
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
                            generatedPoints[n] = (byte) RandomUtil.nextInt(currentMinPoints[n], currentMaxPoints[n]);
                            generatedPercents[n] = (byte) RandomUtil.nextInt(currentMinPercents[n], currentMaxPercents[n]);
                        }

                        EquipmentChest newEquipment = new EquipmentChest();
                        newEquipment.setEquipment(EquipmentManager.getEquipment(us().getActiveCharacterId(), i, (short) (25 + i)));
                        newEquipment.setVipLevel((byte) 2);
                        newEquipment.setAddPoints(generatedPoints);
                        newEquipment.setAddPercents(generatedPercents);

                        us().addEquipment(newEquipment);
                    }

                    exchangeLimitManager.incrementSilverCount(1);

                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                } else if (specialItemChest.getQuantity() == 150) {
                    if (exchangeLimitManager.isSilverLimitReached(2)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị bạc cấp 3");
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
                            generatedPoints[n] = (byte) RandomUtil.nextInt(currentMinPoints[n], currentMaxPoints[n]);
                            generatedPercents[n] = (byte) RandomUtil.nextInt(currentMinPercents[n], currentMaxPercents[n]);
                        }

                        EquipmentChest newEquipment = new EquipmentChest();
                        newEquipment.setEquipment(EquipmentManager.getEquipment(us().getActiveCharacterId(), i, (short) (25 + i)));
                        newEquipment.setVipLevel((byte) 3);
                        newEquipment.setAddPoints(generatedPoints);
                        newEquipment.setAddPercents(generatedPercents);

                        us().addEquipment(newEquipment);
                    }

                    exchangeLimitManager.incrementSilverCount(2);

                    us().updateInventory(null, null, null, List.of(specialItemChest));
                    messageSender.sendServerMessage(us(), GameString.NEW_YEAR_EVENT_GIFT_MESSAGE);
                }
            }

            case 93 -> {
                List<SpecialItemChest> generatedItems = new ArrayList<>();
                StringBuilder rewardMessage = new StringBuilder("Sử dụng thành công, bạn nhận được: ");

                Map<Byte, Short> itemQuantityMap = new HashMap<>();
                for (int i = 0; i < specialItemChest.getQuantity(); i++) {
                    short randomQuantity = (short) RandomUtil.nextInt(1, 5);
                    byte randomItemId = (byte) RandomUtil.nextInt(62, 68);

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

                us().updateInventory(null, null, generatedItems, List.of(specialItemChest));
                messageSender.sendServerMessage(us(), rewardMessage.toString());
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
                if (!serverConfig.isTet()) {
                    sendMessageConfirm("Bạn có muốn dùng bánh trưng không?");
                    return;
                }

                if (specialItemChest.getQuantity() == 50) {
                    if (exchangeLimitManager.isGoldLimitReached(0)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị vàng cấp 1");
                        return;
                    }
                    sendMessageConfirm("Bạn có muốn đổi 50 bánh trưng hoàn thiện để lấy một bộ trang bị vàng cấp 1 không?");
                } else if (specialItemChest.getQuantity() == 100) {
                    if (exchangeLimitManager.isGoldLimitReached(1)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị vàng cấp 2");
                        return;
                    }
                    sendMessageConfirm("Bạn có muốn đổi 100 bánh trưng hoàn thiện để lấy một bộ trang bị vàng cấp 2 không?");
                } else if (specialItemChest.getQuantity() == 150) {
                    if (exchangeLimitManager.isGoldLimitReached(2)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị vàng cấp 3");
                        return;
                    }
                    sendMessageConfirm("Bạn có muốn đổi 150 bánh trưng hoàn thiện để lấy một bộ trang bị vàng cấp 3 không?");
                } else {
                    sendMessageConfirm("Bạn có muốn dùng bánh trưng không?");
                }
            }

            case 87 -> {
                if (!serverConfig.isTet()) {
                    sendMessageConfirm("Bạn có muốn dùng bánh tét không?");
                    return;
                }

                if (specialItemChest.getQuantity() == 50) {
                    if (exchangeLimitManager.isSilverLimitReached(0)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị bạc cấp 1");
                        return;
                    }
                    sendMessageConfirm("Bạn có muốn đổi 50 bánh tét hoàn thiện để lấy một bộ trang bị bạc cấp 1 không?");
                } else if (specialItemChest.getQuantity() == 100) {
                    if (exchangeLimitManager.isSilverLimitReached(1)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị bạc cấp 2");
                        return;
                    }
                    sendMessageConfirm("Bạn có muốn đổi 100 bánh tét hoàn thiện để lấy một bộ trang bị bạc cấp 2 không?");
                } else if (specialItemChest.getQuantity() == 150) {
                    if (exchangeLimitManager.isSilverLimitReached(2)) {
                        messageSender.sendServerMessage(us(), "Đã hết số lượng trang bị bạc cấp 3");
                        return;
                    }
                    sendMessageConfirm("Bạn có muốn đổi 150 bánh tét hoàn thiện để lấy một bộ trang bị bạc cấp 3 không?");
                } else {
                    sendMessageConfirm("Bạn có muốn dùng bánh tét không?");
                }
            }

            case 93 ->
                    sendMessageConfirm(GameString.createBlackFridayGiftBoxConfirmation(specialItemChest.getQuantity()));

            default -> messageSender.sendServerMessage(us(), GameString.COMBINE_ERROR);
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

    public void handleChangeEquipment(Message ms) throws IOException {
        boolean changeSuccessful = false;
        for (int i = 0; i < 5; i++) {
            int key = ms.reader().readInt();
            EquipmentChest equip = us().getEquipmentByKey(key);
            if (equip == null ||
                    equip.isInUse() ||
                    equip.isExpired() ||
                    equip.getEquipment().isDisguise() ||
                    equip.getEquipment().getLevelRequirement() > us().getCurrentLevel() ||
                    equip.getEquipment().getCharacterId() != us().getActiveCharacterId() || equip.getEquipment().getEquipType() != i
            ) {
                continue;
            }
            EquipmentChest oldEquip = us().getCharacterEquips()[us().getActiveCharacterId()][i];
            if (oldEquip != null) {
                oldEquip.setInUse(false);
            }
            equip.setInUse(true);
            us().getCharacterEquips()[us().getActiveCharacterId()][i] = equip;
            us().getEquipData()[us().getActiveCharacterId()][i] = equip.getKey();
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
                    EquipmentChest equip = us().getEquipmentByKey(key);
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
                    if (us().getXu() < totalTransactionAmount) {
                        messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                        return;
                    }

                    //Trừ phí tháo ngọc
                    us().updateXu(-totalTransactionAmount);

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
                    us().updateInventory(selectedEquipment, null, recoveredGems, null);

                    //Gửi thông báo thành công
                    messageSender.sendServerMessage(us(), GameString.GEM_REMOVAL_SUCCESS);
                } else if (userAction == UserAction.SELL_EQUIPMENT) {//Xác nhận bán trang bị
                    //Kiểm tra có khóa rương không
                    if (us().isChestLocked()) {
                        messageSender.sendServerMessage(us(), GameString.CHEST_LOCKED_NO_SELL);
                        return;
                    }

                    for (EquipmentChest equipment : equipList) {
                        if (equipment.isInUse()) {
                            messageSender.sendServerMessage(us(), GameString.EQUIP_SELL_ERROR_IN_USE);
                            return;
                        }
                        if (equipment.getEmptySlot() < 3) {
                            messageSender.sendServerMessage(us(), GameString.EQUIP_SELL_ERROR_REMOVE_GEMS);
                            return;
                        }
                    }
                    for (EquipmentChest validEquipment : equipList) {
                        us().updateInventory(null, validEquipment, null, null);
                    }
                    us().updateXu(totalTransactionAmount);
                    messageSender.sendServerMessage(us(), GameString.PURCHASE_SUCCESS);
                }
                userAction = null;
            }
        }
    }

    private void purchaseEquipment(short saleIndex, byte unit) {
        if (us().getEquipmentChest().size() >= GameConstants.MAX_EQUIPMENT_SLOTS) {
            messageSender.sendServerMessage(us(), GameString.CHEST_NO_SPACE);
            return;
        }
        Equipment equipment = EquipmentManager.getEquipmentBySaleIndex(saleIndex);
        if (equipment == null || (unit == 0 ? equipment.getPriceXu() : equipment.getPriceLuong()) < 0) {
            return;
        }
        if (unit == 0) {
            if (us().getXu() < equipment.getPriceXu()) {
                messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateXu(-equipment.getPriceXu());
        } else {
            if (us().getLuong() < equipment.getPriceLuong()) {
                messageSender.sendServerMessage(us(), GameString.INSUFFICIENT_FUNDS);
                return;
            }
            us().updateLuong(-equipment.getPriceLuong());
        }
        EquipmentChest newEquip = new EquipmentChest();
        newEquip.setEquipment(equipment);
        us().addEquipment(newEquip);
        messageSender.sendServerMessage(us(), GameString.PURCHASE_SUCCESS);
    }
}
