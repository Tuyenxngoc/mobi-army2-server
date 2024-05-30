package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.item.ClanItemEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class ItemClanData {

    public static final Map<Byte, ClanItemEntry> clanItemsMap = new HashMap<>();

    public static ClanItemEntry getItemClanById(byte id) {
        return clanItemsMap.get(id);
    }

}
