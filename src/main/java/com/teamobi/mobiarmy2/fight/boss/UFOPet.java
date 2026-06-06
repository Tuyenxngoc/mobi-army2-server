package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.IFightManager;

public class UFOPet extends Boss {

    public UFOPet(IFightManager fightManager, short x, short y, int maxHp) {
        super(fightManager, (byte) 16, "UFO", x, y, (short) 51, (short) 46, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public boolean shouldCollideWith(Bullet bullet) {
        return bullet.getBullId() != 42;
    }

    @Override
    public void turnAction() {

    }
}
