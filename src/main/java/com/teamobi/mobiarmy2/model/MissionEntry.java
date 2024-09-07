package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class MissionEntry {
    private byte id;
    private byte type;
    private byte level;
    private String name;
    private int requirement;
    private String reward;
    private int rewardXu;
    private int rewardLuong;
    private int rewardXp;
    private int rewardCup;
}