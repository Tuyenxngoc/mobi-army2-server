package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.MapTile;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.server.EffectManager;

public class ItemToNhen extends Bullet {
    public ItemToNhen(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.isCollected) {
            bulletManager.getFightManager().getMapManger().addNewTiles(new MapTile(bulletManager.getMangNhenId(), (short) (X - 21), (short) (Y - 20), EffectManager.spiderWebData, true));
            bulletManager.decreaseSpiderWebCount();
        }
    }
}
