package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.MapTile;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.server.EffectManager;

public class ItemToNhen extends Bullet {
    public ItemToNhen(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 56, damage, player, x, y, vx, vy, 70, 70);
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            bulletManager.getFightMapManager().addNewTiles(new MapTile(-1, (short) (x - 21), (short) (y - 20), EffectManager.spiderWebData, true));
        }
    }
}