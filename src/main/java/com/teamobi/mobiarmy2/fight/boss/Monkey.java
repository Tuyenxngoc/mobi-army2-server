package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.ServerManager;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;
import java.util.ArrayList;

public class Monkey extends Boss {

    public Monkey(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 100;
        super.width = 24;
        super.height = 25;
        this.XPExist = 240;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = this.fightMNG.getPlayerClosest(this.X, this.Y);
            if (pl == null) {
                return;
            }
            ArrayList<Player> ar = new ArrayList();
            for (int i = 0; i < ServerManager.maxPlayers; i++) {
                if (this.fightMNG.players[i] != null && !this.fightMNG.players[i].isDie) {
                    ar.add(this.fightMNG.players[i]);
                }
            }
            if (Math.abs(X - pl.X) <= 35 && Math.abs(Y - pl.Y) <= 35) {
                this.itemUsed = 1;
                this.fightMNG.newShoot(this.index, (byte) 5, (short) 84, (byte) 30, (byte) 0, (byte) 1, false);
                if (!fightMNG.checkWin()) {
                    fightMNG.nextTurn();
                }
                return;
            }
            short[] FA = null;
            switch (Utils.nextInt(2)) {
                case 0:
                case 1:
                case 2:
                    FA = fightMNG.getForceArgXY(idNV, fightMNG.bullMNG, true, X, Y, pl.X, pl.Y, (short) (pl.width / 2), (short) (pl.height / 2), 50, 5, 80, 100);
                    if (FA == null) {
                        if (!fightMNG.checkWin()) {
                            fightMNG.nextTurn();
                        }
                        return;
                    }
                    this.fightMNG.newShoot(this.index, (byte) 61, (short) FA[0], (byte) FA[1], (byte) 0, (byte) 1, false);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
