package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class ChickyBullet extends Bullet {

    protected byte force2;
    private long satThuongGoc;

    public ChickyBullet(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, byte force2) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        this.force2 = force2;
        this.satThuongGoc = satThuong;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (this.force2 == super.frame) {
            bulletManager.addBullet(new ChickyTrung(bulletManager, (byte) 20, (int) this.satThuongGoc, super.pl, super.lastX, super.lastY, 0, 0, 20, 50));
        }
        if (this.isCollect()) {
            return;
        }
    }

}