package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class BalloonLazer extends Bullet {

    public BalloonLazer(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int x, int y) {
        super(bullMNG, bullId, satThuong, pl, x, y, 0, 0, 0, 100);
    }

    @Override
    public boolean isCollect() {
        return this.collect;
    }

    public void nextXY() {
        frame++;
        Player pl2 = bulletManager.getFightManager().findClosestPlayer(X, Y);
        X = pl2.getX();
        Y = pl2.getY();
        this.XArray.add(X);
        this.YArray.add(Y);
        if ((X < -200) || (X > bulletManager.getFightManager().getMapManger().getWidth() + 200) || (Y > bulletManager.getFightManager().getMapManger().getHeight() + 200)) {
            collect = true;
            return;
        }
        short preX = X, preY = Y;
        lastX = X;
        Y += vy;
        lastY = Y;
        short[] XYVC = bulletManager.getCollisionPoint(preX, preY, X, Y, isXuyenPlayer, isXuyenMap);
        if (XYVC != null) {
            collect = true;
            X = XYVC[0];
            Y = XYVC[1];
            XArray.add(X);
            YArray.add(Y);
            if (this.isCanCollision) {
                bulletManager.getFightManager().getMapManger().collision(X, Y, this);
            }
        }
    }

}
