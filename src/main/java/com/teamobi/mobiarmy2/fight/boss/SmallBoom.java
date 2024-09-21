package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class SmallBoom extends Boss {

    public SmallBoom(IFightManager fightManager, byte index, byte characterId, String name, short x, short y, short width, short height, short maxHp, int xpExist) {
        super(fightManager, index, characterId, name, x, y, width, height, maxHp, xpExist);
    }

    @Override
    public void turnAction() {

    }

    public void bomAction() {
        System.out.println("Bom Action");
    }
}
