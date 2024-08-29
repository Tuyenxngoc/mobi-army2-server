package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;

public class SmallBoom extends Boss {

    public SmallBoom(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 100;
        super.width = 18;
        super.height = 18;
        this.XPExist = 50;
    }

    public void bomAction() {
        this.isDie = true;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = this.fightManager.findClosestPlayerByX(this.x);
            if (pl != null) {
                while (x != pl.x || y != pl.y) {
                    int preX = this.x;
                    int preY = this.y;
                    if (pl.x < this.x) {
                        super.move(false);
                    } else if (pl.x > this.x) {
                        super.move(true);
                    } else if (!fightManager.mapManager.isCollision(this.x, this.y)) {
                        this.y++;
                    }
                    // if ko di chuyen dc
                    if (preX == this.x && preY == this.y) {
                        break;
                    }
                }
                this.fightManager.changeLocation(super.index);
                if (Math.abs(y - pl.y) <= 25 && Math.abs(x - pl.x) <= 25) {
                    this.fightManager.newShoot(this.index, (byte) 32, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
                } else if (Math.abs(x - pl.x) < 25 && (y - (pl.y)) > 25) {
                    this.itemUsed = 1;
                    this.fightManager.newShoot(this.index, (byte) 5, (short) Utils.getArgXY(x, y, pl.x, pl.y), (byte) 30, (byte) 0, (byte) 1, false);
                } else if (((pl.y) - y > 25 && Math.abs(x - pl.x) < 25)) {
                    this.itemUsed = 1;
                    short VRd = (short) Utils.nextInt(60, 120);
                    byte Suc = (byte) Utils.nextInt(5, 10);
                    this.fightManager.newShoot(this.index, (byte) 5, (short) VRd, (byte) Suc, (byte) 0, (byte) 1, false);
                } else if (super.buocDi < super.theLuc) {
                    this.itemUsed = 1;
                    short Vxy = (short) (pl.x > y ? 80 : 180 - 80);
                    this.fightManager.newShoot(this.index, (byte) 5, (short) Vxy, (byte) 8, (byte) 0, (byte) 1, false);
                } else if (!fightManager.checkWin()) {
                    this.fightManager.nextTurn();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
