package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;
import java.util.List;

public class XpData {

    public static final class LevelXpRequired {
        public int level;
        public int xp;
    }

    public static final List<LevelXpRequired> xpList = new ArrayList<>();

    public static int getXpRequestLevel(int currentLever) {
        return xpList.get(currentLever).xp;
    }
}
