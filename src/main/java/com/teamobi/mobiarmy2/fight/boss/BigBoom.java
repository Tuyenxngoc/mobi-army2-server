package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

public class BigBoom extends Boss {

    public BigBoom(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 100;
        super.width = 28;
        super.height = 28;
        this.XPExist = 200;
    }

    public void bomAction() {
        this.isDie = true;
        this.height = 0;
        this.width = 0;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = this.fightManager.findClosestPlayerByX(this.x);
            if (pl == null) {
                return;
            }
            //45% add bom
            if (Utils.nextInt(100) <= 0) {
                short Vgoc = 0;
                short Vsuc;
                if (pl.x > x) {
                    Vgoc = (short) Utils.nextInt(70, 75);
                }
                if (pl.x <= x) {
                    Vgoc = (short) Utils.nextInt(110, 115);
                }
                Vsuc = (short) (Math.abs(x - pl.x) / 20);
                if (Vsuc < 8) {
                    Vsuc = 8;
                }
                if (Vsuc > 30) {
                    Vsuc = 30;
                }
                this.fightManager.newShoot(this.index, (byte) 34, (short) Vgoc, (byte) Vsuc, (byte) 0, (byte) 1, false);
            } else {
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
                if (Math.abs(x - pl.x) <= 35 && Math.abs(y - pl.y) <= 35) {
                    this.fightManager.newShoot(this.index, (byte) 31, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
                } else if (this.buocDi < this.theLuc) {
                    this.itemUsed = 7;
                    this.fightManager.newShoot(this.index, (byte) 7, (short) Utils.getArgXY(x, y, pl.x, pl.y), (byte) 30, (byte) 0, (byte) 1, false);
                } else if (!fightManager.checkWin()) {
                    this.fightManager.nextTurn();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
