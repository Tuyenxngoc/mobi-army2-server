package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightWait;
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

    public static NVData.EquipmentEntry[][] nvEquipDefault;
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
    public ruongDoTBEntry[][] nvEquip;
    public List<Integer> friends;
    public List<ruongDoItemEntry> ruongDoItem;
    public List<ruongDoTBEntry> ruongDoTB;
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
        if (this.nvEquip[getNvUsed()][5] != null && this.nvEquip[getNvUsed()][5].entry.isSet) {
            equip[0] = this.nvEquip[getNvUsed()][5].entry.arraySet[0];
            equip[1] = this.nvEquip[getNvUsed()][5].entry.arraySet[1];
            equip[2] = this.nvEquip[getNvUsed()][5].entry.arraySet[2];
            equip[3] = this.nvEquip[getNvUsed()][5].entry.arraySet[3];
            equip[4] = this.nvEquip[getNvUsed()][5].entry.arraySet[4];
        } else {
            for (int i = 0; i < 5; i++) {
                if (this.nvEquip[getNvUsed()][i] != null && !this.nvEquip[getNvUsed()][i].entry.isSet) {
                    equip[i] = this.nvEquip[getNvUsed()][i].entry.id;
                } else if (nvEquipDefault[getNvUsed()][i] != null) {
                    equip[i] = nvEquipDefault[getNvUsed()][i].id;
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
        for (int i = 0; i < ruongDoItem.size(); i++) {
            ruongDoItemEntry spE1 = ruongDoItem.get(i);
            if (spE1.entry.id == id) {
                return spE1.numb;
            }
        }
        return 0;
    }

    public ruongDoTBEntry getEquipNoNgoc(NVData.EquipmentEntry eqE, byte level) {
        for (int i = 0; i < ruongDoTB.size(); i++) {
            ruongDoTBEntry rdE = ruongDoTB.get(i);
            if (rdE != null && rdE.entry == eqE && !rdE.isUse && rdE.vipLevel == level && rdE.slotNull == 3 && rdE.entry.hanSD - Until.getNumDay(rdE.dayBuy, new Date()) > 0) {
                return rdE;
            }
        }
        return null;
    }

    public synchronized void updateRuong(ruongDoTBEntry tbUpdate, ruongDoTBEntry addTB, int removeTB, ArrayList<ruongDoItemEntry> addItem, ArrayList<ruongDoItemEntry> removeItem) {
        try {
            Message ms;
            DataOutputStream ds;
            if (addTB != null) {
                int bestLocation = -1;
                for (int i = 0; i < ruongDoTB.size(); i++) {
                    ruongDoTBEntry ruongDoTBEntry = ruongDoTB.get(i);
                    if (ruongDoTBEntry == null) {
                        bestLocation = i;
                        break;
                    }
                }
                addTB.dayBuy = new Date();
                addTB.isUse = false;
                if (addTB.invAdd == null) {
                    addTB.invAdd = new short[addTB.entry.invAdd.length];
                    for (int j = 0; j < addTB.entry.invAdd.length; j++) {
                        addTB.invAdd[j] = addTB.entry.invAdd[j];
                    }
                }
                if (addTB.percentAdd == null) {
                    addTB.percentAdd = new short[addTB.entry.percenAdd.length];
                    for (int j = 0; j < addTB.entry.percenAdd.length; j++) {
                        addTB.percentAdd[j] = addTB.entry.percenAdd[j];
                    }
                }
                addTB.slotNull = 3;
                addTB.cap = addTB.entry.cap;
                addTB.slot = new int[3];
                for (int i = 0; i < 3; i++) {
                    addTB.slot[i] = -1;
                }
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
                ds.writeByte(addTB.entry.idNV);
                ds.writeByte(addTB.entry.idEquipDat);
                ds.writeShort(addTB.entry.id);
                ds.writeUTF(addTB.entry.name);
                ds.writeByte(addTB.invAdd.length * 2);
                for (int i = 0; i < addTB.invAdd.length; i++) {
                    ds.writeByte(addTB.invAdd[i]);
                    ds.writeByte(addTB.percentAdd[i]);
                }
                ds.writeByte(addTB.entry.hanSD);
                ds.writeByte(addTB.entry.isSet ? 1 : 0);
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
                ds1.writeByte(tbUpdate.slotNull);
                // Ngay het han
                int hanSD = tbUpdate.entry.hanSD - Until.getNumDay(tbUpdate.dayBuy, new Date());
                if (hanSD < 0) {
                    hanSD = 0;
                }
                ds1.writeByte(hanSD);
            }
            if (addItem != null && !addItem.isEmpty()) {
                for (int i = 0; i < addItem.size(); i++) {
                    ruongDoItemEntry spE = addItem.get(i);
                    if (spE.numb > 100) {
                        ruongDoItemEntry spE2 = new ruongDoItemEntry();
                        spE2.entry = spE.entry;
                        spE2.numb = spE.numb - 100;
                        spE.numb = 100;
                        addItem.add(spE2);
                    }
                    if (spE.numb <= 0) {
                        continue;
                    }
                    nUpdate++;
                    // Kiem tra trong ruong co=>tang so luong. ko co=> tao moi
                    boolean isHave = false;
                    for (ruongDoItemEntry spE1 : ruongDoItem) {
                        if (spE1.entry.id == spE.entry.id) {
                            isHave = true;
                            spE1.numb += spE.numb;
                            break;
                        }
                    }
                    // ko co=> Tao moi
                    if (!isHave) {
                        ruongDoItem.add(spE);
                    }
                    ds1.writeByte(spE.numb > 1 ? 3 : 1);
                    ds1.writeByte(spE.entry.id);
                    if (spE.numb > 1) {
                        ds1.writeByte(spE.numb);
                    }
                    ds1.writeUTF(spE.entry.name);
                    ds1.writeUTF(spE.entry.detail);
                }
            }
            if (removeItem != null && !removeItem.isEmpty()) {
                for (int k = 0; k < removeItem.size(); k++) {
                    ruongDoItemEntry spE = removeItem.get(k);
                    if (spE.numb > 100) {
                        ruongDoItemEntry spE2 = new ruongDoItemEntry();
                        spE2.entry = spE.entry;
                        spE2.numb = spE.numb - 100;
                        spE.numb = 100;
                        removeItem.add(spE2);
                    }
                    if (spE.numb <= 0) {
                        continue;
                    }
                    // Kiem tra trong ruong co=>giam so luong
                    for (int i = 0; i < ruongDoItem.size(); i++) {
                        ruongDoItemEntry spE1 = ruongDoItem.get(i);
                        if (spE1.entry.id == spE.entry.id) {
                            if (spE1.numb < spE.numb) {
                                spE.numb = spE1.numb;
                            }
                            spE1.numb -= spE.numb;
                            if (spE1.numb == 0) {
                                ruongDoItem.remove(i);
                            }
                            nUpdate++;
                            ds1.writeByte(0);
                            ds1.writeInt(spE.entry.id);
                            ds1.writeByte(spE.numb);
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
