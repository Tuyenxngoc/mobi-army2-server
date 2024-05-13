package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class XpData {

    public static final class LevelXpRequired {
        public int level;
        public int xp;
    }

    public static final List<LevelXpRequired> xpList = new ArrayList<>();

    public static int getXpRequestLevel(int currentLevel) {
        return xpList.get(currentLevel).xp;
    }

    public static int getLevelByEXP(long exp) {
        for (LevelXpRequired levelXpRequired : xpList) {
            if (exp < levelXpRequired.xp) {
                return levelXpRequired.level - 1;
            }
        }
        return -1;
    }
}
