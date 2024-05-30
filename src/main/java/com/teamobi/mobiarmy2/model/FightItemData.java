package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.item.FightItemEntry;
import com.teamobi.mobiarmy2.util.Until;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class FightItemData {

    public static final List<FightItemEntry> FIGHT_ITEM_ENTRIES = new ArrayList<>();

    public static byte getRandomItem() {
        return (byte) (Until.nextInt(FIGHT_ITEM_ENTRIES.size() - 2) + 2);
    }
}
