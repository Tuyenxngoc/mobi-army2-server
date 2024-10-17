package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class Balloon extends Boss {
    public Balloon(IFightManager fightManager, byte index, short x, short y) {
        super(fightManager, index, (byte) 17, "Balloon", x, y, (short) 0, (short) 0, (short) 0, 1);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}
