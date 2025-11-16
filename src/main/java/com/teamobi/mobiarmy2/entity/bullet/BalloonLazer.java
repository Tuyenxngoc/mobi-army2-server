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
        Player pl2 = bulletManager.getFightManager().findClosestPlayer(x, y);
        x = pl2.getX();
        y = pl2.getY();
        this.XArray.add(x);
        this.YArray.add(y);
        if ((x < -200) || (x > bulletManager.getFightManager().getMapManger().getWidth() + 200) || (y > bulletManager.getFightManager().getMapManger().getHeight() + 200)) {
            isCollected = true;
            return;
        }
        short preX = x, preY = y;
        lastX = x;
        y += vy;
        lastY = y;
        short[] XYVC = bulletManager.getCollisionPoint(preX, preY, x, y, canPassThroughPlayers, canPassThroughMap);
        if (XYVC != null) {
            isCollected = true;
            x = XYVC[0];
            y = XYVC[1];
            XArray.add(x);
            YArray.add(y);
            if (this.canCollide) {
                bulletManager.getFightManager().getMapManger().collision(x, y, this);
            }
        }
    }
}
