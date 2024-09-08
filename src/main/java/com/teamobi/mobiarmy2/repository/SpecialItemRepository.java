package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class SpecialItemRepository {
    public static final List<SpecialItemEntry> SPECIAL_ITEM_ENTRIES = new ArrayList<>();

    /**
     * Retrieves a special item entry by its ID.
     *
     * @param id the ID of the special item entry.
     * @return the special item entry with the given ID, or null if not found.
     */
    public static SpecialItemEntry getSpecialItemById(byte id) {
        return SPECIAL_ITEM_ENTRIES.stream()
                .filter(entry -> entry.getId() == id)
                .findFirst()
                .orElse(null);
    }
}
