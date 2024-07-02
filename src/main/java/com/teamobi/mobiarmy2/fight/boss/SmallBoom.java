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
            Player pl = this.fightMNG.getPlayerClosest(this.X, this.Y);
            if (pl != null) {
                while (X != pl.X || Y != pl.Y) {
                    int preX = this.X;
                    int preY = this.Y;
                    if (pl.X < this.X) {
                        super.move(false);
                    } else if (pl.X > this.X) {
                        super.move(true);
                    } else if (!fightMNG.mapManager.isCollision(this.X, this.Y)) {
                        this.Y++;
                    }
                    // if ko di chuyen dc
                    if (preX == this.X && preY == this.Y) {
                        break;
                    }
                }
                this.fightMNG.changeLocation(super.index);
                if (Math.abs(Y - pl.Y) <= 25 && Math.abs(X - pl.X) <= 25) {
                    this.fightMNG.newShoot(this.index, (byte) 32, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
                } else if (Math.abs(X - pl.X) < 25 && (Y - (pl.Y)) > 25) {
                    this.itemUsed = 1;
                    this.fightMNG.newShoot(this.index, (byte) 5, (short) Utils.getArgXY(X, Y, pl.X, pl.Y), (byte) 30, (byte) 0, (byte) 1, false);
                } else if (((pl.Y) - Y > 25 && Math.abs(X - pl.X) < 25)) {
                    this.itemUsed = 1;
                    short VRd = (short) Utils.nextInt(60, 120);
                    byte Suc = (byte) Utils.nextInt(5, 10);
                    this.fightMNG.newShoot(this.index, (byte) 5, (short) VRd, (byte) Suc, (byte) 0, (byte) 1, false);
                } else if (super.buocDi < super.theLuc) {
                    this.itemUsed = 1;
                    short Vxy = (short) (pl.X > Y ? 80 : 180 - 80);
                    this.fightMNG.newShoot(this.index, (byte) 5, (short) Vxy, (byte) 8, (byte) 0, (byte) 1, false);
                } else if (!fightMNG.checkWin()) {
                    this.fightMNG.nextTurn();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
