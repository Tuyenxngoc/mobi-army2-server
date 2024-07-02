package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.MapEffectManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.boss.SpiderPoisonous;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpiderDropSilk extends Bullet {

    public SpiderDropSilk(BulletManager bullMNG, byte bullId, long satThuong, Player pl) {
        super(bullMNG, bullId, satThuong, pl, pl.x, pl.y - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        this.collect = true;
        this.XArray.add((short) X);
        this.YArray.add((short) Y);
        Y += 38;
        this.XArray.add((short) X);
        this.YArray.add((short) Y);
        if (super.collect) {
            try {
                SpiderPoisonous boss = (SpiderPoisonous) this.pl;
                boss.target.y = (short) (Y - 3);
                this.fightManager.cLocation(boss.target.index);
            } catch (IOException ex) {
                Logger.getLogger(SpiderDropSilk.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.fightManager.mapManager.addEntry(new MapEffectManager(this.bulletManager.mangNhenId++, (short) (X - 21), (short) (Y - 20), MapEffectManager.spiderData, (short) MapEffectManager.spiderWidth, (short) MapEffectManager.spiderHeight, true));

        }
    }

}
