package com.teamobi.mobiarmy2.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanDTO {
    private short id;
    private String name;
    private byte memberCount;
    private byte maxMemberCount;
    private String masterName;
    private int xu;
    private int luong;
    private int cup;
    private byte level;
    private byte levelPercentage;
    private String description;
}