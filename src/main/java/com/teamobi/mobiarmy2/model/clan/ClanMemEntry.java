package com.teamobi.mobiarmy2.model.clan;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanMemEntry {
    int playerId;
    String username;
    int point;
    byte nvUsed;
    byte online;
    byte lever;
    byte levelPt;
    byte index;
    int cup;
    short[] dataEquip;
    String contribute_text;
    String contribute_count;
}