package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemSaoBang extends Bullet {
    public ItemSaoBang(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.isCollected) {
            this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 24, this.damage, player, this.X, this.Y - 187, 0, 3, 0, 50));
            for (int i = 1; i < 7; i++) {
                this.bulletManager.addBullet(new Bullet(bulletManager, (byte) 24, this.damage, player, this.X + i * (i % 2 == 0 ? 30 : -30), this.Y - 187, 0, Math.abs(3 - i), 0, 50));
            }
        }
    }
}
