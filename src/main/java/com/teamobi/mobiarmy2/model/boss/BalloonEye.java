package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class BalloonEye extends Boss {
    public BalloonEye(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 21, "Balloon Eye", x, y, (short) 13, (short) 14, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonEye!");

    }
}
