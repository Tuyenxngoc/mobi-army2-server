package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.LevelXpRequiredEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpDataTest {

    @BeforeEach
    void setUp() {
        // Set up test data
        XpData.LEVEL_XP_REQUIRED_ENTRIES.clear(); // Clear existing data
        LevelXpRequiredEntry level1 = new LevelXpRequiredEntry();
        level1.setLevel((short) 1);
        level1.setXp(100);
        LevelXpRequiredEntry level2 = new LevelXpRequiredEntry();
        level1.setLevel((short) 2);
        level1.setXp(100);
        LevelXpRequiredEntry level3 = new LevelXpRequiredEntry();
        level1.setLevel((short) 3);
        level1.setXp(400);
        XpData.LEVEL_XP_REQUIRED_ENTRIES.add(level1);
        XpData.LEVEL_XP_REQUIRED_ENTRIES.add(level2);
        XpData.LEVEL_XP_REQUIRED_ENTRIES.add(level3);
    }

    @Test
    void getLevelByEXP() {
        assertEquals(2, XpData.getLevelByEXP(200));
    }
}