package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mission {
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