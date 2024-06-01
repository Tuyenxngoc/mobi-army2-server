package com.teamobi.mobiarmy2.model.entry.clan;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ClanInfo extends ClanEntry {
    private int exp;
    private int xpUpLevel;
    private String dateCreated;
    private List<ClanItem> items;
}