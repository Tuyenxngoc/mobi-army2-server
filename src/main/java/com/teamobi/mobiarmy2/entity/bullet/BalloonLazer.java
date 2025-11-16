package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class BalloonLazer extends Bullet {
    public BalloonLazer(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int x, int y) {
        super(bullMNG, bullId, satThuong, pl, x, y, 0, 0, 0, 100);
    }

    @Override
    public boolean isCollected() {
        return this.isCollected;
    }

    public void nextXY() {
        frame++;
        Player pl2 = bulletManager.getFightManager().findClosestPlayer(X, Y);
        X = pl2.getX();
        Y = pl2.getY();
        this.XArray.add(X);
        this.YArray.add(Y);
        if ((X < -200) || (X > bulletManager.getFightManager().getMapManger().getWidth() + 200) || (Y > bulletManager.getFightManager().getMapManger().getHeight() + 200)) {
            isCollected = true;
            return;
        }
        short preX = X, preY = Y;
        lastX = X;
        Y += vy;
        lastY = Y;
        short[] XYVC = bulletManager.getCollisionPoint(preX, preY, X, Y, canPassThroughPlayers, canPassThroughMap);
        if (XYVC != null) {
            isCollected = true;
            X = XYVC[0];
            Y = XYVC[1];
            XArray.add(X);
            YArray.add(Y);
            if (this.canCollide) {
                bulletManager.getFightManager().getMapManger().collision(X, Y, this);
            }
        }
    }
}
