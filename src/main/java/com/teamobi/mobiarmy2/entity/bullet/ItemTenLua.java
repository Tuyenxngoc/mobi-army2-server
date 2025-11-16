package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemTenLua extends Bullet {
    protected byte force;

    public ItemTenLua(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, byte force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
        this.force = force;
    }

    @Override
    public void update() {
        super.update();
        if (super.frame == this.force || this.isCollected) {
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, super.damage, player, this.x + 18, this.y - 20, 2, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, super.damage, player, this.x + 17, this.y - 20, -3, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, super.damage, player, this.x + 16, this.y - 23, 3, -2, 15, 60));
        }
    }
}
