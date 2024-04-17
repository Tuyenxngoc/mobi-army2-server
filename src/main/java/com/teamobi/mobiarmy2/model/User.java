package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import com.teamobi.mobiarmy2.service.IUserService;
import com.teamobi.mobiarmy2.service.Impl.UserService;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class User {

    public static NVData.EquipmentEntry[][] nvEquipDefault;

    private ISession session;
    private UserState state = UserState.WAITING;
    private int id;
    private byte nhanVat;
    private String username;
    private String password;
    private short clanId;
    private int xu;
    private int luong;
    private int danhVong;
    private boolean isLogged;
    private boolean isLock;
    private boolean isActive;
    private byte nvUsed;
    private int pointEvent;
    private LocalDateTime xpX2Time;
    private boolean[] nvStt;
    private int[] lever;
    private byte[] leverPercent;
    private int[] xp;
    private short[] point;
    private short[][] pointAdd;
    private byte[] items;
    private int[][] NvData;
    private int[] friends;
    private int[] mission;
    private byte[] missionLevel;

    private final List<ruongDoItemEntry> ruongDoItem = new ArrayList<>();
    private final List<ruongDoTBEntry> ruongDoTB = new ArrayList<>();
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
        this.userService.sendServerMessage(ss);
    }

    public void updateXu(int xuUp) {
    }
}
