package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemBomB52 extends Bullet {
    public ItemBomB52(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (super.isCollected) {
            this.bulletManager.addBullet(new B52Bullet(bulletManager, (byte) 3, this.damage, super.player, this.x - 50, this.y - 260, 2, 0, 0, 80, this.x, this.y));
        }
    }
}
