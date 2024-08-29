package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.fight.FightWait;
import lombok.Getter;

/**
 * @author tuyen
 */
@Getter
public class Room {
    private final byte index;
    private final byte type;
    private final int minXu;
    private final int maxXu;
    private final byte minMap;
    private final byte maxMap;
    private final byte[] mapCanSelected;
    private final boolean isContinuous;
    private final byte maxPlayerFight;
    private final byte maxElementFight;
    private final byte numPlayerInitRoom;
    private final byte iconType;
    private final FightWait[] fightWaits;

    public Room(byte index, byte type, int minXu, int maxXu, byte minMap, byte maxMap, byte[] mapCanSelected, boolean isContinuous, byte numArea, byte maxPlayerFight, byte maxElementFight, byte numPlayerInitRoom, byte iconType) {
        this.index = index;
        this.type = type;
        this.minXu = minXu;
        this.maxXu = maxXu;
        this.minMap = minMap;
        this.maxMap = maxMap;
        this.mapCanSelected = mapCanSelected;
        this.isContinuous = isContinuous;
        this.maxPlayerFight = maxPlayerFight;
        this.maxElementFight = maxElementFight;
        this.numPlayerInitRoom = numPlayerInitRoom;
        this.iconType = iconType;
        this.fightWaits = new FightWait[numArea];
        for (byte i = 0; i < numArea; i++) {
            fightWaits[i] = new FightWait(this, i);
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

    /**
     * This method calculates and returns the number of available fight wait slots in the room.
     *
     * @return int representing the number of available fight wait slots.
     */
    public int getFightWaitsAvailable() {
        int startedFightsCount = 0;
        for (FightWait fight : fightWaits) {
            if (fight.isStarted()) {
                startedFightsCount++;
            }
        }
        return fightWaits.length - startedFightsCount;
    }

    /**
     * This method returns the map ID based on the available map IDs for the room.
     * If there are multiple map IDs available, it returns the first one.
     * If there are no map IDs available, it returns the minimum map ID.
     *
     * @return byte representing the map ID
     */
    public byte getMapId() {
        if (mapCanSelected != null) {
            return mapCanSelected[0];
        }

        return minMap;
    }
}
