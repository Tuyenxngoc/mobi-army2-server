package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulletManager {
    private FightManager fightManager;
    private List<Bullet> bullets = new ArrayList<>();
    private byte typeShoot;
    private byte superType;
    private short superX;
    private short superY;

    public BulletManager(FightManager fightManager) {
        this.fightManager = fightManager;
    }

    public void addShoot(Player pl, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        //Tính vị trí bắt đầu của viên đạn
        int x = pl.getX() + (20 * Utils.cos(angle) >> 10);
        int y = pl.getY() - 12 - (20 * Utils.sin(angle) >> 10);

        // Tính vận tốc ban đầu của viên đạn
        int vx = (force * Utils.cos(angle) >> 10);
        int vy = -(force * Utils.sin(angle) >> 10);

        int characterId = pl.getCharacterId();
        for (int k = 0; k < numShoot; k++) {
            switch (bullId) {
                case 0 -> {//Gunner
                    if (pl.getUsedItemId() > 0 || (characterId != 0 && characterId != 14)) {
                        return;
                    }
                    bullets.add(new Bullet(this, (byte) 0, (pl.isUsePow() ? 630 : (numShoot == 2 ? 210 : 280)), pl, x, y, vx, vy, 80, 100));
                }
            }
        }
    }

    public void updateBullets() {
        boolean hasNext;
        do {
            hasNext = false;
            for (int i = 0; i < bullets.size(); i++) {
                Bullet bullet = bullets.get(i);
                if (bullet == null || bullet.isCollected()) {
                    continue;
                }
                hasNext = true;
                bullet.update();
            }
        } while (hasNext);
    }

    public short[] getCollisionPoint(short x1, short y1, short x2, short y2, boolean canPassThroughPlayers, boolean canPassThroughMap) {
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
                if (fightManager.getMapManger().isCollision(X, Y)) {
                    return new short[]{X, Y, 0};
                }
            }

            // Check player collision
            if (!canPassThroughPlayers) {
                for (int j = 0; j < fightManager.getTotalPlayers(); j++) {
                    Player pl = fightManager.getPlayers()[j];
                    if (pl != null && pl.isCollision(X, Y)) {
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

    public void handleCollision(Bullet bullet) {
        fightManager.getMapManger().collision(bullet.getX(), bullet.getY(), bullet);
        fightManager.collisionPlayers(bullet.getX(), bullet.getY(), bullet);
    }

}
