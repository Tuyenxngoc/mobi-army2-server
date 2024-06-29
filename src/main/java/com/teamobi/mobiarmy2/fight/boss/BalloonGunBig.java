package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;

import java.io.IOException;

public class BalloonGunBig extends Boss {

    public BalloonGunBig(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 35;
        super.height = 39;
        this.fly = true;
        this.XPExist = 300;
    }

    @Override
    public void turnAction() {
    }

}
