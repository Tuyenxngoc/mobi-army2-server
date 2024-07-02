package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class Robot extends Boss {

    public Robot(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 24;
        super.height = 25;
        this.XPExist = 240;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = fightManager.getPlayerClosest(x, y);
            if (pl == null) {
                return;
            }
            if (Math.abs(x - pl.x) <= 40 && Math.abs(y - pl.y) <= 40) {
                fightManager.isNextTurn = false;
                this.fightManager.newShoot(this.index, (byte) 35, (short) 0, (byte) 0, (byte) 0, (byte) 1, false);
                fightManager.isNextTurn = true;
            }
            if (Math.abs(x - pl.x) <= 40) {
                byte force = (byte) Utils.nextInt(15, 30);
                short arg = (short) Utils.nextInt(80, 100);
                this.fightManager.newShoot(this.index, (byte) 36, (short) arg, (byte) force, (byte) 0, (byte) 1, false);
                return;
            }
            ArrayList<Player> ar = new ArrayList();
            for (int i = 0; i < 8; i++) {
                if (this.fightManager.players[i] != null && !this.fightManager.players[i].isDie) {
                    ar.add(this.fightManager.players[i]);
                }
            }
            if (ar.size() > 0) {
                pl = ar.get(Utils.nextInt(ar.size()));
            }
            if (pl == null) {
                return;
            }
            short[] FA = null;
            switch (Utils.nextInt(9)) {
                case 0:
                    FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, true, x, y, pl.x, pl.y, (short) (pl.width / 2), (short) (pl.height / 2), 50, 5, 80, 100);
                    if (FA == null) {
                        if (!fightManager.checkWin()) {
                            fightManager.nextTurn();
                        }
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 0, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
                case 1:
                    FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, true, x, y, pl.x, pl.y, (short) (pl.width / 2), (short) (pl.height / 2), 50, 5, 80, 60);
                    if (FA == null) {
                        if (!fightManager.checkWin()) {
                            fightManager.nextTurn();
                        }
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 2, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
                case 2:
                    FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, true, x, y, pl.x, pl.y, (short) (pl.width / 2), (short) (pl.height / 2), 50, 5, 50, 80);
                    if (FA == null) {
                        if (!fightManager.checkWin()) {
                            fightManager.nextTurn();
                        }
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 10, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
                case 3:
                    this.itemUsed = 6;
                    FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, true, x, y, pl.x, pl.y, (short) (pl.width / 2), (short) (pl.height / 2), 50, 5, 70, 90);
                    if (FA == null) {
                        if (!fightManager.checkWin()) {
                            fightManager.nextTurn();
                        }
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 6, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
                case 4:
                    this.itemUsed = 7;
                    FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, true, x, y, pl.x, pl.y, (short) (pl.width / 2), (short) (pl.height / 2), 50, 5, 70, 80);
                    if (FA == null) {
                        if (!fightManager.checkWin()) {
                            fightManager.nextTurn();
                        }
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 7, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
                case 5:
                case 6:
                case 7:
                case 8:
                    FA = fightManager.getForceArgXY(idNV, fightManager.bulletManager, true, x, y, pl.x, pl.y, pl.width, pl.height, 50, 5, 0, 80);
                    if (FA == null) {
                        if (!fightManager.checkWin()) {
                            fightManager.nextTurn();
                        }
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 36, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
