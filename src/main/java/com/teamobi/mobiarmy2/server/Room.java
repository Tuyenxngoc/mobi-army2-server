package com.teamobi.mobiarmy2.server;

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

    public Room(byte id, byte type, byte i, byte maxFightWaits, int minXu, int maxXu, int minMap, int maxMap) {
        this.id = id;
        this.type = type;
        this.fightWaits = new FightWait[maxFightWaits];
        this.minXu = minXu;
        this.maxXu = maxXu;
        this.minMap = minMap;
        this.maxMap = maxMap;
        this.initFightWaits(type == 5 && i == 11, type == 5 && i == 12);
    }

    private void initFightWaits(boolean lienHoan, boolean fe) {
        for (byte i = 0; i < fightWaits.length; i++) {
            fightWaits[i] = new FightWait(this, type, i, (byte) 8, (byte) 8, (byte) 1, (byte) 0, lienHoan, fe);
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
