package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ItemLaser extends Bullet {
    public ItemLaser(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            this.bulletManager.addBullet(new ItemLaserDelay(bulletManager, (byte) 15, this.damage, super.pl, this.X, this.Y));
        }
    }
}
