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
            short ys = this.Y, xs = this.X;
            while (this.turnShoot && ys < this.fightMNG.mapManager.height + 200 && !this.fightMNG.mapManager.isCollision(xs, ys)) {
                if (ys > this.fightMNG.mapManager.height) {
                    this.turnShoot = false;
                }
                ys++;
            }
            if (!this.turnShoot) {
                this.turnShoot = true;
                Player pl = this.fightMNG.getPlayerClosest(this.X, this.Y);
                if (pl != null) {
                    this.Y = (short) (pl.Y - Utils.nextInt(150, 500));
                    this.X = pl.X;
                    this.fightMNG.flyChangeLocation(super.index);
                    if (!fightMNG.checkWin()) {
                        fightMNG.nextTurn();
                    }
                } else if (!fightMNG.checkWin()) {
                    fightMNG.nextTurn();
                }
            } else {
                this.turnShoot = false;
                this.fightMNG.newShoot(this.index, (byte) 42, (short) 270, (byte) 20, (byte) 0, (byte) 1, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
