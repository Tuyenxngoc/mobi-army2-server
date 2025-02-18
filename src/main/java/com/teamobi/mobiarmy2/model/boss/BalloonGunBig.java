package com.teamobi.mobiarmy2.model.boss;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.model.Boss;

/**
 * @author tuyen
 */
public class BalloonGunBig extends Boss {
    public BalloonGunBig(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 19, "Balloon Gun Big", x, y, (short) 35, (short) 39, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {
        throw new UnsupportedOperationException("Cannot call nextTurn from BalloonGunBig!");
    }
}