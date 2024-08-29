package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

public class Trex extends Boss {

    public Trex(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 45;
        super.height = 50;
        this.fly = false;
        this.XPExist = 400;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = this.fightManager.findClosestPlayerByX(this.x);
            if (pl == null) {
                return;
            }
            if (Math.abs(x - pl.x) <= 90 && Math.abs(y - pl.y) <= 250) {
                this.fightManager.newShoot(this.index, (byte) 35, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
            } else {
                int randi = Utils.nextInt(3);
                short[] FA = null;
                switch (randi) {
                    case 0:
                        this.fightManager.newShoot(this.index, (byte) 37, (short) 110, (byte) 30, (byte) 0, (byte) 1, false);
                        break;
                    case 1:
                        FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, false, x, y, pl.x, pl.y, (short) 70, (short) (70), 110, 5, 10, 80);
                        if (FA == null) {
                            if (!fightManager.checkWin()) {
                                fightManager.nextTurn();
                            }
                            return;
                        }
                        this.fightManager.newShoot(this.index, (byte) 40, FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                        break;
                    case 2:
                        FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, false, x, y, pl.x, pl.y, (short) (70), (short) (70), 110, 5, 10, 80);
                        if (FA == null) {
                            if (!fightManager.checkWin()) {
                                fightManager.nextTurn();
                            }
                            return;
                        }
                        this.fightManager.newShoot(this.index, (byte) 41, FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
