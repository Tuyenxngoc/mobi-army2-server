package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.model.LevelXpRequiredEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class XpRepository {
    public static final List<LevelXpRequiredEntry> LEVEL_XP_REQUIRED_ENTRIES = new ArrayList<>();

    public static int getRequiredXpLevel(int level) {
        if (level >= LEVEL_XP_REQUIRED_ENTRIES.size()) {
            return CommonConstant.MAX_XP;
        }

        return LEVEL_XP_REQUIRED_ENTRIES.get(level).getXp();
    }

    public static int getLevelByXP(long xp) {
        for (LevelXpRequiredEntry levelXpRequiredEntry : LEVEL_XP_REQUIRED_ENTRIES) {
            if (xp < levelXpRequiredEntry.getXp()) {
                return levelXpRequiredEntry.getLevel() - 1;
            }
        }
        return LEVEL_XP_REQUIRED_ENTRIES.get(LEVEL_XP_REQUIRED_ENTRIES.size() - 1).getLevel();
    }
}
