package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.common.util.Utils;
import com.teamobi.mobiarmy2.entity.FightItem;

import java.util.ArrayList;
import java.util.List;

public class FightItemManager {
    public static final List<FightItem> FIGHT_ITEMS = new ArrayList<>();

    public static byte getRandomItem() {
        return (byte) (Utils.nextInt(FIGHT_ITEMS.size() - 2) + 2);
    }
}
