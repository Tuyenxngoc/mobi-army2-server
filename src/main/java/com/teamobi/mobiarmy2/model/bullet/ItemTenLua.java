package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class ItemTenLua extends Bullet {

    protected byte force;

    public ItemTenLua(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, byte force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        this.force = force;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.frame == this.force || this.collect) {
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, super.damage, pl, this.X + 18, this.Y - 20, 2, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, super.damage, pl, this.X + 17, this.Y - 20, -3, -1, 15, 60));
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 27, super.damage, pl, this.X + 16, this.Y - 23, 3, -2, 15, 60));
        }
    }
}
