package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.ApplicationContext;
import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.dao.*;
import com.teamobi.mobiarmy2.fight.IFightWait;
import com.teamobi.mobiarmy2.fight.IGiftBoxManager;
import com.teamobi.mobiarmy2.fight.ITrainingManager;
import com.teamobi.mobiarmy2.fight.impl.GiftBoxManager;
import com.teamobi.mobiarmy2.network.IMessage;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.impl.Message;
import com.teamobi.mobiarmy2.server.*;
import com.teamobi.mobiarmy2.service.IClanService;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.impl.UserService;
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
    private ISession session;
    private UserState state;
    private String accountId;
    private int userId;
    private String username;
    private Short clanId;
    private int xu;
    private int luong;
    private int cup;
    private boolean isLogged;
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
    private int[] xps;
    private int[] points;
    private short[][] addedPoints;
    private byte[] items;
    private int[][] equipData;
    private int[] mission;
    private byte[] missionLevel;
    private EquipmentChest[][] characterEquips;
    private List<Integer> friends;
    private List<SpecialItemChest> specialItemChest;
    private List<EquipmentChest> equipmentChest;
    private IFightWait fightWait;
    private ITrainingManager trainingManager;
    private final IUserService userService;
    private final IGiftBoxManager giftBoxManager;
    private int topEarningsXu;

    public User(ISession session) {
        this.session = session;
        this.state = UserState.WAITING;
        ApplicationContext context = ApplicationContext.getInstance();
        this.userService = new UserService(
                this,
                context.getBean(IClanService.class),
                context.getBean(IUserDAO.class),
                context.getBean(IAccountDAO.class),
                context.getBean(IGiftCodeDAO.class),
                context.getBean(IUserGiftCodeDAO.class),
                context.getBean(IUserCharacterDAO.class),
                context.getBean(IUserEquipmentDAO.class),
                context.getBean(IUserSpecialItemDAO.class)
        );
        this.giftBoxManager = new GiftBoxManager(this);
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public boolean isOpeningGift() {
        return giftBoxManager.isOpeningGift();
    }

    public void sendMessage(IMessage ms) {
        session.sendMessage(ms);
    }

    public int getCurrentLevelPercent() {
        int currentXp = getCurrentXp();
        int currentLevel = getCurrentLevel();

        int requiredXpCurrentLevel = PlayerXpManager.getRequiredXpLevel(currentLevel - 1);
        int requiredXpNextLevel = PlayerXpManager.getRequiredXpLevel(currentLevel);

        int currentXpInLevel = currentXp - requiredXpCurrentLevel;
        int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;

        return Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);
    }

    public int getCurrentRequiredXp() {
        return PlayerXpManager.getRequiredXpLevel(getCurrentLevel());
    }

    public int getCurrentLevel() {
        return levels[activeCharacterId];
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
        if (sum > GameConstants.MAX_XU) {
            xu = GameConstants.MAX_XU;
        } else if (sum < GameConstants.MIN_XU) {
            xu = GameConstants.MIN_XU;
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
        if (sum > GameConstants.MAX_LUONG) {
            luong = GameConstants.MAX_LUONG;
        } else if (sum < GameConstants.MIN_LUONG) {
            luong = GameConstants.MIN_LUONG;
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
        if (sum > GameConstants.MAX_CUP) {
            cup = GameConstants.MAX_CUP;
        } else if (sum < GameConstants.MIN_CUP) {
            cup = GameConstants.MIN_CUP;
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

        if (isXpMultiplier && xpX2Time != null) {
            if (xpX2Time.isAfter(LocalDateTime.now())) {
                xpUp *= 2;
            }
        }

        int oldXp = getCurrentXp();
        long totalXp = xpUp + oldXp;
        if (totalXp > GameConstants.MAX_XP) {
            totalXp = GameConstants.MAX_XP;
        }

        int currentLevel = getCurrentLevel();
        int newLevel = PlayerXpManager.getLevelByXP(totalXp);

        int levelDiff = newLevel - currentLevel;
        if (levelDiff > 0) {
            levels[activeCharacterId] = newLevel;
            points[activeCharacterId] += levelDiff * GameConstants.POINT_ON_LEVEL;
        }
        xps[activeCharacterId] = (int) totalXp;

        userService.sendUpdateXp(xpUp, levelDiff > 0);
    }

    public short[] getEquips() {
        short[] equips = new short[5];
        EquipmentChest[] equipEntries = characterEquips[activeCharacterId];

        if (equipEntries[5] != null && equipEntries[5].getEquipment().isDisguise()) {
            short[] disguiseIndexes = equipEntries[5].getEquipment().getDisguiseEquippedIndexes();
            System.arraycopy(disguiseIndexes, 0, equips, 0, 5);
        } else {
            for (byte i = 0; i < 5; i++) {
                if (equipEntries[i] != null && !equipEntries[i].getEquipment().isDisguise()) {
                    equips[i] = equipEntries[i].getEquipment().getEquipIndex();
                } else if (EquipmentManager.equipDefault[activeCharacterId][i] != null) {
                    equips[i] = EquipmentManager.equipDefault[activeCharacterId][i].getEquipIndex();
                } else {
                    equips[i] = -1;
                }
            }
        }
        return equips;
    }

    public synchronized void updateItems(byte itemIndex, byte quantity) {
        if (itemIndex < 0 || itemIndex >= items.length) {
            return;
        }

        items[itemIndex] += quantity;
        if (items[itemIndex] < 0) {
            items[itemIndex] = 0;
        }
        if (items[itemIndex] > ServerManager.getInstance().getConfig().getMaxItem()) {
            items[itemIndex] = ServerManager.getInstance().getConfig().getMaxItem();
        }
        items[0] = items[1] = 99;
    }

    public synchronized void addEquipment(EquipmentChest addEquipment) {
        if (addEquipment == null) {
            return;
        }

        addEquipment.setPurchaseDate(LocalDateTime.now());
        addEquipment.setInUse(false);
        if (addEquipment.getAddPoints() == null) {
            addEquipment.setAddPoints(addEquipment.getEquipment().getAddPoints());
        }
        if (addEquipment.getAddPercents() == null) {
            addEquipment.setAddPercents(addEquipment.getEquipment().getAddPercents());
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
            ds.writeByte(addEquipment.getEquipment().getCharacterId());
            ds.writeByte(addEquipment.getEquipment().getEquipType());
            ds.writeShort(addEquipment.getEquipment().getEquipIndex());
            ds.writeUTF(addEquipment.getEquipment().getName());
            ds.writeByte(addEquipment.getAddPoints().length * 2);
            for (int i = 0; i < addEquipment.getAddPoints().length; i++) {
                ds.writeByte(addEquipment.getAddPoints()[i]);
                ds.writeByte(addEquipment.getAddPercents()[i]);
            }
            ds.writeByte(addEquipment.getEquipment().getExpirationDays());
            ds.writeByte(addEquipment.getEquipment().isDisguise() ? 1 : 0);
            ds.writeByte(addEquipment.getVipLevel());
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateInventory(
            EquipmentChest updateEquip,
            EquipmentChest removeEquip,
            List<SpecialItemChest> addItems,
            List<SpecialItemChest> removeItems
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
                for (SpecialItemChest newItem : addItems) {
                    if (newItem.getQuantity() <= 0) {
                        continue;
                    }
                    updateQuantity++;
                    SpecialItemChest existingItem = getSpecialItemById(newItem.getItem().getId());
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
                for (SpecialItemChest itemToRemove : removeItems) {
                    if (itemToRemove.getQuantity() <= 0) {
                        continue;
                    }
                    SpecialItemChest existingItem = getSpecialItemById(itemToRemove.getItem().getId());
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

    public EquipmentChest getEquipmentByKey(int key) {
        return equipmentChest.stream()
                .filter(equip -> equip.getKey() == key)
                .findFirst()
                .orElse(null);
    }

    public SpecialItemChest getSpecialItemById(byte id) {
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
        SpecialItemChest specialItemChest = this.specialItemChest.stream()
                .filter(item -> item.getItem().getId() == itemId)
                .findFirst()
                .orElse(null);
        if (specialItemChest == null) {
            return 0;
        }
        return specialItemChest.getQuantity();
    }

    public synchronized void addDaysToXpX2Time(int days) {
        LocalDateTime now = LocalDateTime.now();
        if (xpX2Time == null || xpX2Time.isBefore(now)) {
            xpX2Time = now;
        }
        xpX2Time = xpX2Time.plusDays(days);
    }

    public boolean hasEquipment(short equipIndex, byte vipLevel) {
        return equipmentChest.stream()
                .anyMatch(equip -> equip != null && equip.getEquipment() != null &&
                        equip.getEquipment().getEquipIndex() == equipIndex &&
                        equip.getVipLevel() == vipLevel &&
                        equip.getEmptySlot() == 3 &&
                        !equip.isInUse() &&
                        equip.getRemainingDays() > 0
                );
    }

    public EquipmentChest getEquipment(short equipIndex, byte characterId, byte vipLevel) {
        return equipmentChest.stream()
                .filter(equip -> equip != null && equip.getEquipment() != null &&
                        equip.getEquipment().getEquipIndex() == equipIndex &&
                        equip.getEquipment().getCharacterId() == characterId &&
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

    public short getGunId() {
        if (characterEquips[activeCharacterId][0] != null) {
            return this.characterEquips[activeCharacterId][0].getEquipment().getEquipIndex();
        }
        return EquipmentManager.equipDefault[activeCharacterId][0].getEquipIndex();
    }

    /**
     * Calculates the team points for the current character based on the bonus percent.
     *
     * @param bonusPercent The bonus percent to be applied to the team points.
     * @return The calculated team points, capped at a maximum of MAX_ABILITY_VALUE.
     */
    public short calculateTeamPoints(byte bonusPercent) {
        short percents = bonusPercent;
        short points = addedPoints[activeCharacterId][4];

        EquipmentChest[] equippedItems = characterEquips[activeCharacterId];
        for (EquipmentChest equip : equippedItems) {
            if (equip == null || equip.isExpired()) {
                continue;//Bỏ qua nếu trang bị không tồn tại hoặc đã hết hạn
            }

            points += equip.getAddPoints()[4];
            percents += equip.getAddPercents()[4];
        }

        int teamPoints = points * 10;
        teamPoints += (teamPoints * percents / 100);

        return (short) Math.min(teamPoints, GameConstants.MAX_ABILITY_VALUE);
    }

    public short[] calculateCharacterAbilities(short teamPoints) {
        int[] abilities = new int[5];
        short[] percents = new short[5];

        short[] points = addedPoints[activeCharacterId];
        for (int i = 0; i < points.length; i++) {
            points[i] += teamPoints;
        }

        EquipmentChest[] equippedItems = characterEquips[activeCharacterId];
        for (EquipmentChest equip : equippedItems) {
            if (equip == null || equip.isExpired()) {
                continue;//Bỏ qua nếu trang bị không tồn tại hoặc đã hết hạn
            }

            for (byte i = 0; i < points.length; i++) {
                points[i] += equip.getAddPoints()[i];
                percents[i] += equip.getAddPercents()[i];
            }
        }

        abilities[0] = 1000 + (points[0] * 10);
        abilities[0] += (abilities[0] * percents[0] / 100);

        short baseDamage = CharacterManager.CHARACTERS.get(activeCharacterId).getDamage();
        abilities[1] = (baseDamage * (100 + (points[1] / 3) + percents[1]) / 100) * 100 / baseDamage;

        for (byte i = 2; i < abilities.length; i++) {
            abilities[i] = points[i] * 10;
            abilities[i] += (abilities[i] * percents[i] / 100);
        }

        short[] result = new short[abilities.length];
        for (byte i = 0; i < result.length; i++) {
            result[i] = (short) Math.min(abilities[i], GameConstants.MAX_ABILITY_VALUE);
        }

        return result;
    }

    public void addSpecialItem(byte id, short quantity) {
        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(id);
        if (specialItem == null) {
            return;
        }
        SpecialItemChest newItem = new SpecialItemChest(quantity, specialItem);
        updateInventory(null, null, List.of(newItem), null);
    }
}
