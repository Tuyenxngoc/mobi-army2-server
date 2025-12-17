package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.util.Utils;

public class ApaBullet extends Bullet {
    private int baseDamage;
    private short angle;
    private byte force;
    private byte force2;

    public ApaBullet(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, short angle, byte force, byte force2) {
        super(bulletManager, (byte) 17, damage, player, x, y, vx, vy, 30, 100);
        this.baseDamage = damage;
        this.angle = angle;
        this.force = force;
        this.force2 = force2;
    }

    @Override
    public void update() {
        super.update();

        if (frame == force2) {
            int lastX = trajectory.getLast().getX();
            int lastY = trajectory.getLast().getY();

            int dx = player.getX() - lastX;
            int dy = player.getY() - lastY;
            int arg = angle + Utils.normalizeAngle360(Utils.getArg(dx, dy));

            if (angle < 90) {
                arg = 180 - arg;
            }

            arg = arg - 15;// Bắt đầu lệch 15° cho viên đầu tiên

            // Tạo 3 viên đạn con, lệch nhau 15°
            for (int i = 0; i < 3; i++, arg += 15) {
                int bulletX = lastX + (20 * Utils.cos(arg) >> 10);
                int bulletY = lastY - 12 - (20 * Utils.sin(arg) >> 10);

                int vxn = (force * Utils.cos(arg) >> 11);
                int vyn = -(force * Utils.sin(arg) >> 11);

                bulletManager.addBullet(new Bullet(bulletManager, (byte) 18, baseDamage, player, bulletX, bulletY, vxn, vyn, 30, 100));
            }

            isCollected = true;// Viên đạn mẹ biến mất
        }
    }
}