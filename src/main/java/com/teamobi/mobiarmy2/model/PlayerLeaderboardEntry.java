package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

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