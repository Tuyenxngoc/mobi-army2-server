package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.Point;

public class ItemBomB52 extends Bullet {
    public ItemBomB52(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 4, damage, player, x, y, vx, vy, 0, 80);
        this.canCollide = false;
        this.canSuperType = false;
        bulletManager.setTypeShoot((byte) 1);
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            bulletManager.addBullet(new B52Bullet(bulletManager, damage, player, x - 140, y - 322, new Point(x, y)));
        }
    }
}