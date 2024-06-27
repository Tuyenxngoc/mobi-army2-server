package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.fight.FightWait;
import lombok.Getter;

/**
 * @author tuyen
 */
@Getter
public class Room {
    public byte id;
    public byte type;
    public int minXu;
    public int maxXu;
    public int minMap;
    public int maxMap;
    public final FightWait[] fightWaits;

    public Room(byte id, byte type, byte roomCount, byte maxFightWaits, int minXu, int maxXu, byte minMap, byte maxMap, byte initMapId) {
        this.id = id;
        this.type = type;
        this.fightWaits = new FightWait[maxFightWaits];
        this.minXu = minXu;
        this.maxXu = maxXu;
        this.minMap = minMap;
        this.maxMap = maxMap;
        this.initFightWaits(roomCount, initMapId);
    }

    private void initFightWaits(byte roomCount, byte initMapId) {
        if (type == 5) {
            initMapId = 30;
        }
        for (byte i = 0; i < fightWaits.length; i++) {
            fightWaits[i] = new FightWait(this, i, (byte) 8, (byte) 4, initMapId, (byte) 0, false, false);
        }
    }

    public int getStatus() {
        // 0: đỏ
        // 1: vàng
        // 2: xanh
        return 2;
    }

    public int getFightWaitsAvailable() {
        return 0;
    }
}
