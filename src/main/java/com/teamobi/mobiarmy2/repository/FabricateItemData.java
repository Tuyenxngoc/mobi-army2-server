package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.model.FabricateItemEntry;
import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tuyen
 */
public class FabricateItemData {
    public static final List<FabricateItemEntry> FABRICATE_ITEM_ENTRIES = new ArrayList<>();

    /**
     * Gets a FabricateItemEntry that matches the required special items.
     *
     * @param selectedSpecialItems The list of selected special items.
     * @return The FabricateItemEntry if found, otherwise null.
     */
    public static FabricateItemEntry getFabricateItem(List<SpecialItemChestEntry> selectedSpecialItems) {
        // Convert the list to a set to eliminate duplicates and for faster comparison
        Set<SpecialItemChestEntry> entrySet = new HashSet<>(selectedSpecialItems);
        return FABRICATE_ITEM_ENTRIES.stream()
                .filter(entry -> entry.getItemRequire().equals(entrySet))
                .findFirst()
                .orElse(null);
    }
}
