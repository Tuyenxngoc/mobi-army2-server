package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.ClanItem;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class ClanItemManager {
    public static final Map<Byte, ClanItem> CLAN_ITEM_MAP = new HashMap<>();

    public static ClanItem getItemClanById(byte id) {
        return CLAN_ITEM_MAP.get(id);
    }
}
