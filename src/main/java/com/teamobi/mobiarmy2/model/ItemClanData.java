package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.item.ClanItemDetail;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class ItemClanData {

    public static final Map<Byte, ClanItemDetail> clanItemsMap = new HashMap<>();

    public static ClanItemDetail getItemClanById(byte id) {
        return clanItemsMap.get(id);
    }

}
