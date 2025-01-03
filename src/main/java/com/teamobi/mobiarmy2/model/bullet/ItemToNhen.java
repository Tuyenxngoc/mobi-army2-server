package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.server.EffectManager;
import com.teamobi.mobiarmy2.model.MapTile;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class ItemToNhen extends Bullet {

    public ItemToNhen(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            bulletManager.getFightManager().getMapManger().addNewTiles(new MapTile(bulletManager.getMangNhenId(), (short) (X - 21), (short) (Y - 20), EffectManager.spiderWebData, true));
            bulletManager.decreaseSpiderWebCount();
        }
    }

}
