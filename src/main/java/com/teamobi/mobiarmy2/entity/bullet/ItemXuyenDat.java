package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemXuyenDat extends Bullet {
    private final int force;

    public ItemXuyenDat(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100, int force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        this.force = force / 2;
        this.canPassThroughMap = true;
        this.canPassThroughPlayers = false;
    }

    @Override
    public void nextXY() {
        frame++;
        short preX = X, preY = Y;
        X += vx;
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
            return;
        }
        XArray.add(X);
        YArray.add(Y);
        if ((X < -100) || (X > bulletManager.getFightManager().getMapManger().getWidth() + 100) || (Y > bulletManager.getFightManager().getMapManger().getHeight() + 200)) {
            XArray.add(X);
            YArray.add(Y);
            isCollected = true;
            return;
        }
        if (this.frame == force - 1) {
            XArray.add(X);
            YArray.add(Y);
            this.isCollected = true;
            return;
        }
        vyTemp2 -= g100;
        if (Math.abs(vyTemp2) >= 100) {
            vy -= vyTemp2 / 100;
            vyTemp2 %= 100;
        }
        if (this.bulletManager.isHasVoiRong()) {
            for (BulletManager.VoiRong vr : this.bulletManager.getVoiRongs()) {
                if (this.X >= vr.X - 5 && this.X <= vr.X + 10) {
                    this.vx -= 2;
                    this.vy -= 2;
                    break;
                }
            }
        }
    }
}
