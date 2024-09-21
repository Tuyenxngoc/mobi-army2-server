package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.EffectManager;
import com.teamobi.mobiarmy2.fight.MapTile;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class SpiderDropSilk extends Bullet {

    public SpiderDropSilk(BulletManager bulletManager, byte bullId, int damage, Player pl) {
        super(bulletManager, bullId, damage, pl, pl.getX(), pl.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        collect = true;
        XArray.add(X);
        YArray.add(Y);
        Y += 38;
        XArray.add(X);
        YArray.add(Y);
        //Todo

        bulletManager.getFightManager().getMapManger().addNewTiles(new MapTile(bulletManager.getMangNhenId(), (short) (X - 21), (short) (Y - 20), EffectManager.spiderWebData, true));
        bulletManager.decreaseSpiderWebCount();
    }
}
