package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.Player;


public class ItemSaoBang extends Bullet {

    public ItemSaoBang(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect) {
            this.bullMNG.addBullet(new Bullet(bullMNG, (byte) 24, this.satThuong, pl, this.X, this.Y - 187, 0, 3, 0, 50));
            for (int i = 1; i < 7; i++) {
                this.bullMNG.addBullet(new Bullet(bullMNG, (byte) 24, this.satThuong, pl, this.X + i * (i % 2 == 0 ? 30 : -30), this.Y - 187, 0, Math.abs(3 - i), 0, 50));
            }
        }
    }

}
