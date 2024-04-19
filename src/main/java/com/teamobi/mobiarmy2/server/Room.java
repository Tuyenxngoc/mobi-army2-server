package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.util.Until;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class Room {
    
    public byte id;
    public byte type;
    public String name;
    public int maxXu;
    public int minXu;
    public int minMap;
    public int maxMap;
    public final FightWait[] entrys;
    public int nType;
    public ArrayList<Byte> slMap;

    public Room(int id, int type, int maxEntrys, int ntype) {
        this.id = (byte) id;
        this.type = (byte) type;
        this.nType = ntype;
        this.slMap = new ArrayList<>();
        byte maxPlayerInit = 0;
        byte map = 0;
        boolean isLH = false;

        IServerConfig config = ServerManager.getInstance().config();
        switch (type) {
            case 0 -> {
                this.minXu = config.getMin_xu_so_cap();
                this.maxXu = config.getMax_xu_so_cap();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 1 -> {
                this.minXu = config.getMin_xu_trung_cap();
                this.maxXu = config.getMax_xu_trung_cap();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 2 -> {
                this.minXu = config.getMin_xu_cao_cap();
                this.maxXu = config.getMax_xu_cao_cap();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 3 -> {
                this.minXu = config.getMin_xu_dau_truong();
                this.maxXu = config.getMax_xu_dau_truong();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 4 -> {
                this.minXu = config.getMin_xu_tu_do();
                this.maxXu = config.getMax_xu_tu_do();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 5 -> {
                this.minXu = config.getMin_xu_boss();
                this.maxXu = config.getMax_xu_boss();
                this.minMap = 30;
                this.maxMap = 39;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 6 -> {
                this.minXu = config.getMin_xu_clan();
                this.maxXu = config.getMax_xu_clan();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
        }
        if (this.slMap.size() > 0) {
            map = this.slMap.get(0);
        }
        this.entrys = new FightWait[maxEntrys];
        for (int i = 0; i < maxEntrys; i++) {
            this.entrys[i] = new FightWait(this, this.type, (byte) i, config.getMaxPlayers(), maxPlayerInit, map, (byte) Until.nextInt(0, 2), isLH);
        }
    }

}
