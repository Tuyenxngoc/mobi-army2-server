package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class VenomousSpider extends Boss {

    public VenomousSpider(IFightManager fightManager, byte index, short x, short y, short maxHp) {
        super(fightManager, index, (byte) 22, "Venomous Spider", x, y, (short) 45, (short) 48, maxHp, 8);
        super.isFlying = true;
    }

    @Override
    public void turnAction() {

    }
}
