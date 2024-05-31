package com.teamobi.mobiarmy2.model.user;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class PlayerLeaderboardEntry {
    private int playerId;
    private String username;
    private short clanId;
    private byte nvUsed;
    private byte level;
    private byte levelPt;
    private byte index;
    private short[] data;
    private String detail;
}