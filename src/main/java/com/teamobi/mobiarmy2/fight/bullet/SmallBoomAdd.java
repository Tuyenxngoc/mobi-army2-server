package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.boss.SmallBoom;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;

public class SmallBoomAdd extends Bullet {

    public SmallBoomAdd(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        super.isXuyenPlayer = true;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        if (super.collect && X > 0 && X < bulletManager.getFightManager().getMapManger().getWidth() && Y < bulletManager.getFightManager().getMapManger().getHeight()) {
            Boss smallBoom = new SmallBoom(bulletManager.getFightManager(), (byte) bulletManager.getFightManager().getTotalPlayers(), X, Y, (short) 1000);
            bulletManager.getAddboss().add(smallBoom);
        }
    }
}
