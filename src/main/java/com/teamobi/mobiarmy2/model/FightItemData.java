package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.item.FightItem;
import com.teamobi.mobiarmy2.util.Until;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class FightItemData {

    public static final List<FightItem> fightItems = new ArrayList<>();

    public static byte getRandomItem() {
        return (byte) (Until.nextInt(fightItems.size() - 2) + 2);
    }
}
