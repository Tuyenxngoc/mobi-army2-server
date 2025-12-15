package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.*;

// Item chuột gắn bom
public class BombMouseBullet extends Bullet {
    private final int lifeTime;
    private final boolean moveRight;
    private int fallSpeed;

    public BombMouseBullet(BulletManager bullMNG, int satThuong, Player pl, int x, int y, byte force, boolean moveRight) {
        super(bullMNG, (byte) 22, satThuong, pl, moveRight ? (x + 1) : (x - 1), y, 0, 0, 0, 0);
        this.lifeTime = force * 3;
        this.moveRight = moveRight;
        this.fallSpeed = 0;
    }

    @Override
    public void update() {
        fallSpeed++;
        FightMapManager fightMapManager = bulletManager.getFightMapManager();
        for (int i = 0; i < fallSpeed; i++) {
            if (fightMapManager.isCollision(x, y)) {
                fallSpeed = 0;
                break;
            }
            y++;
        }
        byte step = 4;
        if (moveRight) {
            x += step;
        } else {
            x -= step;
        }
        if (fightMapManager.isCollision(x, (short) (y - 5))) {
            if (moveRight) {
                x -= step;
            } else {
                x += step;
            }
        } else {
            for (short i = 4; i >= 0; i--) {
                if (fightMapManager.isCollision(x, (short) (y - i))) {
                    y -= i;
                    break;
                }
            }
        }
        if (y > fightMapManager.getHeight() + 100) {
            trajectory.add(new Point(x, y));
            isCollected = true;
            return;
        }
        trajectory.add(new Point(x, y));
        if (frame == lifeTime) {
            isCollected = true;
            bulletManager.handleCollision(x, y, this);
            return;
        }
        frame++;
    }

}