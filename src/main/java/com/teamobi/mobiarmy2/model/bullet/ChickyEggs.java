package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class ChickyEggs extends Bullet {

    public ChickyEggs(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        if (super.frame == 0) {
            short[] XYVC = bulletManager.getCollisionPoint(X, Y, X, (short) (Y + 8), isXuyenPlayer, isXuyenMap);
            if (XYVC != null) {
                collect = true;
                X = XYVC[0];
                Y = XYVC[1];
                XArray.add(X);
                YArray.add(Y);
                XArray.add(X);
                YArray.add(Y);
                if (this.isCanCollision) {
                    bulletManager.getFightManager().getMapManger().collision(X, Y, this);
                }
                return;
            } else {
                Y += 8;
            }
        }
        super.nextXY();
    }

}
