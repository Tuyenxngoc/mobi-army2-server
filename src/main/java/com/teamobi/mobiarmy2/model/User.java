package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.constant.UserState;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.network.ISession;
import com.teamobi.mobiarmy2.network.Impl.Message;
import lombok.Getter;
import lombok.Setter;

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

    private FightWait fightWait;

    public User() {
    }

    public User(ISession session) {
        this.session = session;
    }

    public boolean isNotWaiting() {
        return !state.equals(UserState.WAITING);
    }

    public void sendMessage(Message ms) {
        session.sendMessage(ms);
    }
}
