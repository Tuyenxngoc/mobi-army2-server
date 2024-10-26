package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class BalloonEye extends Boss {
    public BalloonEye(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 21, "Balloon Eye", x, y, (short) 13, (short) 14, maxHp, 8);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonEye!");

    }
}
