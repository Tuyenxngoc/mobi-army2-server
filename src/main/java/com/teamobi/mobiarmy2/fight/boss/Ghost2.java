package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;

public class Ghost2 extends Boss {

    public Ghost2(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 35;
        super.height = 31;
        this.fly = true;
        this.XPExist = 350;
    }

    @Override
    public void turnAction() {
        try {
            Player pl = this.fightMNG.getPlayerClosest(this.X, this.Y);
            if (pl == null) {
                return;
            }
            //mò tới nguoi chơi
            if (this.X > pl.X) {
                this.X = (short) (pl.X + 30);
            } else {
                this.X = (short) (pl.X - 30);
            }
            this.Y = (short) (pl.Y - 15);
            this.fightMNG.flyChangeLocation(super.index);
            this.fightMNG.GhostBullet(this.index, pl.index);
            short wmap = this.fightMNG.mapManager.width;
            short hmap = this.fightMNG.mapManager.height;
            this.X = (short) ((this.X > pl.X && pl.X < wmap - 80) ? (pl.X + 80) : ((pl.X > 80) ? (pl.X - 80) : (pl.X + 80)));
            this.Y = (short) Utils.nextInt(0, hmap - 200);
            this.fightMNG.flyChangeLocation(super.index);
            pl.updateHP(-Utils.nextInt(200, 500));
            if (!fightMNG.checkWin()) {
                fightMNG.nextTurn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
