package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.model.LevelXpRequired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class UserXpManager {
    public static final List<LevelXpRequired> LEVEL_XP_REQUIRED_LIST = new ArrayList<>();

    public static int getRequiredXpLevel(int level) {
        if (level < 0 || level >= LEVEL_XP_REQUIRED_LIST.size()) {
            return GameConstants.MAX_XP;
        }

        return LEVEL_XP_REQUIRED_LIST.get(level).getXp();
    }

    public static int getLevelByXP(long xp) {
        int left = 0;
        int right = LEVEL_XP_REQUIRED_LIST.size() - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            LevelXpRequired levelXpRequired = LEVEL_XP_REQUIRED_LIST.get(mid);

            if (xp < levelXpRequired.getXp()) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return right >= 0 ? LEVEL_XP_REQUIRED_LIST.get(right).getLevel() : 1;
    }
}
