package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.fight.FightWait;
import com.teamobi.mobiarmy2.util.Utils;

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
        byte maxPlayerInit = 0, map = 0;
        boolean isLH = false, isSieuBoss = false;

        if (type == 5 && ntype == 9) {
            this.slMap.add((byte) 31);
            isSieuBoss = true;
        } else {
            byte[] continuityType = new byte[]{5};
            byte[] continuityNumbers = new byte[]{8};
            for (int i = 0; i < continuityNumbers.length; i++) {
                if (type == continuityType[i] && ntype == continuityNumbers[i]) {
                    isLH = true;
                    break;
                }
            }
            if (!isLH) {
                byte[] slMapId = new byte[]{30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44};
                byte[] slMapType = new byte[]{5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
                byte[] slMapNumbers = new byte[]{0, 0, 1, 2, 3, 4, 5, 6, 7, 7, 10, 11, 12, 13, 14};
                for (int i = 0; i < slMapId.length; i++) {
                    if (type != slMapType[i] || slMapNumbers[i] != ntype) {
                        continue;
                    }
                    this.slMap.add(slMapId[i]);
                }
            }
        }

        IServerConfig config = ServerManager.getInstance().config();

        switch (type) {
            case 0 -> {
                this.minXu = config.getMIN_XU_SO_CAP();
                this.maxXu = config.getMAX_XU_SO_CAP();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 1 -> {
                this.minXu = config.getMIN_XU_TRUNG_CAP();
                this.maxXu = config.getMAX_XU_TRUNG_CAP();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 2 -> {
                this.minXu = config.getMIN_XU_CAO_CAP();
                this.maxXu = config.getMAX_XU_CAO_CAP();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 3 -> {
                this.minXu = config.getMIN_XU_DAU_TRUONG();
                this.maxXu = config.getMAX_XU_DAU_TRUONG();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 4 -> {
                this.minXu = config.getMIN_XU_TU_DO();
                this.maxXu = config.getMAX_XU_TU_DO();
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = config.getnPlayersInitRoom();
                map = config.getInitMap();
            }
            case 5 -> {
                this.minXu = config.getMIN_XU_BOSS();
                this.maxXu = config.getMAX_XU_BOSS();
                this.minMap = 30;
                this.maxMap = 39;
                maxPlayerInit = config.getMaxPlayers();
                map = config.getInitMapBoss();
            }
            case 6 -> {
                this.minXu = config.getMIN_XU_CLAN();
                this.maxXu = config.getMAX_XU_CLAN();
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
            this.entrys[i] = new FightWait(this, this.type, (byte) i, config.getMaxPlayers(), maxPlayerInit, map, (byte) Utils.nextInt(0, 2), isLH, isSieuBoss);
        }
    }

    public int getFully() {
        int maxPlayers = 0;
        int player = 0;
        synchronized (entrys) {
            for (FightWait fw : entrys) {
                maxPlayers += fw.maxSetPlayer;
                if (fw.started) {
                    player += fw.maxSetPlayer;
                } else {
                    player += fw.numPlayer;
                }
            }
            int perCent = (player * 100) / maxPlayers;
            return (perCent < 50) ? 2 : ((perCent < 75) ? 1 : 0);
        }
    }
}
