package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCharacterDTO {
    private long userId;
    private byte characterId;
    private short[] additionalPoints;
    private int[] data;
    private int level;
    private int points;
    private int xp;
    private boolean isActive;
}
