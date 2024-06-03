package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.Cmd;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;
import com.teamobi.mobiarmy2.util.Until;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class User {

    public static EquipmentEntry[][] nvEquipDefault;
    private ISession session;
    private UserState state;
    private int userId;
    private int playerId;
    private String username;
    private Short clanId;
    private int xu;
    private int luong;
    private int danhVong;
    private boolean isLogged;
    private boolean isLock;
    private boolean isActive;
    private byte nvUsed;
    private int pointEvent;
    private LocalDateTime xpX2Time;
    private LocalDateTime lastOnline;
    private boolean[] ownedCharacters;
    private int[] levels;
    private byte[] levelPercents;
    private int[] xps;
    private int[] points;
    private short[][] pointAdd;
    private byte[] items;
    private int[][] NvData;
    private int[] mission;
    private byte[] missionLevel;
    private EquipmentChestEntry[][] nvEquip;
    private List<Integer> friends;
    private List<SpecialItemChestEntry> ruongDoItem;
    private List<EquipmentChestEntry> ruongDoTB;
    private FightWait fightWait;
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

    public static void setDefaultValue(User user) {
        user.xu = 1000;
        user.luong = 0;
        user.items = new byte[FightItemData.FIGHT_ITEM_ENTRIES.size()];
        user.friends = new ArrayList<>();
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }

    public void logout() {
        userService.handleLogout();
    }

    public int getCurrentLevelPercent() {
        float requiredXp = getCurrentXpLevel();
        float currentXp = getCurrentXp();
        return Until.calculateLevelPercent(currentXp, requiredXp);
    }

    public int getCurrentXpLevel() {
        return XpData.getXpRequestLevel(getCurrentLevel());
    }

    public int getCurrentLevel() {
        return Math.min(levels[nvUsed], 127);
    }

    public int getCurrentXp() {
        return xps[nvUsed];
    }

    public int getCurrentPoint() {
        return points[nvUsed];
    }

    public short[] getCurrentPointAdd() {
        return pointAdd[nvUsed];
    }

    public void updateXu(int xuUp) {
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

    public void updateLuong(int luongUp) {
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

    public void updateDanhVong(int danhVongUp) {
        if (danhVongUp == 0) {
            return;
        }
        long sum = danhVongUp + danhVong;
        if (sum > CommonConstant.MAX_DANH_VONG) {
            danhVong = CommonConstant.MAX_DANH_VONG;
        } else if (sum < CommonConstant.MIN_DANH_VONG) {
            danhVong = CommonConstant.MIN_DANH_VONG;
        } else {
            danhVong += danhVongUp;
        }
        userService.sendUpdateDanhVong(danhVongUp);
    }

    public void updateXp(int xpUp) {
        updateXp(xpUp, false);
    }

    public void updateXp(int xpUp, boolean isXpMultiplier) {
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
            levels[nvUsed] = newLevel;
            points[nvUsed] += levelDiff * CommonConstant.POINT_ON_LEVEL;
        }

        userService.sendUpdateXp(xpUp, levelDiff > 0);
    }

    public short[] getEquip() {
        short[] equip = new short[5];
        if (this.nvEquip[getNvUsed()][5] != null && this.nvEquip[getNvUsed()][5].getEquipmentEntry().isDisguise()) {
            equip[0] = this.nvEquip[getNvUsed()][5].getEquipmentEntry().getDisguiseEquippedIndexes()[0];
            equip[1] = this.nvEquip[getNvUsed()][5].getEquipmentEntry().getDisguiseEquippedIndexes()[1];
            equip[2] = this.nvEquip[getNvUsed()][5].getEquipmentEntry().getDisguiseEquippedIndexes()[2];
            equip[3] = this.nvEquip[getNvUsed()][5].getEquipmentEntry().getDisguiseEquippedIndexes()[3];
            equip[4] = this.nvEquip[getNvUsed()][5].getEquipmentEntry().getDisguiseEquippedIndexes()[4];
        } else {
            for (int i = 0; i < 5; i++) {
                if (this.nvEquip[getNvUsed()][i] != null && !this.nvEquip[getNvUsed()][i].getEquipmentEntry().isDisguise()) {
                    equip[i] = this.nvEquip[getNvUsed()][i].getEquipmentEntry().getEquipIndex();
                } else if (nvEquipDefault[getNvUsed()][i] != null) {
                    equip[i] = nvEquipDefault[getNvUsed()][i].getEquipIndex();
                } else {
                    equip[i] = -1;
                }
            }
        }
        return equip;
    }

    public void updateItems(byte itemIndex, int quantity) {
        this.items[itemIndex] += quantity;
        if (this.items[itemIndex] < 0) {
            this.items[itemIndex] = 0;
        }
        if (this.items[itemIndex] > ServerManager.getInstance().config().getMax_item()) {
            this.items[itemIndex] = (byte) ServerManager.getInstance().config().getMax_item();
        }
        this.items[0] = this.items[1] = 99;
    }

    public int getNumItemRuong(int id) {
        // Kiem tra trong ruong co=>tang so luong. ko co=> tao moi
        for (SpecialItemChestEntry spE1 : ruongDoItem) {
            if (spE1.getItem().getId() == id) {
                return spE1.getQuantity();
            }
        }
        return 0;
    }

    public synchronized void addEquipment(EquipmentChestEntry addEquipment) {
        if (addEquipment == null) {
            return;
        }

        addEquipment.setPurchaseDate(LocalDateTime.now());
        addEquipment.setInUse(false);
        if (addEquipment.getAdditionalPoints() == null) {
            addEquipment.setAdditionalPoints(addEquipment.getEquipmentEntry().getAdditionalPoints());
        }
        if (addEquipment.getAdditionalPercent() == null) {
            addEquipment.setAdditionalPercent(addEquipment.getEquipmentEntry().getAdditionalPercent());
        }
        addEquipment.setEmptySlot((byte) 3);
        addEquipment.setSlots(new byte[]{-1, -1, -1});

        int bestLocation = ruongDoTB.indexOf(null);
        if (bestLocation == -1) {
            addEquipment.setIndex(ruongDoTB.size());
            ruongDoTB.add(addEquipment);
        } else {
            addEquipment.setIndex(bestLocation);
            ruongDoTB.set(bestLocation, addEquipment);
        }

        try {
            Message ms = new Message(Cmd.BUY_EQUIP);
            DataOutputStream ds = ms.writer();
            ds.writeByte(0);
            ds.writeInt(addEquipment.getIndex() | 0x10000);
            ds.writeByte(addEquipment.getEquipmentEntry().getCharacterId());
            ds.writeByte(addEquipment.getEquipmentEntry().getEquipType());
            ds.writeShort(addEquipment.getEquipmentEntry().getEquipIndex());
            ds.writeUTF(addEquipment.getEquipmentEntry().getName());
            ds.writeByte(addEquipment.getAdditionalPoints().length * 2);
            for (int i = 0; i < addEquipment.getAdditionalPoints().length; i++) {
                ds.writeByte(addEquipment.getAdditionalPoints()[i]);
                ds.writeByte(addEquipment.getAdditionalPercent()[i]);
            }
            ds.writeByte(addEquipment.getEquipmentEntry().getExpirationDays());
            ds.writeByte(addEquipment.getEquipmentEntry().isDisguise() ? 1 : 0);
            ds.writeByte(addEquipment.getVipLevel());
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateInventory(
            EquipmentChestEntry updateEquipment,
            int removeEquipmentIndex,
            List<SpecialItemChestEntry> addItems,
            List<SpecialItemChestEntry> removeItems
    ) {
        try {
            Message ms;
            DataOutputStream ds;
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds1 = new DataOutputStream(bas);
            int updateQuantity = 0;

            if (updateEquipment != null) {
                updateQuantity++;
                ds1.writeByte(2);
                ds1.writeInt(updateEquipment.getIndex() | 0x10000);
                ds1.writeByte(updateEquipment.getAdditionalPoints().length * 2);
                for (int i = 0; i < updateEquipment.getAdditionalPoints().length; i++) {
                    ds1.writeByte(updateEquipment.getAdditionalPoints()[i]);
                    ds1.writeByte(updateEquipment.getAdditionalPercent()[i]);
                }
                ds1.writeByte(updateEquipment.getEmptySlot());
                // Ngay het han
                int hanSD = updateEquipment.getEquipmentEntry().getExpirationDays() - Until.getNumDay(updateEquipment.getPurchaseDate(), LocalDateTime.now());
                if (hanSD < 0) {
                    hanSD = 0;
                }
                ds1.writeByte(hanSD);
            }

            if (addItems != null && !addItems.isEmpty()) {
                for (SpecialItemChestEntry newItem : addItems) {
                    if (newItem.getQuantity() <= 0) {
                        continue;
                    }
                    updateQuantity++;
                    SpecialItemChestEntry existingItem = ruongDoItem.stream()
                            .filter(item -> item.getItem().getId() == newItem.getItem().getId())
                            .findFirst()
                            .orElse(null);
                    if (existingItem != null) {
                        existingItem.increaseQuantity(newItem.getQuantity());
                    } else {
                        ruongDoItem.add(newItem);
                    }
                    ds1.writeByte(newItem.getQuantity() > 1 ? 3 : 1);
                    ds1.writeByte(newItem.getItem().getId());
                    if (newItem.getQuantity() > 1) {
                        ds1.writeByte(newItem.getQuantity());
                    }
                    ds1.writeUTF(newItem.getItem().getName());
                    ds1.writeUTF(newItem.getItem().getDetail());
                }
            }

            if (removeItems != null && !removeItems.isEmpty()) {
                for (SpecialItemChestEntry itemToRemove : removeItems) {
                    if (itemToRemove.getQuantity() <= 0) {
                        continue;
                    }
                    SpecialItemChestEntry existingItem = ruongDoItem.stream()
                            .filter(item -> item.getItem().getId() == itemToRemove.getItem().getId())
                            .findFirst()
                            .orElse(null);
                    if (existingItem != null) {
                        existingItem.decreaseQuantity(itemToRemove.getQuantity());
                        if (existingItem.getQuantity() <= 0) {
                            ruongDoItem.remove(existingItem);
                        }
                        updateQuantity++;
                        ds1.writeByte(0);
                        ds1.writeInt(itemToRemove.getItem().getId());
                        ds1.writeByte(itemToRemove.getQuantity());
                    }
                }
            }

            if (removeEquipmentIndex >= 0 && removeEquipmentIndex < ruongDoTB.size() && ruongDoTB.get(removeEquipmentIndex) != null) {
                updateQuantity++;
                ruongDoTB.set(removeEquipmentIndex, null);
                ds1.writeByte(0);
                ds1.writeInt(removeEquipmentIndex | 0x10000);
                ds1.writeByte(1);
            }

            ds1.flush();
            bas.flush();

            if (updateQuantity == 0) {
                return;
            }

            ms = new Message(Cmd.INVENTORY_UPDATE);
            ds = ms.writer();
            ds.writeByte(updateQuantity);
            ds.write(bas.toByteArray());
            ds.flush();
            sendMessage(ms);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePoints(short[] pointsToAdd, short totalPointsToSubtract) {
        for (int i = 0; i < 5; i++) {
            pointAdd[nvUsed][i] += pointsToAdd[i];
        }
        points[nvUsed] -= totalPointsToSubtract;
    }

    public void updateMission(int missionId, int quantity) {
        if (missionId < 0 || missionId >= mission.length) {
            return;
        }
        mission[missionId] += quantity;
    }
}
