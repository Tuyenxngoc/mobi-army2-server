package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.fight.FightWait;
import lombok.Getter;

/**
 * @author tuyen
 */
@Getter
public class Room {
    private byte index;
    private byte type;
    private int minXu;
    private int maxXu;
    private byte minMap;
    private byte maxMap;
    private final byte[] mapCanSelected;
    boolean isContinuous;
    boolean isSelectable;
    byte maxPlayerFight;
    byte numPlayerInitRoom;
    private final FightWait[] fightWaits;

    public Room(byte index, byte type, int minXu, int maxXu, byte minMap,
                byte maxMap, byte[] mapCanSelected, boolean isContinuous, boolean isSelectable, byte numArea,
                byte maxPlayerFight, byte numPlayerInitRoom) {
        this.index = index;
        this.type = type;
        this.minXu = minXu;
        this.maxXu = maxXu;
        this.minMap = minMap;
        this.maxMap = maxMap;
        this.mapCanSelected = mapCanSelected;
        this.isContinuous = isContinuous;
        this.isSelectable = isSelectable;
        this.maxPlayerFight = maxPlayerFight;
        this.numPlayerInitRoom = numPlayerInitRoom;
        this.fightWaits = new FightWait[numArea];
        for (byte i = 0; i < numArea; i++) {
            fightWaits[i] = new FightWait();
        }
    }

    /**
     * This method returns the status based on the percentage of started fights.
     * <p>
     * Status codes:
     * 0: đỏ (red)
     * 1: vàng (yellow)
     * 2: xanh (green)
     *
     * @return int representing the status code
     */
    public int getStatus() {
        int startedFightsCount = 0;
        int totalFights = fightWaits.length;

        for (FightWait fight : fightWaits) {
            if (fight.isStarted()) {
                startedFightsCount++;
            }
        }

        int percentage = Math.round((startedFightsCount / (float) totalFights) * 100);
        return (percentage < 50) ? 2 : ((percentage < 75) ? 1 : 0);
    }

    public int getFightWaitsAvailable() {
        int startedFightsCount = 0;
        for (FightWait fight : fightWaits) {
            if (fight.isStarted()) {
                startedFightsCount++;
            }
        }
        return fightWaits.length - startedFightsCount;
    }

}
