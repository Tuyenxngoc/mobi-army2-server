package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
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
            Player pl = fightMNG.getPlayerClosest(X, Y);
            if (pl == null)
                return;
            if (Math.abs(X - pl.X) <= 40 && Math.abs(Y - pl.Y) <= 30) {
                this.itemUsed = 9;
                fightMNG.isNextTurn = false;
                this.fightMNG.newShoot(this.index, (byte) 8, (short) Utils.getArgXY(X, Y, pl.X, pl.Y), (byte) 30, (byte) 0, (byte) 1);
                fightMNG.isNextTurn = true;
                byte force = (byte) Utils.nextInt(15, 30);
                short arg = (short) Utils.nextInt(80, 100);
                this.fightMNG.newShoot(this.index, (byte) 36, (short) arg, (byte) force, (byte) 0, (byte) 1);
                return;
            }
            ArrayList<Player> ar = new ArrayList();
            for (int i = 0; i < ServerManager.maxPlayers; i++) {
                if (this.fightMNG.players[i] != null && !this.fightMNG.players[i].isDie)
                    ar.add(this.fightMNG.players[i]);
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
                    FA = fightMNG.getForceArgXY(idNV, this.fightMNG.bullMNG, false, X, Y, pl.X, (short) (pl.Y - (pl.height / 2)), (short) (pl.width / 2), pl.height, 50, 5, 70, 70);
                    if (FA == null) {
                        if (!fightMNG.checkWin())
                            fightMNG.nextTurn();
                        return;
                    }
                    fightMNG.isNextTurn = false;
                    this.fightMNG.newShoot(this.index, (byte) 8, FA[0], (byte) FA[1], (byte) 0, (byte) 1);
                    fightMNG.isNextTurn = true;
                    byte force = (byte) Utils.nextInt(15, 30);
                    short arg = (short) Utils.nextInt(80, 100);
                    this.fightMNG.newShoot(this.index, (byte) 36, (short) arg, (byte) force, (byte) 0, (byte) 1);
                    break;
                //lazer
                case 1:
                    this.itemUsed = 16;
                    FA = fightMNG.getForceArgXY(idNV, this.fightMNG.bullMNG, false, X, Y, pl.X, (short) (pl.Y - (pl.height / 2)), (short) (pl.width / 2), pl.height, 50, 5, 10, 50);
                    if (FA == null) {
                        if (!fightMNG.checkWin())
                            fightMNG.nextTurn();
                        return;
                    }
                    this.fightMNG.newShoot(this.index, (byte) 14, FA[0], (byte) FA[1], (byte) 0, (byte) 1);
                    break;
                //rôcet
                case 2:
                    FA = fightMNG.getForceArgXY(idNV, this.fightMNG.bullMNG, true, X, Y, pl.X, (short) (pl.Y - (pl.height / 2)), (short) (pl.width / 2), pl.height, 50, 5, 30, 50);
                    if (FA == null) {
                        if (!fightMNG.checkWin())
                            fightMNG.nextTurn();
                        return;
                    }
                    this.fightMNG.newShoot(this.index, (byte) 33, FA[0], (byte) FA[1], (byte) 0, (byte) 1);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}