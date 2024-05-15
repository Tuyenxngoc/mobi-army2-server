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

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    public boolean[] nvStt;
    public int[] levels;
    public byte[] levelPercents;
    public int[] xps;
    public int[] points;
    public int[][] pointAdd;
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

    public short getIDBullet() {
        return 0;
    }

    public short getGunId() {
        return 0;
    }

    public int[] getAbility() {
        return new int[0];
    }

    public void notifyNetWait() {

    }

    public void netWait() {

    }

    public void updateMission(int i, int i1) {

    }

    public void updateSpecialItem(int i, int soluong) {

    }

    public void updateItem(byte b, int i) {
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

    public void updateItems(byte itemIndex, byte quantity) {
        this.items[itemIndex] += quantity;
        if (this.items[itemIndex] < 0) {
            this.items[itemIndex] = 0;
        }
        if (this.items[itemIndex] > ServerManager.getInstance().config().getMax_item()) {
            this.items[itemIndex] = (byte) ServerManager.getInstance().config().getMax_item();
        }
        this.items[0] = this.items[1] = 99;
    }
}
