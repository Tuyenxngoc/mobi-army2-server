package com.teamobi.mobiarmy2.manager;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.model.LevelXpRequiredEntry;

import java.util.ArrayList;
import java.util.List;

public class ClanXpManager {
    public static final List<LevelXpRequiredEntry> LEVEL_XP_REQUIRED_ENTRIES = new ArrayList<>();

    public static int getRequiredXpLevel(int level) {
        if (level < 0 || level >= LEVEL_XP_REQUIRED_ENTRIES.size()) {
            return GameConstants.MAX_XP;
        }

        return LEVEL_XP_REQUIRED_ENTRIES.get(level).getXp();
    }

    public static int getLevelByXP(long xp) {
        int left = 0;
        int right = LEVEL_XP_REQUIRED_ENTRIES.size() - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            LevelXpRequiredEntry midEntry = LEVEL_XP_REQUIRED_ENTRIES.get(mid);

            if (xp < midEntry.getXp()) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return right >= 0 ? LEVEL_XP_REQUIRED_ENTRIES.get(right).getLevel() : 1;
    }
}
