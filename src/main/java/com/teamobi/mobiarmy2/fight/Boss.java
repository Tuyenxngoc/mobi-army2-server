package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.server.ServerManager;

import java.util.ArrayList;

public abstract class Boss extends Player {

    public String name;
    public ArrayList<Player> list = new ArrayList<>();

    public Boss(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) {
        super(fightMNG, location, X, Y, null, (byte) 0, null);
        this.idNV = idGun;
        this.HPMax = HPMax;
        this.HP = this.HPMax;
        this.name = name;
        this.satThuong = 100;
        this.phongThu = 0;
        this.mayMan = 0;
        for (int i = 0; i < ServerManager.getInstance().config().getMaxPlayerFight(); i++) {
            Player pl = this.fightMNG.players[i];
            if (pl != null) {
                list.add(pl);
            }
        }
    }

    protected abstract void turnAction();

}
