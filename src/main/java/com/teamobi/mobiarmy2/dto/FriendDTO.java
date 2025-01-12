package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FriendDTO {
    private int userId;
    private String name;
    private int xu;
    private byte activeCharacterId;
    private short clanId;
    private byte online;
    private byte level;
    private byte levelPt;
    private short[] data;
}
