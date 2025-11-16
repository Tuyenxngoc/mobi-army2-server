package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class MGTBulletOld extends Bullet {
    private final int force;

    public MGTBulletOld(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx100, int vy100, int force) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx100, vy100, 0, 0);
        this.force = force;
    }

    @Override
    public void update() {
        frame++;
        XArray.add(x);
        YArray.add(y);
        if ((x < -200) || (x > bulletManager.getFightManager().getMapManger().getWidth() + 200) || (y > bulletManager.getFightManager().getMapManger().getHeight() + 200)) {
            isCollected = true;
            return;
        }
        short preX = x, preY = y;
        vxTemp += Math.abs(vx);
        vyTemp += Math.abs(vy);
        if (Math.abs(vxTemp) >= 100) {
            if (vx > 0) {
                x += vxTemp / 100;
            } else {
                x -= vxTemp / 100;
            }
            vxTemp %= 100;
        }
        if (Math.abs(vyTemp) >= 100) {
            if (vy > 0) {
                y += vyTemp / 100;
            } else {
                y -= vyTemp / 100;
            }
            vyTemp %= 100;
        }
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
            return;
        }
        if (frame == force) {
            vy = (short) (-vy);
            vxTemp = 0;
            vyTemp = 0;
        }
    }
}
