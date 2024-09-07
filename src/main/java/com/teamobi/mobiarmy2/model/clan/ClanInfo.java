package com.teamobi.mobiarmy2.model.clan;

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
    private String createdDate;
    private List<ClanItem> items;
}