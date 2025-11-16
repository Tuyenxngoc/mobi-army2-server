package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ChickyEggs extends Bullet {
    public ChickyEggs(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        if (super.frame == 0) {
            short[] XYVC = bulletManager.getCollisionPoint(x, y, x, (short) (y + 8), canPassThroughPlayers, canPassThroughMap);
            if (XYVC != null) {
                isCollected = true;
                x = XYVC[0];
                y = XYVC[1];
                XArray.add(x);
                YArray.add(y);
                XArray.add(x);
                YArray.add(y);
                if (this.canCollide) {
                    bulletManager.getFightManager().getMapManger().collision(x, y, this);
                }
                return;
            } else {
                y += 8;
            }
        }
        super.nextXY();
    }
}
