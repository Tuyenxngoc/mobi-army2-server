package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.FabricateItemEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;

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

        // Iterate through the list of fabricate item entries to find a match
        for (FabricateItemEntry fabricateItemEntry : FABRICATE_ITEM_ENTRIES) {
            if (fabricateItemEntry.getItemRequire().size() == entrySet.size() &&
                    fabricateItemEntry.getItemRequire().equals(entrySet)) {
                return fabricateItemEntry;
            }
        }
        return null;
    }

}
