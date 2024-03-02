package com.teamobi.mobiarmy2.army2.server;

import com.teamobi.mobiarmy2.army2.fight.FightWait;

import java.util.ArrayList;

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

    public ArrayList<Byte> slMap = new ArrayList<>();

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
        switch (type) {
            case 0:
                this.minXu = ServerManager.MIN_XU_SO_CAP;
                this.maxXu = ServerManager.MAX_XU_SO_CAP;
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = ServerManager.nPlayersInitRoom;
                map = ServerManager.initMap;
                break;

            case 1:
                this.minXu = ServerManager.MIN_XU_TRUNG_CAP;
                this.maxXu = ServerManager.MAX_XU_TRUNG_CAP;
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = ServerManager.nPlayersInitRoom;
                map = ServerManager.initMap;
                break;

            case 2:
                this.minXu = ServerManager.MIN_XU_CAO_CAP;
                this.maxXu = ServerManager.MAX_XU_CAO_CAP;
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = ServerManager.nPlayersInitRoom;
                map = ServerManager.initMap;
                break;

            case 3:
                this.minXu = ServerManager.MIN_XU_DAU_TRUONG;
                this.maxXu = ServerManager.MAX_XU_DAU_TRUONG;
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = ServerManager.nPlayersInitRoom;
                map = ServerManager.initMap;
                break;

            case 4:
                this.minXu = ServerManager.MIN_XU_TU_DO;
                this.maxXu = ServerManager.MAX_XU_TU_DO;
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = ServerManager.nPlayersInitRoom;
                map = ServerManager.initMap;
                break;

            case 5:
                this.minXu = ServerManager.MIN_XU_BOSS;
                this.maxXu = ServerManager.MAX_XU_BOSS;
                this.minMap = 30;
                this.maxMap = 39;
                maxPlayerInit = ServerManager.maxPlayers;
                map = ServerManager.initMapBoss;
                break;

            case 6:
                //this.maxXu = Integer.MAX_VALUE;
                this.minXu = ServerManager.MIN_XU_CLAN;
                this.maxXu = ServerManager.MAX_XU_CLAN;
                this.minMap = 0;
                this.maxMap = 29;
                maxPlayerInit = ServerManager.nPlayersInitRoom;
                map = ServerManager.initMap;
                break;
        }
        if (this.slMap.size() > 0) {
            map = this.slMap.get(0);
        }
        this.entrys = new FightWait[maxEntrys];
        for (int i = 0; i < maxEntrys; i++) {
            this.entrys[i] = new FightWait(this, this.type, (byte) i, ServerManager.maxPlayers, maxPlayerInit, map, (byte) Until.nextInt(0, 2), isLH, isSieuBoss);
        }
    }

    protected int getFully() {
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
