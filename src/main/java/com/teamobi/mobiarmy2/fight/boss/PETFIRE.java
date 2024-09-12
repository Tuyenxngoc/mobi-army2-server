package com.teamobi.mobiarmy2.fight.boss;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

import java.io.IOException;

public class PETFIRE extends Boss {

    private boolean acll;
    private Player pl;
    private byte turnDie;

    public PETFIRE(FightManager fightMNG, byte idGun, String name, byte location, int HPMax, short X, short Y, Player pl, byte tdie) throws IOException {
        super(fightMNG, idGun, name, location, HPMax, X, Y);
        super.theLuc = 0;
        super.width = 51;
        super.height = 46;
        this.fly = true;
        this.pl = pl;
        this.turnDie = tdie;
        this.XPExist = 4;
        this.team = pl.index % 2 == 0;

    }

    @Override
    public void turnAction() {
        int dame = this.HPMax;
        try {
            Boss bs = (Boss) this.fightManager.getBossClosest(this.X, this.Y);
            if (bs == null) {
                return;
            }
            //mò tới boss
            if (this.X > bs.X) {
                this.X = (short) (bs.X + 30);
            } else {
                this.X = (short) (bs.X - 30);
            }
            this.Y = (short) (bs.Y - 15);
            this.fightManager.flyChangeLocation(super.index);
            this.fightManager.GhostBullet(this.index, bs.index);
            short wmap = this.fightManager.mapMNG.Width;
            short hmap = this.fightManager.mapMNG.Height;
            this.X = (short) Utils.nextInt(100, wmap - 100);
            this.Y = (short) Utils.nextInt(0, hmap - 200);
            this.fightManager.flyChangeLocation(super.index);
            bs.updateHP(-dame);
            //  bs.updateHP(-dame);
            if (bs.isDie) {
                pl.updateEXP(this.XPExist * 100);
            }
            if (!fightManager.checkWin()) {
                fightManager.nextTurn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
