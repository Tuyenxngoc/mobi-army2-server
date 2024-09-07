package com.teamobi.mobiarmy2.model.clan;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanMemEntry {
    private int playerId;
    private String username;
    private int point;
    private byte activeCharacter;
    private byte online;
    private byte lever;
    private byte levelPt;
    private byte index;
    private int cup;
    private short[] dataEquip;
    private String contributeText;
    private String contributeCount;
}