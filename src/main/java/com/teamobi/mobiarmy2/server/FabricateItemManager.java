package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.FabricateItem;
import com.teamobi.mobiarmy2.model.SpecialItemChest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tuyen
 */
public class FabricateItemManager {
    public static final List<FabricateItem> FABRICATE_ITEMS = new ArrayList<>();

    /**
     * Gets a FabricateItemEntry that matches the required special items.
     *
     * @param selectedSpecialItems The list of selected special items.
     * @return The FabricateItemEntry if found, otherwise null.
     */
    public static FabricateItem getFabricateItem(List<SpecialItemChest> selectedSpecialItems) {
        //Convert the list to a set to eliminate duplicates and for faster comparison
        Set<SpecialItemChest> entrySet = new HashSet<>(selectedSpecialItems);
        return FABRICATE_ITEMS.stream()
                .filter(entry -> entry.getItemRequire().equals(entrySet))
                .findFirst()
                .orElse(null);
    }
}
