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
        this.canSuperType = false;
    }

    @Override
    public void update() {
        super.update();

        // Viên đạn đã đạt đỉnh khi vy >= 0
        if (vy >= 0) {
            isCollected = true; // Kết thúc viên đạn khi đạt đỉnh

            // Lưu lại tọa độ tại đỉnh
            trajectory.add(new Point(x, y));

            // Tính toán sau khi đạt đỉnh
            Point first = trajectory.getFirst();
            Point last = trajectory.getLast();

            int deltaX = last.getX() - first.getX();
            int deltaY = last.getY() - first.getY();

            int arg = Utils.getArg(deltaX, deltaY);
            deltaX = (force * Utils.cos(arg)) >> 10;
            deltaY = (force * Utils.sin(arg)) >> 10;

            short newX = (short) last.getX();
            short newY = (short) last.getY();

            while (true) {
                if ((newX < -100) || (newX > bulletManager.getFightManager().getMapManger().getWidth() + 100) || (newY > bulletManager.getFightManager().getMapManger().getHeight() + 100)) {
                    break;
                }
                short[] collisionResult = bulletManager.getCollisionPoint(newX, newY, (short) (newX + deltaX), (short) (newY - deltaY), false, false);
                if (collisionResult != null) {

                    newX = collisionResult[0];
                    newY = collisionResult[1];

                    if (canCollide) {
                        bulletManager.handleCollision(newX, newY, this);
                    }

                    break;
                }

                newX += deltaX;
                newY -= deltaY;
            }

            trajectory.add(new Point(newX, newY));

            dXLaser = (byte) deltaX;
            dYLaser = (byte) deltaY;
        }
    }
}