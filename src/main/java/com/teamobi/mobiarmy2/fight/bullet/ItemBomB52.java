package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class ItemBomB52 extends Bullet {

    public ItemBomB52(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            this.bulletManager.addBullet(new B52Bullet(bulletManager, (byte) 3, this.damage, super.pl, this.X - 50, this.Y - 260, 2, 0, 0, 80, this.X, this.Y));
        }
    }

}
