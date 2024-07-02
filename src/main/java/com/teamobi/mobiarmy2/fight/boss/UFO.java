package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;

public class UFO extends Boss {

    private boolean turnShoot;

    public UFO(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 51;
        super.height = 46;
        this.fly = true;
        this.XPExist = 400;
    }

    @Override
    public void turnAction() {
        try {
            short ys = this.y, xs = this.x;
            while (this.turnShoot && ys < this.fightManager.mapManager.height + 200 && !this.fightManager.mapManager.isCollision(xs, ys)) {
                if (ys > this.fightManager.mapManager.height) {
                    this.turnShoot = false;
                }
                ys++;
            }
            if (!this.turnShoot) {
                this.turnShoot = true;
                Player pl = this.fightManager.getPlayerClosest(this.x, this.y);
                if (pl != null) {
                    this.y = (short) (pl.y - Utils.nextInt(150, 500));
                    this.x = pl.x;
                    this.fightManager.flyChangeLocation(super.index);
                    if (!fightManager.checkWin()) {
                        fightManager.nextTurn();
                    }
                } else if (!fightManager.checkWin()) {
                    fightManager.nextTurn();
                }
            } else {
                this.turnShoot = false;
                this.fightManager.newShoot(this.index, (byte) 42, (short) 270, (byte) 20, (byte) 0, (byte) 1, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
