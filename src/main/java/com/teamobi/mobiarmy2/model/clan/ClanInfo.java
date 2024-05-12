package com.teamobi.mobiarmy2.model.clan;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClanInfo extends ClanEntry {
    private int exp;
    private int xpUpLevel;
    private String dateCreated;
    private List<ClanItem> items;
}