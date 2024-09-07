package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.IFightWait;
import com.teamobi.mobiarmy2.fight.ITrainingManager;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class User {

    public static EquipmentEntry[][] equipDefault;
    private ISession session;
    private UserState state;
    private String userId;
    private int playerId;
    private String username;
    private short clanId;
    private int xu;
    private int luong;
    private int cup;
    private boolean isLogged;
    private boolean isLock;
    private boolean isActive;
    private boolean isChestLocked;
    private boolean isInvitationLocked;
    private byte activeCharacterId;
    private int pointEvent;
    private byte materialsPurchased;
    private short equipmentPurchased;
    private LocalDateTime xpX2Time;
    private LocalDateTime lastOnline;
    private long[] playerCharacterIds;
    private boolean[] ownedCharacters;
    private int[] levels;
    private byte[] levelPercents;
    private int[] xps;
    private int[] points;
    private short[][] addedPoints;
    private byte[] items;
    private int[][] equipData;
    private int[] mission;
    private byte[] missionLevel;
    private EquipmentChestEntry[][] characterEquips;
    private List<Integer> friends;
    private List<SpecialItemChestEntry> specialItemChest;
    private List<EquipmentChestEntry> equipmentChest;
    private IFightWait fightWait;
    private ITrainingManager trainingManager;
    private final IUserService userService;
    private boolean openingGift;
    private int topEarningsXu;

    public User() {
        this.state = UserState.WAITING;
        this.userService = new UserService(this);
    }

    public User(ISession session) {
        this();
        this.session = session;
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public void sendMessage(IMessage ms) {
        session.sendMessage(ms);
    }

    public int getCurrentLevelPercent() {
        float requiredXp = getCurrentXpLevel();
        float currentXp = getCurrentXp();
        return Utils.calculateLevelPercent(currentXp, requiredXp);
    }

    public int getCurrentXpLevel() {
        return XpData.getXpRequestLevel(getCurrentLevel());
    }

    public int getCurrentLevel() {
        return Math.min(levels[activeCharacterId], 127);
    }

    public int getCurrentXp() {
        return xps[activeCharacterId];
    }

    public int getCurrentPoint() {
        return points[activeCharacterId];
    }

    public short[] getCurrentAddedPoints() {
        return addedPoints[activeCharacterId];
    }

    public synchronized void updateXu(int xuUp) {
        if (xuUp == 0) {
            return;
        }
        long sum = xuUp + xu;
        if (sum > CommonConstant.MAX_XU) {
            xu = CommonConstant.MAX_XU;
        } else if (sum < CommonConstant.MIN_XU) {
            xu = CommonConstant.MIN_XU;
        } else {
            xu += xuUp;
        }
        userService.sendUpdateMoney();
    }

    public synchronized void updateLuong(int luongUp) {
        if (luongUp == 0) {
            return;
        }
        long sum = luongUp + luong;
        if (sum > CommonConstant.MAX_LUONG) {
            luong = CommonConstant.MAX_LUONG;
        } else if (sum < CommonConstant.MIN_LUONG) {
            luong = CommonConstant.MIN_LUONG;
        } else {
            luong += luongUp;
        }
        userService.sendUpdateMoney();
    }

    public synchronized void updateCup(int cupUp) {
        if (cupUp == 0) {
            return;
        }
        long sum = cupUp + cup;
        if (sum > CommonConstant.MAX_CUP) {
            cup = CommonConstant.MAX_CUP;
        } else if (sum < CommonConstant.MIN_CUP) {
            cup = CommonConstant.MIN_CUP;
        } else {
            cup += cupUp;
        }
        userService.sendUpdateCup(cupUp);
    }

    public synchronized void updateXp(int xpUp) {
        updateXp(xpUp, false);
    }

    public synchronized void updateXp(int xpUp, boolean isXpMultiplier) {
        if (xpUp <= 0) {
            return;
        }

        if (isXpMultiplier) {
            if (xpX2Time.isAfter(LocalDateTime.now())) {
                xpUp *= 2;
            }
        }

        int oldXp = getCurrentXp();
        long totalXp = xpUp + oldXp;
        if (totalXp > CommonConstant.MAX_XP) {
            totalXp = CommonConstant.MAX_XP;
        }

        int currentLevel = getCurrentLevel();
        int newLevel = XpData.getLevelByEXP(totalXp);

        int levelDiff = newLevel - currentLevel;
        if (levelDiff > 0) {
            levels[activeCharacterId] = newLevel;
            points[activeCharacterId] += levelDiff * CommonConstant.POINT_ON_LEVEL;
        }

        userService.sendUpdateXp(xpUp, levelDiff > 0);
    }

    public short[] getEquips() {
        short[] equips = new short[5];
        EquipmentChestEntry[] equipEntries = characterEquips[activeCharacterId];

        if (equipEntries[5] != null && equipEntries[5].getEquipEntry().isDisguise()) {
            short[] disguiseIndexes = equipEntries[5].getEquipEntry().getDisguiseEquippedIndexes();
            System.arraycopy(disguiseIndexes, 0, equips, 0, 5);
        } else {
            for (byte i = 0; i < 5; i++) {
                if (equipEntries[i] != null && !equipEntries[i].getEquipEntry().isDisguise()) {
                    equips[i] = equipEntries[i].getEquipEntry().getEquipIndex();
                } else if (equipDefault[activeCharacterId][i] != null) {
                    equips[i] = equipDefault[activeCharacterId][i].getEquipIndex();
                } else {
                    equips[i] = -1;
                }
            }
        }
        return equips;
    }

    public synchronized void updateItems(byte itemIndex, byte quantity) {
        items[itemIndex] += quantity;
        if (items[itemIndex] < 0) {
            items[itemIndex] = 0;
        }
        if (items[itemIndex] > ServerManager.getInstance().config().getMaxItem()) {
            items[itemIndex] = ServerManager.getInstance().config().getMaxItem();
        }
        items[0] = items[1] = 99;
    }

    public synchronized void addEquipment(EquipmentChestEntry addEquipment) {
        if (addEquipment == null) {
            return;
        }

        addEquipment.setPurchaseDate(LocalDateTime.now());
        addEquipment.setInUse(false);
        if (addEquipment.getAddPoints() == null) {
            addEquipment.setAddPoints(addEquipment.getEquipEntry().getAddPoints());
        }
        if (addEquipment.getAddPercents() == null) {
            addEquipment.setAddPercents(addEquipment.getEquipEntry().getAddPercents());
        }
        addEquipment.setEmptySlot((byte) 3);
        addEquipment.setSlots(new byte[]{-1, -1, -1});
        addEquipment.setKey(equipmentPurchased | 0x10000);
        equipmentChest.add(addEquipment);

        //Tăng số lượng trang bị mua
        equipmentPurchased++;

        try {
            IMessage ms = new Message(Cmd.BUY_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeInt(addEquipment.getKey());
            ds.writeByte(addEquipment.getEquipEntry().getCharacterId());
            ds.writeByte(addEquipment.getEquipEntry().getEquipType());
            ds.writeShort(addEquipment.getEquipEntry().getEquipIndex());
            ds.writeUTF(addEquipment.getEquipEntry().getName());
            ds.writeByte(addEquipment.getAddPoints().length * 2);
            for (int i = 0; i < addEquipment.getAddPoints().length; i++) {
                ds.writeByte(addEquipment.getAddPoints()[i]);
                ds.writeByte(addEquipment.getAddPercents()[i]);
            }
            ds.writeByte(addEquipment.getEquipEntry().getExpirationDays());
            ds.writeByte(addEquipment.getEquipEntry().isDisguise() ? 1 : 0);
            ds.writeByte(addEquipment.getVipLevel());
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateInventory(
            EquipmentChestEntry updateEquip,
            EquipmentChestEntry removeEquip,
            List<SpecialItemChestEntry> addItems,
            List<SpecialItemChestEntry> removeItems
    ) {
        try {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds = new DataOutputStream(bas);
            int updateQuantity = 0;

            if (updateEquip != null) {
                updateQuantity++;
                ds.writeByte(2);
                ds.writeInt(updateEquip.getKey());
                ds.writeByte(updateEquip.getAddPoints().length * 2);
                for (int i = 0; i < updateEquip.getAddPoints().length; i++) {
                    ds.writeByte(updateEquip.getAddPoints()[i]);
                    ds.writeByte(updateEquip.getAddPercents()[i]);
                }
                ds.writeByte(updateEquip.getEmptySlot());
                ds.writeByte(updateEquip.getRemainingDays());
            }

            if (addItems != null && !addItems.isEmpty()) {
                for (SpecialItemChestEntry newItem : addItems) {
                    if (newItem.getQuantity() <= 0) {
                        continue;
                    }
                    updateQuantity++;
                    SpecialItemChestEntry existingItem = getSpecialItemById(newItem.getItem().getId());
                    if (existingItem != null) {
                        existingItem.increaseQuantity(newItem.getQuantity());
                    } else {
                        specialItemChest.add(newItem);
                    }
                    ds.writeByte(newItem.getQuantity() > 1 ? 3 : 1);
                    ds.writeByte(newItem.getItem().getId());
                    if (newItem.getQuantity() > 1) {
                        ds.writeByte(newItem.getQuantity());
                    }
                    ds.writeUTF(newItem.getItem().getName());
                    ds.writeUTF(newItem.getItem().getDetail());
                }
            }

            if (removeItems != null && !removeItems.isEmpty()) {
                for (SpecialItemChestEntry itemToRemove : removeItems) {
                    if (itemToRemove.getQuantity() <= 0) {
                        continue;
                    }
                    SpecialItemChestEntry existingItem = getSpecialItemById(itemToRemove.getItem().getId());
                    if (existingItem != null) {
                        existingItem.decreaseQuantity(itemToRemove.getQuantity());
                        if (existingItem.getQuantity() <= 0) {
                            specialItemChest.remove(existingItem);
                        }
                        updateQuantity++;
                        ds.writeByte(0);
                        ds.writeInt(itemToRemove.getItem().getId());
                        ds.writeByte(itemToRemove.getQuantity());
                    }
                }
            }

            if (removeEquip != null) {
                updateQuantity++;
                equipmentChest.remove(removeEquip);
                ds.writeByte(0);
                ds.writeInt(removeEquip.getKey());
                ds.writeByte(1);
            }

            ds.flush();
            bas.flush();

            if (updateQuantity == 0) {
                return;
            }

            IMessage ms = new Message(Cmd.INVENTORY_UPDATE);
            ds = ms.writer();
            ds.writeByte(updateQuantity);
            ds.write(bas.toByteArray());
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updatePoints(short[] pointsToAdd, int totalPointsToSubtract) {
        for (int i = 0; i < 5; i++) {
            addedPoints[activeCharacterId][i] += pointsToAdd[i];
        }
        points[activeCharacterId] -= totalPointsToSubtract;
    }

    public synchronized void updateMission(int missionId, int quantity) {
        if (missionId < 0 || missionId >= mission.length) {
            return;
        }
        mission[missionId] += quantity;
    }

    public EquipmentChestEntry getEquipmentByKey(int key) {
        return equipmentChest.stream()
                .filter(equip -> equip.getKey() == key)
                .findFirst()
                .orElse(null);
    }

    public SpecialItemChestEntry getSpecialItemById(byte id) {
        return specialItemChest.stream()
                .filter(item -> item.getItem().getId() == id)
                .findFirst()
                .orElse(null);
    }

    public synchronized void resetPoints() {
        int total = -30;
        for (short point : addedPoints[activeCharacterId]) {
            total += point;
        }
        addedPoints[activeCharacterId] = new short[]{0, 0, 10, 10, 10};
        points[activeCharacterId] += total;
    }

    public synchronized void incrementMaterialsPurchased(byte quantity) {
        materialsPurchased += quantity;
    }

    public short getInventorySpecialItemCount(byte itemId) {
        SpecialItemChestEntry specialItemChestEntry = specialItemChest.stream()
                .filter(item -> item.getItem().getId() == itemId)
                .findFirst()
                .orElse(null);
        if (specialItemChestEntry == null) {
            return 0;
        }
        return specialItemChestEntry.getQuantity();
    }

    public synchronized void addDaysToXpX2Time(int days) {
        LocalDateTime now = LocalDateTime.now();
        if (xpX2Time.isBefore(now)) {
            xpX2Time = now;
        }
        xpX2Time = xpX2Time.plusDays(days);
    }

    public boolean hasEquipment(short equipIndex, byte vipLevel) {
        return equipmentChest.stream()
                .anyMatch(equip -> equip != null && equip.getEquipEntry() != null &&
                        equip.getEquipEntry().getEquipIndex() == equipIndex &&
                        equip.getVipLevel() == vipLevel &&
                        equip.getEmptySlot() == 3 &&
                        !equip.isInUse() &&
                        equip.getRemainingDays() > 0
                );
    }

    public EquipmentChestEntry getEquipment(short equipIndex, byte characterId, byte vipLevel) {
        return equipmentChest.stream()
                .filter(equip -> equip != null && equip.getEquipEntry() != null &&
                        equip.getEquipEntry().getEquipIndex() == equipIndex &&
                        equip.getEquipEntry().getCharacterId() == characterId &&
                        equip.getVipLevel() == vipLevel &&
                        equip.getEmptySlot() == 3 &&
                        !equip.isInUse() &&
                        equip.getRemainingDays() > 0)
                .findFirst()
                .orElse(null);
    }

    public byte getItemFightQuantity(int index) {
        if (index >= 0 && index < items.length) {
            return items[index];
        }
        return 0;
    }

    public short getBulletId() {
        if (characterEquips[activeCharacterId][0] != null) {
            return characterEquips[activeCharacterId][0].getEquipEntry().getBulletId();
        }
        return equipDefault[activeCharacterId][0].getBulletId();
    }

    public short getGunId() {
        if (characterEquips[activeCharacterId][0] != null) {
            return this.characterEquips[activeCharacterId][0].getEquipEntry().getEquipIndex();
        }
        return equipDefault[activeCharacterId][0].getEquipIndex();
    }

    public short[] calculateCharacterAbilities() {
        int[] abilities = new int[5];
        short[] points = addedPoints[activeCharacterId];
        short[] percents = new short[5];

        EquipmentChestEntry[] equippedItems = characterEquips[activeCharacterId];
        for (int i = 0; i < equippedItems.length; i++) {
            EquipmentChestEntry equip = equippedItems[i];
            if (equip == null || equip.isExpired()) {
                continue;// Bỏ qua nếu trang bị không tồn tại hoặc đã hết hạn
            }

            for (byte j = 0; j < points.length; j++) {
                points[i] += equip.getAddPoints()[i];
                percents[i] += equip.getAddPercents()[i];
            }
        }

        abilities[0] = 1000 + (points[0] * 10);
        abilities[0] += (abilities[0] * percents[0] / 100);

        short baseDamage = CharacterData.CHARACTER_ENTRIES.get(activeCharacterId).getDamage();
        abilities[1] = (baseDamage * (100 + (points[1] / 3) + percents[1]) / 100) * 100 / baseDamage;

        for (byte i = 2; i < abilities.length; i++) {
            abilities[i] = points[i] * 10;
            abilities[i] += (abilities[i] * percents[i] / 100);
        }

        short[] result = new short[abilities.length];
        for (byte i = 0; i < result.length; i++) {
            result[i] = (short) Math.min(abilities[i], 32000);
        }

        return result;
    }

}
