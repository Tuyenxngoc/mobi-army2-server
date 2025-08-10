package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.entity.SpecialItem;

import java.util.HashMap;
import java.util.Map;

public class SpecialItemManager {
    public static final Map<Byte, SpecialItem> SPECIAL_ITEMS = new HashMap<>();

    public static void addSpecialItem(SpecialItem specialItem) {
        SPECIAL_ITEMS.put(specialItem.getId(), specialItem);
    }

    public static SpecialItem getSpecialItemById(byte id) {
        return SPECIAL_ITEMS.get(id);
    }
}
