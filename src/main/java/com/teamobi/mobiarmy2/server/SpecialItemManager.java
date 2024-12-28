package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.SpecialItem;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class SpecialItemManager {
    private static final Map<Byte, SpecialItem> SPECIAL_ITEMS = new HashMap<>();

    public static void addSpecialItem(SpecialItem specialItem) {
        SPECIAL_ITEMS.put(specialItem.getId(), specialItem);
    }

    public static SpecialItem getSpecialItemById(byte id) {
        return SPECIAL_ITEMS.get(id);
    }

    public static Map<Byte, SpecialItem> getSpecialItems() {
        return SPECIAL_ITEMS;
    }
}
