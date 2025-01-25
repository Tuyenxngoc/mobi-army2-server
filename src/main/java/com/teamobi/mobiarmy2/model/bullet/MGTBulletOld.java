package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

public class MGTBulletOld extends Bullet {

    private final int force;

    public MGTBulletOld(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx100, int vy100, int force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx100, vy100, 0, 0);
        this.force = force;
    }

    @Override
    public void nextXY() {
        frame++;
        XArray.add(X);
        YArray.add(Y);
        if ((X < -200) || (X > bulletManager.getFightManager().getMapManger().getWidth() + 200) || (Y > bulletManager.getFightManager().getMapManger().getHeight() + 200)) {
            collect = true;
            return;
        }
        short preX = X, preY = Y;
        vxTemp += Math.abs(vx);
        vyTemp += Math.abs(vy);
        if (Math.abs(vxTemp) >= 100) {
            if (vx > 0) {
                X += vxTemp / 100;
            } else {
                X -= vxTemp / 100;
            }
            vxTemp %= 100;
        }
        if (Math.abs(vyTemp) >= 100) {
            if (vy > 0) {
                Y += vyTemp / 100;
            } else {
                Y -= vyTemp / 100;
            }
            vyTemp %= 100;
        }
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
            return;
        }
        if (frame == force) {
            vy = (short) (-vy);
            vxTemp = 0;
            vyTemp = 0;
        }
    }

}
