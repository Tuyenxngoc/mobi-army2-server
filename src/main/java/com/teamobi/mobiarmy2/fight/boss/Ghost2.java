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
            Player pl = this.fightManager.findClosestPlayerByX(this.x);
            if (pl == null) {
                return;
            }
            //mò tới nguoi chơi
            if (this.x > pl.x) {
                this.x = (short) (pl.x + 30);
            } else {
                this.x = (short) (pl.x - 30);
            }
            this.y = (short) (pl.y - 15);
            this.fightManager.flyChangeLocation(super.index);
            this.fightManager.GhostBullet(this.index, pl.index);
            short wmap = this.fightManager.mapManager.width;
            short hmap = this.fightManager.mapManager.height;
            this.x = (short) ((this.x > pl.x && pl.x < wmap - 80) ? (pl.x + 80) : ((pl.x > 80) ? (pl.x - 80) : (pl.x + 80)));
            this.y = (short) Utils.nextInt(0, hmap - 200);
            this.fightManager.flyChangeLocation(super.index);
            pl.updateHP(-Utils.nextInt(200, 500));
            if (!fightManager.checkWin()) {
                fightManager.nextTurn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
