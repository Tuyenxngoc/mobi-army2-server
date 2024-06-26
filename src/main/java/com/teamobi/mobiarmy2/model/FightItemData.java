package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.item.FightItemEntry;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class FightItemData {
    public static final List<FightItemEntry> FIGHT_ITEM_ENTRIES = new ArrayList<>();

    public static byte getRandomItem() {
        return (byte) (Utils.nextInt(FIGHT_ITEM_ENTRIES.size() - 2) + 2);
    }
}
