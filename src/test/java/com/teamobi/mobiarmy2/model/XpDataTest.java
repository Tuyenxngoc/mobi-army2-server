package com.teamobi.mobiarmy2.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XpDataTest {

    @BeforeEach
    void setUp() {
        // Set up test data
        XpData.xpList.clear(); // Clear existing data
        XpData.LevelXpRequired level1 = new XpData.LevelXpRequired();
        level1.level = 1;
        level1.xp = 100;
        XpData.LevelXpRequired level2 = new XpData.LevelXpRequired();
        level2.level = 2;
        level2.xp = 200;
        XpData.LevelXpRequired level3 = new XpData.LevelXpRequired();
        level3.level = 3;
        level3.xp = 300;
        XpData.xpList.add(level1);
        XpData.xpList.add(level2);
        XpData.xpList.add(level3);
    }

    @Test
    void getLevelByEXP() {
        assertEquals(2, XpData.getLevelByEXP(200));
    }
}