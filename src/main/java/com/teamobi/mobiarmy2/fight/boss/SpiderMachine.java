package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class SpiderMachine extends Boss {

    public SpiderMachine(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 42;
        super.height = 42;
        this.XPExist = 300;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = fightManager.findClosestPlayerByX(x);
            if (pl == null)
                return;
            if (Math.abs(x - pl.x) <= 40 && Math.abs(y - pl.y) <= 30) {
                this.itemUsed = 9;
                fightManager.isNextTurn = false;
                this.fightManager.newShoot(this.index, (byte) 8, (short) Utils.getArgXY(x, y, pl.x, pl.y), (byte) 30, (byte) 0, (byte) 1, false);
                fightManager.isNextTurn = true;
                byte force = (byte) Utils.nextInt(15, 30);
                short arg = (short) Utils.nextInt(80, 100);
                this.fightManager.newShoot(this.index, (byte) 36, (short) arg, (byte) force, (byte) 0, (byte) 1, false);
                return;
            }
            ArrayList<Player> ar = new ArrayList();
            for (int i = 0; i < 8; i++) {
                if (this.fightManager.players[i] != null && !this.fightManager.players[i].isDie)
                    ar.add(this.fightManager.players[i]);
            }
            if (ar.size() > 0)
                pl = ar.get(Utils.nextInt(ar.size()));
            if (pl == null) {
                return;
            }
            short[] FA = null;
            switch (Utils.nextInt(3)) {
                //tơ nhện
                case 0:
                    this.itemUsed = 9;
                    FA = fightManager.getForceArgXY(idNV, this.fightManager.bulletManager, false, x, y, pl.x, (short) (pl.y - (pl.height / 2)), (short) (pl.width / 2), pl.height, 50, 5, 70, 70);
                    if (FA == null) {
                        if (!fightManager.checkWin())
                            fightManager.nextTurn();
                        return;
                    }
                    fightManager.isNextTurn = false;
                    this.fightManager.newShoot(this.index, (byte) 8, FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    fightManager.isNextTurn = true;
                    byte force = (byte) Utils.nextInt(15, 30);
                    short arg = (short) Utils.nextInt(80, 100);
                    this.fightManager.newShoot(this.index, (byte) 36, (short) arg, (byte) force, (byte) 0, (byte) 1, false);
                    break;
                //lazer
                case 1:
                    this.itemUsed = 16;
                    FA = fightManager.getForceArgXY(idNV, this.fightManager.bulletManager, false, x, y, pl.x, (short) (pl.y - (pl.height / 2)), (short) (pl.width / 2), pl.height, 50, 5, 10, 50);
                    if (FA == null) {
                        if (!fightManager.checkWin())
                            fightManager.nextTurn();
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 14, FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
                //rôcet
                case 2:
                    FA = fightManager.getForceArgXY(idNV, this.fightManager.bulletManager, true, x, y, pl.x, (short) (pl.y - (pl.height / 2)), (short) (pl.width / 2), pl.height, 50, 5, 30, 50);
                    if (FA == null) {
                        if (!fightManager.checkWin())
                            fightManager.nextTurn();
                        return;
                    }
                    this.fightManager.newShoot(this.index, (byte) 33, FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}