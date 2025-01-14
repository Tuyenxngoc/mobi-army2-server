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

    public static FabricateItem getFabricateItem(List<SpecialItemChest> selectedSpecialItems) {
        Set<SpecialItemChest> specialItemChests = new HashSet<>(selectedSpecialItems);
        return FABRICATE_ITEMS.stream()
                .filter(fabricateItem -> fabricateItem.getItemRequire().equals(specialItemChests))
                .findFirst()
                .orElse(null);
    }
}
