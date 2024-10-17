package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class Robot extends Boss {
    public Robot(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 14, "Robot", x, y, (short) 24, (short) 25, maxHp, 8);
    }

    @Override
    public void turnAction() {

    }
}