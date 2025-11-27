package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.entity.Point;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.util.Utils;

public class MagentaBulletNew extends Bullet {
    private final int force;

    public MagentaBulletNew(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100, byte force) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, msg, g100);
        this.force = force;
    }

    @Override
    public void update() {
        super.update();
        if (isMaxY) {
            // Lưu lại tọa độ đỉnh vì tọa độ chỉ lưu vào đầu hàm update
            trajectory.add(new Point(x, y));
            isCollected = true;

            int deltaX = xAtPeakY - trajectory.getFirst().getX();
            int deltaY = peakY - trajectory.getFirst().getY();

            int arg = Utils.getArg(deltaX, deltaY);

            deltaX = (force * Utils.cos(arg)) >> 10;
            deltaY = (force * Utils.sin(arg)) >> 10;

            short newX = xAtPeakY;
            short newY = peakY;

            while (true) {
                if ((newX < -100) || (newX > bulletManager.getFightManager().getMapManger().getWidth() + 100) || (newY > bulletManager.getFightManager().getMapManger().getHeight() + 100)) {
                    break;
                }
                short[] collisionResult = bulletManager.getCollisionPoint(newX, newY, (short) (newX + deltaX), (short) (newY - deltaY), false, false);
                if (collisionResult != null) {

                    newX = collisionResult[0];
                    newY = collisionResult[1];

                    if (canCollide) {
                        bulletManager.handleCollision(this);
                    }

                    break;
                }

                newX += (short) deltaX;
                newY -= (short) deltaY;
            }

            trajectory.add(new Point(newX, newY));

            dXLaser = (byte) deltaX;
            dYLaser = (byte) deltaY;
        }
    }
}