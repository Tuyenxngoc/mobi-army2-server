package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class Ghost2 extends Boss {

    public Ghost2(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 26, "Ghost II", x, y, (short) 35, (short) 31, maxHp, 8);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}