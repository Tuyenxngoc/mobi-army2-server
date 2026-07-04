package com.teamobi.mobiarmy2.entity;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.fight.TrainingManager;
import com.teamobi.mobiarmy2.network.Message;
import com.teamobi.mobiarmy2.network.MessageSender;
import com.teamobi.mobiarmy2.network.Session;
import com.teamobi.mobiarmy2.server.EquipmentManager;
import com.teamobi.mobiarmy2.server.SpecialItemManager;
import com.teamobi.mobiarmy2.server.UserXpManager;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class User {
    private final Session session;
    private final MessageSender messageSender;
    private UserState state = UserState.WAITING;
    private String accountId;
    private int userId;
    private String username;
    private short clanId;
    private int xu;
    private int luong;
    private int cup;
    private int rank;
    private boolean isLogged;
    private boolean isChestLocked;
    private boolean isInvitationLocked;
    private byte activeCharacterId;
    private int eventPoint;
    private byte materialsPurchased;
    private short equipmentPurchased;
    private LocalDateTime xpX2Time;
    private int topEarningsXu;
    private long[] userCharacterIds;
    private boolean[] ownedCharacters;
    private int[] levels;
    private int[] xps;
    private int[] points;
    private short[][] addedPoints;
    private byte[] fightItems;
    private int[][] equipData;
    private int[] mission;
    private byte[] missionLevel;
    private EquipmentChest[][] characterEquips;
    private Set<Integer> friends;
    private Map<Byte, SpecialItemChest> specialItemChest;
    private Map<Integer, EquipmentChest> equipmentChest;
    private FightWait fightWait;
    private TrainingManager trainingManager;

    public User(Session session, MessageSender messageSender) {
        this.session = session;
        this.messageSender = messageSender;
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public boolean hasClan() {
        return clanId > 0;
    }

    public int getCurrentLevelPercent() {
        int currentXp = getCurrentXp();
        int currentLevel = getCurrentLevel();

        int requiredXpCurrentLevel = UserXpManager.getRequiredXpLevel(currentLevel - 1);
        int requiredXpNextLevel = UserXpManager.getRequiredXpLevel(currentLevel);

        int currentXpInLevel = currentXp - requiredXpCurrentLevel;
        int xpNeededForNextLevel = requiredXpNextLevel - requiredXpCurrentLevel;

        return Utils.calculateLevelPercent(currentXpInLevel, xpNeededForNextLevel);
    }

    public int getCurrentRequiredXp() {
        return UserXpManager.getRequiredXpLevel(getCurrentLevel());
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

        sendUpdateMoney();
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

        sendUpdateMoney();
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

        sendUpdateCup(cupUp);
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
        int newLevel = UserXpManager.getLevelByXP(totalXp);

        int levelDiff = newLevel - currentLevel;
        if (levelDiff > 0) {
            levels[activeCharacterId] = newLevel;
            points[activeCharacterId] += levelDiff * GameConstants.POINT_ON_LEVEL;
        }
        xps[activeCharacterId] = (int) totalXp;

        sendUpdateXp(xpUp, levelDiff > 0);
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

    public synchronized void updateFightItems(byte itemIndex, byte quantity) {
        if (itemIndex < 0 || itemIndex >= fightItems.length) {
            return;
        }

        fightItems[itemIndex] += quantity;
        if (fightItems[itemIndex] < 0) {
            fightItems[itemIndex] = 0;
        }
        byte maxItem = GameConstants.MAX_FIGHT_ITEM_QUANTITY;
        if (fightItems[itemIndex] > maxItem) {
            fightItems[itemIndex] = maxItem;
        }
        fightItems[0] = fightItems[1] = maxItem;
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
        addEquipment.setKey((userId << 16) | (equipmentPurchased & 0xFFFF));
        addEquipmentChest(addEquipment);

        //Tăng số lượng trang bị mua
        equipmentPurchased++;

        try {
            Message ms = new Message(Cmd.BUY_EQUIP);
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
            messageSender.sendTo(this, ms);
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
                for (byte i = 0; i < updateEquip.getAddPoints().length; i++) {
                    ds.writeByte(updateEquip.getAddPoints()[i]);
                    ds.writeByte(updateEquip.getAddPercents()[i]);
                }
                ds.writeByte(updateEquip.getEmptySlot());
                ds.writeByte(updateEquip.getRemainingDays());
            }

            if (removeEquip != null) {
                equipmentChest.remove(removeEquip.getKey());

                updateQuantity++;
                ds.writeByte(0);
                ds.writeInt(removeEquip.getKey());
                ds.writeByte(1);
            }

            if (addItems != null) {
                for (SpecialItemChest newItem : addItems) {
                    if (newItem.getQuantity() <= 0) {
                        continue;
                    }
                    SpecialItemChest existingItem = getSpecialItemById(newItem.getItem().getId());
                    if (existingItem != null) {
                        existingItem.increaseQuantity(newItem.getQuantity());
                    } else {
                        addSpecialItemChest(newItem);
                    }

                    updateQuantity++;
                    ds.writeByte(newItem.getQuantity() > 1 ? 3 : 1);
                    ds.writeByte(newItem.getItem().getId());
                    if (newItem.getQuantity() > 1) {
                        ds.writeByte(newItem.getQuantity());
                    }
                    ds.writeUTF(newItem.getItem().getName());
                    ds.writeUTF(newItem.getItem().getDetail());
                }
            }

            if (removeItems != null) {
                for (SpecialItemChest itemToRemove : removeItems) {
                    if (itemToRemove.getQuantity() <= 0) {
                        continue;
                    }
                    SpecialItemChest existingItem = getSpecialItemById(itemToRemove.getItem().getId());
                    if (existingItem == null) {
                        continue;
                    }
                    existingItem.decreaseQuantity(itemToRemove.getQuantity());
                    if (existingItem.getQuantity() <= 0) {
                        specialItemChest.remove(itemToRemove.getItem().getId());
                    }

                    updateQuantity++;
                    ds.writeByte(0);
                    ds.writeInt(itemToRemove.getItem().getId());
                    ds.writeByte(itemToRemove.getQuantity());
                }
            }

            ds.flush();
            bas.flush();

            if (updateQuantity == 0) {
                return;
            }

            Message ms = new Message(Cmd.INVENTORY_UPDATE);
            ds = ms.writer();
            ds.writeByte(updateQuantity);
            ds.write(bas.toByteArray());
            ds.flush();
            messageSender.sendTo(this, ms);
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
        return equipmentChest.get(key);
    }

    public SpecialItemChest getSpecialItemById(byte id) {
        return specialItemChest.get(id);
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

    public short getInventorySpecialItemCount(byte id) {
        SpecialItemChest specialItemChest = getSpecialItemById(id);
        if (specialItemChest == null) {
            return 0;
        }
        return specialItemChest.getQuantity();
    }

    public synchronized void addEquipmentChest(EquipmentChest addEquipmentChest) {
        equipmentChest.put(addEquipmentChest.getKey(), addEquipmentChest);
    }

    public synchronized void addSpecialItemChest(SpecialItemChest addSpecialItemChest) {
        specialItemChest.put(addSpecialItemChest.getItem().getId(), addSpecialItemChest);
    }

    public synchronized void addDaysToXpX2Time(int days) {
        LocalDateTime now = LocalDateTime.now();
        if (xpX2Time == null || xpX2Time.isBefore(now)) {
            xpX2Time = now;
        }
        xpX2Time = xpX2Time.plusDays(days);
    }

    public boolean hasEquipment(short equipIndex, byte vipLevel) {
        return equipmentChest.values().stream()
                .anyMatch(equip -> equip != null && equip.getEquipment() != null &&
                        equip.getEquipment().getEquipIndex() == equipIndex &&
                        equip.getVipLevel() == vipLevel &&
                        equip.getEmptySlot() == 3 &&
                        !equip.isInUse() &&
                        !equip.isExpired()
                );
    }

    public EquipmentChest getEquipment(short equipIndex, byte characterId, byte vipLevel) {
        return equipmentChest.values().stream()
                .filter(equip -> equip != null && equip.getEquipment() != null &&
                        equip.getEquipment().getEquipIndex() == equipIndex &&
                        equip.getEquipment().getCharacterId() == characterId &&
                        equip.getVipLevel() == vipLevel &&
                        equip.getEmptySlot() == 3 &&
                        !equip.isInUse() &&
                        !equip.isExpired())
                .findFirst()
                .orElse(null);
    }

    public byte getItemFightQuantity(int index) {
        if (index >= 0 && index < fightItems.length) {
            return fightItems[index];
        }
        return 0;
    }

    public short getGunId() {
        if (characterEquips[activeCharacterId][0] != null) {
            return this.characterEquips[activeCharacterId][0].getEquipment().getEquipIndex();
        }
        return EquipmentManager.equipDefault[activeCharacterId][0].getEquipIndex();
    }

    public int calculateTeamPoints(byte bonusPercent) {
        short percents = bonusPercent;
        short points = addedPoints[activeCharacterId][4];// Điểm cộng thêm cho chỉ số đồng đội

        EquipmentChest[] equippedItems = characterEquips[activeCharacterId];
        for (EquipmentChest equip : equippedItems) {
            if (equip == null || equip.isExpired()) {
                continue;//Bỏ qua nếu trang bị không tồn tại hoặc đã hết hạn
            }

            // Cộng thêm điểm và % cộng thêm từ trang bị đang mặc cho chỉ số đồng đội
            points += equip.getAddPoints()[4];
            percents += equip.getAddPercents()[4];
        }

        // Tính điểm đồng đội: 10 đơn vị mỗi điểm + % cộng thêm từ trang bị và bonusPercent
        int teamPoints = points * 10;
        teamPoints = Utils.calculatePercentBonus(teamPoints, percents);

        return teamPoints;
    }

    public int[] calculateCharacterAbilities(short teamPoints) {
        int[] ability = new int[4];

        // base ability của nhân vật
        short[] source = addedPoints[activeCharacterId];
        int[] baseAbility = new int[source.length];

        for (int i = 0; i < source.length; i++) {
            baseAbility[i] = source[i];
        }

        // cộng điểm team
        for (int i = 0; i < baseAbility.length; i++) {
            baseAbility[i] += teamPoints;
        }

        // tổng điểm và % từ trang bị
        short[] totalPointEquip = new short[5];
        short[] totalPercentEquip = new short[5];

        EquipmentChest[] equippedItems = characterEquips[activeCharacterId];
        for (EquipmentChest equip : equippedItems) {
            if (equip == null || equip.isExpired()) {
                continue;//Bỏ qua nếu trang bị không tồn tại hoặc đã hết hạn
            }

            byte[] addPoints = equip.getAddPoints();
            byte[] addPercents = equip.getAddPercents();

            for (byte i = 0; i < 5; i++) {
                totalPointEquip[i] += addPoints[i];
                totalPercentEquip[i] += addPercents[i];
            }
        }

        // HP
        int hp = 1000 + baseAbility[0] * 10 + totalPointEquip[0] * 10;
        hp += (1000 + baseAbility[0]) * totalPercentEquip[0] / 100;
        ability[0] = hp;

        // DAMAGE
        int damPoint = baseAbility[1] + totalPointEquip[1];
        ability[1] = damPoint / 3 + 100 + totalPercentEquip[1]; // % damage

        // DEFENSE
        int defPoint = baseAbility[2] + totalPointEquip[2];
        ability[2] = Utils.calculatePercentBonus(defPoint * 10, totalPercentEquip[2]);

        // LUCK
        int luckPoint = baseAbility[3] + totalPointEquip[3];
        ability[3] = Utils.calculatePercentBonus(luckPoint * 10, totalPercentEquip[3]);

        return ability;
    }

    public void addSpecialItem(byte id, short quantity) {
        SpecialItem specialItem = SpecialItemManager.getSpecialItemById(id);
        if (specialItem == null) {
            return;
        }
        SpecialItemChest newItem = new SpecialItemChest(quantity, specialItem);
        updateInventory(null, null, List.of(newItem), null);
    }

    public void sendUpdateMoney() {
        try {
            Message ms = new Message(Cmd.UPDATE_MONEY);
            DataOutputStream ds = ms.writer();
            ds.writeInt(xu);
            ds.writeInt(luong);
            ds.flush();
            messageSender.sendTo(this, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateCup(int cupUp) {
        try {
            Message ms = new Message(Cmd.CUP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(cupUp);
            ds.writeInt(cup);
            ds.flush();
            messageSender.sendTo(this, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendUpdateXp(int xpUp, boolean updateLevel) {
        try {
            Message ms = new Message(Cmd.UPDATE_EXP);
            DataOutputStream ds = ms.writer();
            ds.writeInt(xpUp);
            ds.writeInt(getCurrentXp());
            ds.writeInt(getCurrentRequiredXp());
            if (updateLevel) {
                ds.writeByte(1);
                ds.writeByte(getCurrentLevel());
                ds.writeByte(getCurrentLevelPercent());
                ds.writeShort(getCurrentPoint());
            } else {
                ds.writeByte(0);
                ds.writeByte(getCurrentLevelPercent());
            }
            ds.flush();
            messageSender.sendTo(this, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendCharacterInfo() {
        try {
            Message ms = new Message(Cmd.CHARACTOR_INFO);
            DataOutputStream ds = ms.writer();
            ds.writeByte(getCurrentLevel());
            ds.writeByte(getCurrentLevelPercent());
            ds.writeShort(getCurrentPoint());
            for (short point : getCurrentAddedPoints()) {
                ds.writeShort(point);
            }
            ds.writeInt(getCurrentXp());
            ds.writeInt(getCurrentRequiredXp());
            ds.writeInt(cup);
            ds.flush();
            messageSender.sendTo(this, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendEquipInfo() {
        try {
            Message ms = new Message(Cmd.CURR_EQUIP_DBKEY);
            DataOutputStream ds = ms.writer();
            for (int i = 0; i < 5; i++) {
                ds.writeInt(equipData[activeCharacterId][i]);
            }
            ds.flush();
            messageSender.sendTo(this, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendInventoryInfo() {
        try {
            Message ms = new Message(Cmd.INVENTORY);
            DataOutputStream ds = ms.writer();
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
                ds.writeInt(getEquipData()[getActiveCharacterId()][i]);
            }
            ds.flush();
            messageSender.sendTo(this, ms);

            ms = new Message(Cmd.MATERIAL);
            ds = ms.writer();
            ds.writeByte(0);
            ds.writeByte(specialItemChest.size());
            for (SpecialItemChest item : specialItemChest.values()) {
                ds.writeByte(item.getItem().getId());
                ds.writeShort(item.getQuantity());
                ds.writeUTF(item.getItem().getName());
                ds.writeUTF(item.getItem().getDetail());
            }
            ds.flush();
            messageSender.sendTo(this, ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
