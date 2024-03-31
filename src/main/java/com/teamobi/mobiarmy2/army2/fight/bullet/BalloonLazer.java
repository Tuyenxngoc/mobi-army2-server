package com.teamobi.mobiarmy2.army2.fight.bullet;

import com.teamobi.mobiarmy2.army2.fight.Bullet;
import com.teamobi.mobiarmy2.army2.fight.BulletManager;
import com.teamobi.mobiarmy2.army2.fight.Player;

public class BalloonLazer extends Bullet {

    public BalloonLazer(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int x, int y) {
        super(bullMNG, bullId, satThuong, pl, x, y, 0, 0, 0, 100);
    }

    @Override

    public boolean isCollect() {
        return this.collect;
    }

    public void nextXY() {
        frame++;
        Player pl2 = this.fm.getPlayerClosest(X, Y);
        X = pl2.X;
        Y = pl2.Y;
        this.XArray.add(X);
        this.YArray.add(Y);
        if ((X < -200) || (X > fm.mapMNG.Width + 200) || (Y > fm.mapMNG.Height + 200)) {
            collect = true;
            return;
        }
        short preX = X, preY = Y;
        lastX = X;
        Y += vy;
        lastY = Y;
        short[] XYVC = bullMNG.getCollisionPoint(preX, preY, X, Y, isXuyenPlayer, isXuyenMap);
        if (XYVC != null) {
            collect = true;
            X = XYVC[0];
            Y = XYVC[1];
            XArray.add(X);
            YArray.add(Y);
            if (this.isCanCollision) {
                fm.mapMNG.collision(X, Y, this);
            }
        }
    }

}
