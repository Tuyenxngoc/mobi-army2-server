package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class UFO extends Boss {

    public UFO(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 16, "UFO", x, y, (short) 51, (short) 46, maxHp, 8);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}
