package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class UserLeaderboardDTO {
    private int userId;
    private String username;
    private short clanId;
    private byte activeCharacter;
    private byte level;
    private byte levelPt;
    private byte index;
    private short[] data;
    private String detail;
}