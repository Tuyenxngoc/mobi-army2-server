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
    private int[][] equipData;
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

    public synchronized void updateDanhVong(int danhVongUp) {
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
            levels[nvUsed] = newLevel;
            points[nvUsed] += levelDiff * CommonConstant.POINT_ON_LEVEL;
        }

        userService.sendUpdateXp(xpUp, levelDiff > 0);
    }

    public short[] getEquip() {
        short[] equip = new short[5];
        if (this.nvEquip[getNvUsed()][5] != null && this.nvEquip[getNvUsed()][5].getEquipEntry().isDisguise()) {
            equip[0] = this.nvEquip[getNvUsed()][5].getEquipEntry().getDisguiseEquippedIndexes()[0];
            equip[1] = this.nvEquip[getNvUsed()][5].getEquipEntry().getDisguiseEquippedIndexes()[1];
            equip[2] = this.nvEquip[getNvUsed()][5].getEquipEntry().getDisguiseEquippedIndexes()[2];
            equip[3] = this.nvEquip[getNvUsed()][5].getEquipEntry().getDisguiseEquippedIndexes()[3];
            equip[4] = this.nvEquip[getNvUsed()][5].getEquipEntry().getDisguiseEquippedIndexes()[4];
        } else {
            for (int i = 0; i < 5; i++) {
                if (this.nvEquip[getNvUsed()][i] != null && !this.nvEquip[getNvUsed()][i].getEquipEntry().isDisguise()) {
                    equip[i] = this.nvEquip[getNvUsed()][i].getEquipEntry().getEquipIndex();
                } else if (nvEquipDefault[getNvUsed()][i] != null) {
                    equip[i] = nvEquipDefault[getNvUsed()][i].getEquipIndex();
                } else {
                    equip[i] = -1;
                }
            }
        }
        return equip;
    }

    public synchronized void updateItems(byte itemIndex, int quantity) {
        this.items[itemIndex] += quantity;
        if (this.items[itemIndex] < 0) {
            this.items[itemIndex] = 0;
        }
        if (this.items[itemIndex] > ServerManager.getInstance().config().getMaxItem()) {
            this.items[itemIndex] = (byte) ServerManager.getInstance().config().getMaxItem();
        }
        this.items[0] = this.items[1] = 99;
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
        addEquipment.setKey((int) (System.currentTimeMillis() & 0xFFFFFF));
        ruongDoTB.add(addEquipment);

        try {
            Message ms = new Message(Cmd.BUY_EQUIP);
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
            Message ms;
            DataOutputStream ds;
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds1 = new DataOutputStream(bas);
            int updateQuantity = 0;

            if (updateEquip != null) {
                updateQuantity++;
                ds1.writeByte(2);
                ds1.writeInt(updateEquip.getKey());
                ds1.writeByte(updateEquip.getAddPoints().length * 2);
                for (int i = 0; i < updateEquip.getAddPoints().length; i++) {
                    ds1.writeByte(updateEquip.getAddPoints()[i]);
                    ds1.writeByte(updateEquip.getAddPercents()[i]);
                }
                ds1.writeByte(updateEquip.getEmptySlot());
                ds1.writeByte(updateEquip.getRemainingDays());
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

            if (removeEquip != null) {
                updateQuantity++;
                ruongDoTB.remove(removeEquip);
                ds1.writeByte(0);
                ds1.writeInt(removeEquip.getKey());
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

    public synchronized void updatePoints(short[] pointsToAdd, short totalPointsToSubtract) {
        for (int i = 0; i < 5; i++) {
            pointAdd[nvUsed][i] += pointsToAdd[i];
        }
        points[nvUsed] -= totalPointsToSubtract;
    }

    public synchronized void updateMission(int missionId, int quantity) {
        if (missionId < 0 || missionId >= mission.length) {
            return;
        }
        mission[missionId] += quantity;
    }

    public EquipmentChestEntry getEquipmentByKey(int key) {
        return ruongDoTB.stream()
                .filter(equip -> equip.getKey() == key)
                .findFirst()
                .orElse(null);
    }
}
