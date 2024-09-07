package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class PlayerCharacterEntry {
    private long id;
    private short[] additionalPoints;
    private int[] data;
    private int level;
    private int points;
    private int xp;
    private byte characterId;
    private long playerId;
}
