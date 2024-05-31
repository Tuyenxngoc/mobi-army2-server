package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.item.ClanItemEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class ItemClanData {

    public static final Map<Byte, ClanItemEntry> CLAN_ITEM_ENTRY_MAP = new HashMap<>();

    public static ClanItemEntry getItemClanById(byte id) {
        return CLAN_ITEM_ENTRY_MAP.get(id);
    }

}
