package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.model.LevelXpRequiredEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class XpRepository {
    public static final List<LevelXpRequiredEntry> LEVEL_XP_REQUIRED_ENTRIES = new ArrayList<>();

    public static int getXpRequestLevel(int currentLevel) {
        return LEVEL_XP_REQUIRED_ENTRIES.get(currentLevel).getXp();
    }

    public static int getLevelByEXP(long exp) {
        for (LevelXpRequiredEntry levelXpRequiredEntry : LEVEL_XP_REQUIRED_ENTRIES) {
            if (exp < levelXpRequiredEntry.getXp()) {
                return levelXpRequiredEntry.getLevel() - 1;
            }
        }
        return LEVEL_XP_REQUIRED_ENTRIES.get(LEVEL_XP_REQUIRED_ENTRIES.size() - 1).getLevel();
    }
}
