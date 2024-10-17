package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class BalloonFanBack extends Boss {
    public BalloonFanBack(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 20, "Balloon Fan Back", x, y, (short) 10, (short) 19, maxHp, 1);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}