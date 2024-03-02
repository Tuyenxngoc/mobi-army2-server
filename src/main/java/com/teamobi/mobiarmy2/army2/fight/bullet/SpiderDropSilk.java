package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.MapEntry;
import com.teamobi.mobiarmy2.army2.fight.Player;
import com.teamobi.mobiarmy2.army2.fight.boss.SpiderPoisonous;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SpiderDropSilk extends Bullet {

    public SpiderDropSilk(BulletManager bullMNG, byte bullId, long satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.X, pl.Y - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        this.collect = true;
        this.XArray.add(X);
        this.YArray.add(Y);
        Y += 38;
        this.XArray.add(X);
        this.YArray.add(Y);
        if (super.collect) {
            try {
                SpiderPoisonous boss = (SpiderPoisonous) this.pl;
                boss.target.Y = (short) (Y - 3);
                this.fm.cLocation(boss.target.index);
            } catch (IOException ex) {
                Logger.getLogger(SpiderDropSilk.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.fm.mapMNG.addEntry(new MapEntry(this.bullMNG.mangNhenId++, (short) (X - 21), (short) (Y - 20), MapEntry.mangNhenData, (short) MapEntry.mangNhenW, (short) MapEntry.mangNhenH, true));

        }
    }

}
