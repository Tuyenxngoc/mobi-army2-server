package com.teamobi.mobiarmy2.model;

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
import java.util.Date;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class User {

    public static EquipmentEntry[][] nvEquipDefault;
    public ISession session;
    public UserState state;
    public int userId;
    public int playerId;
    public String username;
    public Short clanId;
    public int xu;
    public int luong;
    public int danhVong;
    public boolean isLogged;
    public boolean isLock;
    public boolean isActive;
    public byte nvUsed;
    public int pointEvent;
    public LocalDateTime xpX2Time;
    private LocalDateTime lastOnline;
    public boolean[] nvStt;
    public int[] levels;
    public byte[] levelPercents;
    public int[] xps;
    public int[] points;
    public short[][] pointAdd;
    public byte[] items;
    public int[][] NvData;
    public int[] mission;
    public byte[] missionLevel;
    public EquipmentChestEntry[][] nvEquip;
    public List<Integer> friends;
    public List<SpecialItemChestEntry> ruongDoItem;
    public List<EquipmentChestEntry> ruongDoTB;
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
        user.items = new byte[36];
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
        if (this.nvEquip[getNvUsed()][5] != null && this.nvEquip[getNvUsed()][5].equipmentEntry.isDisguise) {
            equip[0] = this.nvEquip[getNvUsed()][5].equipmentEntry.disguiseEquippedIndexes[0];
            equip[1] = this.nvEquip[getNvUsed()][5].equipmentEntry.disguiseEquippedIndexes[1];
            equip[2] = this.nvEquip[getNvUsed()][5].equipmentEntry.disguiseEquippedIndexes[2];
            equip[3] = this.nvEquip[getNvUsed()][5].equipmentEntry.disguiseEquippedIndexes[3];
            equip[4] = this.nvEquip[getNvUsed()][5].equipmentEntry.disguiseEquippedIndexes[4];
        } else {
            for (int i = 0; i < 5; i++) {
                if (this.nvEquip[getNvUsed()][i] != null && !this.nvEquip[getNvUsed()][i].equipmentEntry.isDisguise) {
                    equip[i] = this.nvEquip[getNvUsed()][i].equipmentEntry.index;
                } else if (nvEquipDefault[getNvUsed()][i] != null) {
                    equip[i] = nvEquipDefault[getNvUsed()][i].index;
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
            if (spE1.item.getId() == id) {
                return spE1.quantity;
            }
        }
        return 0;
    }

    public EquipmentChestEntry getEquipNoNgoc(EquipmentEntry eqE, byte level) {
        for (EquipmentChestEntry rdE : ruongDoTB) {
            if (rdE != null && rdE.equipmentEntry == eqE && !rdE.isUse && rdE.vipLevel == level && rdE.emptySlot == 3 && rdE.equipmentEntry.expirationDays - Until.getNumDay(rdE.purchaseDate, new Date()) > 0) {
                return rdE;
            }
        }
        return null;
    }

    public synchronized void updateRuong(EquipmentChestEntry tbUpdate, EquipmentChestEntry addTB, int removeTB, ArrayList<SpecialItemChestEntry> addItem, ArrayList<SpecialItemChestEntry> removeItem) {
        try {
            Message ms;
            DataOutputStream ds;
            if (addTB != null) {
                int bestLocation = -1;
                for (int i = 0; i < ruongDoTB.size(); i++) {
                    EquipmentChestEntry EquipmentChestEntry = ruongDoTB.get(i);
                    if (EquipmentChestEntry == null) {
                        bestLocation = i;
                        break;
                    }
                }
                addTB.purchaseDate = new Date();
                addTB.isUse = false;
                if (addTB.invAdd == null) {
                    addTB.invAdd = addTB.equipmentEntry.additionalPoints;
                }
                if (addTB.percentAdd == null) {
                    addTB.percentAdd = addTB.equipmentEntry.additionalPercent;
                }
                addTB.emptySlot = 3;
                if (bestLocation == -1) {
                    addTB.index = ruongDoTB.size();
                    ruongDoTB.add(addTB);
                } else {
                    addTB.index = bestLocation;
                    ruongDoTB.set(bestLocation, addTB);
                }
                ms = new Message(104);
                ds = ms.writer();
                ds.writeByte(0);
                ds.writeInt(addTB.index | 0x10000);
                ds.writeByte(addTB.equipmentEntry.characterId);
                ds.writeByte(addTB.equipmentEntry.equipType);
                ds.writeShort(addTB.equipmentEntry.index);
                ds.writeUTF(addTB.equipmentEntry.name);
                ds.writeByte(addTB.invAdd.length * 2);
                for (int i = 0; i < addTB.invAdd.length; i++) {
                    ds.writeByte(addTB.invAdd[i]);
                    ds.writeByte(addTB.percentAdd[i]);
                }
                ds.writeByte(addTB.equipmentEntry.expirationDays);
                ds.writeByte(addTB.equipmentEntry.isDisguise ? 1 : 0);
                ds.writeByte(addTB.vipLevel);
                ds.flush();
                sendMessage(ms);
            }
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            DataOutputStream ds1 = new DataOutputStream(bas);
            int nUpdate = 0;
            if (tbUpdate != null) {
                nUpdate++;
                ds1.writeByte(2);
                ds1.writeInt(tbUpdate.index | 0x10000);
                ds1.writeByte(tbUpdate.invAdd.length * 2);
                for (int i = 0; i < tbUpdate.invAdd.length; i++) {
                    ds1.writeByte(tbUpdate.invAdd[i]);
                    ds1.writeByte(tbUpdate.percentAdd[i]);
                }
                ds1.writeByte(tbUpdate.emptySlot);
                // Ngay het han
                int hanSD = tbUpdate.equipmentEntry.expirationDays - Until.getNumDay(tbUpdate.purchaseDate, new Date());
                if (hanSD < 0) {
                    hanSD = 0;
                }
                ds1.writeByte(hanSD);
            }
            if (addItem != null && !addItem.isEmpty()) {
                for (int i = 0; i < addItem.size(); i++) {
                    SpecialItemChestEntry spE = addItem.get(i);
                    if (spE.quantity > 100) {
                        SpecialItemChestEntry spE2 = new SpecialItemChestEntry();
                        spE2.item = spE.item;
                        spE2.quantity = (short) (spE.quantity - 100);
                        spE.quantity = 100;
                        addItem.add(spE2);
                    }
                    if (spE.quantity <= 0) {
                        continue;
                    }
                    nUpdate++;
                    // Kiem tra trong ruong co=>tang so luong. ko co=> tao moi
                    boolean isHave = false;
                    for (SpecialItemChestEntry spE1 : ruongDoItem) {
                        if (spE1.item.getId() == spE.item.getId()) {
                            isHave = true;
                            spE1.quantity += spE.quantity;
                            break;
                        }
                    }
                    // ko co=> Tao moi
                    if (!isHave) {
                        ruongDoItem.add(spE);
                    }
                    ds1.writeByte(spE.quantity > 1 ? 3 : 1);
                    ds1.writeByte(spE.item.getId());
                    if (spE.quantity > 1) {
                        ds1.writeByte(spE.quantity);
                    }
                    ds1.writeUTF(spE.item.getName());
                    ds1.writeUTF(spE.item.getDetail());
                }
            }
            if (removeItem != null && !removeItem.isEmpty()) {
                for (int k = 0; k < removeItem.size(); k++) {
                    SpecialItemChestEntry spE = removeItem.get(k);
                    if (spE.quantity > 100) {
                        SpecialItemChestEntry spE2 = new SpecialItemChestEntry();
                        spE2.item = spE.item;
                        spE2.quantity = (short) (spE.quantity - 100);
                        spE.quantity = 100;
                        removeItem.add(spE2);
                    }
                    if (spE.quantity <= 0) {
                        continue;
                    }
                    // Kiem tra trong ruong co=>giam so luong
                    for (int i = 0; i < ruongDoItem.size(); i++) {
                        SpecialItemChestEntry spE1 = ruongDoItem.get(i);
                        if (spE1.item.getId() == spE.item.getId()) {
                            if (spE1.quantity < spE.quantity) {
                                spE.quantity = spE1.quantity;
                            }
                            spE1.quantity -= spE.quantity;
                            if (spE1.quantity == 0) {
                                ruongDoItem.remove(i);
                            }
                            nUpdate++;
                            ds1.writeByte(0);
                            ds1.writeInt(spE.item.getId());
                            ds1.writeByte(spE.quantity);
                            break;
                        }
                    }
                }
            }
            if (removeTB >= 0 && removeTB < ruongDoTB.size() && ruongDoTB.get(removeTB) != null) {
                nUpdate++;
                ruongDoTB.set(removeTB, null);
                ds1.writeByte(0);
                ds1.writeInt(removeTB | 0x10000);
                ds1.writeByte(1);
            }
            ds1.flush();
            bas.flush();
            if (nUpdate == 0) {
                return;
            }
            ms = new Message(27);
            ds = ms.writer();
            ds.writeByte(nUpdate);
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
