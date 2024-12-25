package com.teamobi.mobiarmy2.manager;

import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class SpecialItemManager {
    private static final Map<Byte, SpecialItemEntry> SPECIAL_ITEM_ENTRIES = new HashMap<>();

    public static void addSpecialItem(SpecialItemEntry entry) {
        SPECIAL_ITEM_ENTRIES.put(entry.getId(), entry);
    }

    public static SpecialItemEntry getSpecialItemById(byte id) {
        return SPECIAL_ITEM_ENTRIES.get(id);
    }

    public static Map<Byte, SpecialItemEntry> getSpecialItems() {
        return SPECIAL_ITEM_ENTRIES;
    }
}
