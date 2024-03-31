package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.Player;

public class ChickyTrung extends Bullet {

    public ChickyTrung(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
    }

    @Override
    public void nextXY() {
        if (super.frame == 0) {
            short[] XYVC = bullMNG.getCollisionPoint(X, Y, X, (short) (Y + 8), isXuyenPlayer, isXuyenMap);
            if (XYVC != null) {
                collect = true;
                X = XYVC[0];
                Y = XYVC[1];
                XArray.add(X);
                YArray.add(Y);
                XArray.add(X);
                YArray.add(Y);
                if (this.isCanCollision) {
                    fm.mapMNG.collision(X, Y, this);
                }
                return;
            } else {
                Y += 8;
            }
        }
        super.nextXY();
    }

}
