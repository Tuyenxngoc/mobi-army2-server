package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class RobotSpider extends Boss {

    public RobotSpider(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 13, "Robot Spider", x, y, (short) 42, (short) 42, maxHp, 8);
    }

    @Override
    public void turnAction() {

    }
}
