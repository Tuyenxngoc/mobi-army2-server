package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class BalloonGun extends Boss {
    public BalloonGun(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 18, "Balloon Gun", x, y, (short) 21, (short) 20, maxHp, 1);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}