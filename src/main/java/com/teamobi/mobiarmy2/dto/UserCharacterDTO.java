package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class UserCharacterDTO {
    private long userCharacterId;
    private short[] additionalPoints;
    private int[] data;
    private int level;
    private int points;
    private int xp;
    private byte characterId;
    private int userId;
}
