package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
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
    public int id;
    public byte nhanVat;
    public String username;
    public String password;
    public short clanId;
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
    public int[] lever;
    public byte[] leverPercent;
    public int[] xp;
    public int[] point;
    public int[][] pointAdd;
    public byte[] items;
    public int[][] NvData;
    public int[] friends;
    public int[] mission;
    public byte[] missionLevel;
    public ruongDoTBEntry[][] nvEquip;

    public List<ruongDoItemEntry> ruongDoItem;
    public List<ruongDoTBEntry> ruongDoTB;
    private FightWait fightWait;

    private final IUserService userService;

    public User() {
        this.userService = new UserService(this);
    }

    public User(ISession session) {
        this();
        this.session = session;
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }

    public void sendServerMessage(String ss) {
        userService.sendServerMessage(ss);
    }


    public void logout() {
        isLogged = false;
    }

    public int getLever(byte nv) {
        return lever[nv];
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
}
