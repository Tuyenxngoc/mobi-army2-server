package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;

public class UFOPet extends Boss {

    public UFOPet(FightManager fightManager, short x, short y, short maxHp) {
        super(fightManager, (byte) 16, "UFO", x, y, (short) 51, (short) 46, maxHp, 4);
        super.isFlying = true;
    }

    @Override
    public boolean shouldCollide() {
        return !isDead;
    }

    @Override
    public boolean shouldCollideWith(Player shooter) {
        // UFOPet không va chạm với UFO khác (bao gồm cả UFO và UFOPet)
        return !(shooter instanceof UFO || shooter instanceof UFOPet);
    }

    @Override
    public void turnAction() {

    }
}
