package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.repository.XpRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpRepositoryTest {

    @BeforeAll
    static void setUp() {
        // Khởi tạo dữ liệu cho LEVEL_XP_REQUIRED_ENTRIES
        XpRepository.LEVEL_XP_REQUIRED_ENTRIES.clear();
        XpRepository.LEVEL_XP_REQUIRED_ENTRIES.addAll(Arrays.asList(
                new LevelXpRequiredEntry((short) 1, 100),
                new LevelXpRequiredEntry((short) 2, 300),
                new LevelXpRequiredEntry((short) 3, 600),
                new LevelXpRequiredEntry((short) 4, 1000)
        ));
    }

    @Test
    void testGetXpRequestLevel() {
        assertEquals(100, XpRepository.getXpRequestLevel(0), "Xp for level 1 should be 100");
        assertEquals(300, XpRepository.getXpRequestLevel(1), "Xp for level 2 should be 300");
        assertEquals(600, XpRepository.getXpRequestLevel(2), "Xp for level 3 should be 600");
        assertEquals(1000, XpRepository.getXpRequestLevel(3), "Xp for level 4 should be 1000");
    }

    @Test
    void testGetLevelByEXP() {
        assertEquals(0, XpRepository.getLevelByEXP(50), "Exp 50 should correspond to level 0");
        assertEquals(1, XpRepository.getLevelByEXP(150), "Exp 150 should correspond to level 1");
        assertEquals(2, XpRepository.getLevelByEXP(350), "Exp 350 should correspond to level 2");
        assertEquals(3, XpRepository.getLevelByEXP(700), "Exp 700 should correspond to level 3");
        assertEquals(-1, XpRepository.getLevelByEXP(1200), "Exp 1200 should correspond to an undefined level");
    }
}