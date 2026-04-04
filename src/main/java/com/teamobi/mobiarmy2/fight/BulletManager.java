package com.teamobi.mobiarmy2.fight;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulletManager {
    private IFightManager fightManager;
    private FightMapManager fightMapManager;
    private List<Bullet> bullets = new ArrayList<>();
    private byte typeShoot;
    private byte superType;
    private short superX;
    private short superY;

    public BulletManager(IFightManager fightManager) {
        this.fightManager = fightManager;
        this.fightMapManager = fightManager.getFightMapManager();
    }

    public void addShoot(Player pl, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        List<Bullet> newBullets = BulletFactory.createBullets(this, pl, bullId, angle, force, force2, numShoot);
        bullets.addAll(newBullets);
    }

    public void resetBullets() {
        bullets.clear();
        typeShoot = 0;
        superType = 0;
        superX = 0;
        superY = 0;
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    public void updateBullets() {
        boolean updated;
        do {
            updated = false;
            for (int i = 0; i < bullets.size(); i++) {
                Bullet bullet = bullets.get(i);
                if (bullet == null || bullet.isCollected()) {
                    continue;
                }
                bullet.update();
                updated = true;
            }
        } while (updated);
    }

    public short[] getCollisionPoint(Bullet bullet, short x1, short y1, short x2, short y2, boolean canPassThroughPlayers, boolean canPassThroughMap) {
        int deltaX = x2 - x1;
        int deltaY = y2 - y1;

        byte x_unit = 0;
        byte y_unit = 0;
        byte x_unit2 = 0;
        byte y_unit2 = 0;

        if (deltaX < 0) {
            x_unit = x_unit2 = -1;
        } else if (deltaX > 0) {
            x_unit = x_unit2 = 1;
        }
        if (deltaY < 0) {
            y_unit = y_unit2 = -1;
        } else if (deltaY > 0) {
            y_unit = y_unit2 = 1;
        }

        int k1 = Math.abs(deltaX);
        int k2 = Math.abs(deltaY);
        if (k1 > k2) {
            y_unit2 = 0;
        } else {
            k1 = Math.abs(deltaY);
            k2 = Math.abs(deltaX);
            x_unit2 = 0;
        }
        int k = k1 >> 1;
        short X = x1, Y = y1;

        for (int i = 0; i <= k1; i++) {
            // Check map collision
            if (!canPassThroughMap) {
                if (fightMapManager.isCollision(X, Y)) {
                    return new short[]{X, Y, 0};
                }
            }

            // Check player collision
            if (!canPassThroughPlayers) {
                for (int j = 0; j < fightManager.getTotalPlayers(); j++) {
                    Player target = fightManager.getPlayers()[j];

                    if (target == null) continue;

                    if (!target.shouldCollide() || !target.shouldCollideWith(bullet)) continue;

                    if (target.isCollision(X, Y)) {
                        return new short[]{X, Y, 1};
                    }
                }
            }

            k += k2;
            if (k >= k1) {
                k -= k1;
                X += x_unit;
                Y += y_unit;
            } else {
                X += x_unit2;
                Y += y_unit2;
            }
        }
        return null;
    }

    public void handleCollision(short x, short y, Bullet bullet) {
        fightMapManager.collision(x, y, bullet);
        fightManager.collisionPlayers(x, y, bullet);
    }

}
