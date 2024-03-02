package com.teamobi.mobiarmy2.army2.fight.boss;

import com.teamobi.mobiarmy2.army2.fight.Boss;
import com.teamobi.mobiarmy2.army2.fight.FightManager;
import com.teamobi.mobiarmy2.army2.fight.Player;
import com.teamobi.mobiarmy2.army2.server.Until;

import java.io.IOException;

public class PETFIRE extends Boss {

    private boolean acll;
    private final Player pl;
    private final byte turnDie;

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
            Boss bs = this.fightMNG.getBossClosest(this.X, this.Y);
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
            this.fightMNG.flyChangeLocation(super.index);
            this.fightMNG.GhostBullet(this.index, bs.index);
            short wmap = this.fightMNG.mapMNG.Width;
            short hmap = this.fightMNG.mapMNG.Height;
            this.X = (short) Until.nextInt(100, wmap - 100);
            this.Y = (short) Until.nextInt(0, hmap - 200);
            this.fightMNG.flyChangeLocation(super.index);
            bs.updateHP(-dame);
            //  bs.updateHP(-dame);
            if (bs.isDie) {
                pl.updateEXP(this.XPExist * 100);
            }
            if (!fightMNG.checkWin()) {
                fightMNG.nextTurn();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
