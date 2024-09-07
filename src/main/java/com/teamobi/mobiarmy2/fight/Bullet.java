package com.teamobi.mobiarmy2.fight;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class Bullet {

    private boolean collect;
    private short x;
    private short y;
    private short lastX;
    private short lastY;
    private short vx;
    private short vy;
    private short ax100;
    private short ay100;
    private short g100;
    private short frame;
    private short damage;

    private final List<Short> xArrays = new ArrayList<>();
    private final List<Short> yArrays = new ArrayList<>();

    private final IBulletManager bulletManager;

    public Bullet(IBulletManager bulletManager) {
        this.bulletManager = bulletManager;
    }

    protected void nextXY() {
        frame++;
        xArrays.add(x);
        yArrays.add(y);

        IFightManager fightManager = bulletManager.getFightManager();
        IMapManager mapManager = fightManager.getMapManger();

        if ((x < -200) || (x > mapManager.getWidth() + 200) || (y > mapManager.getHeight() + 200)) {
            collect = true;
            return;
        }

        short preX = x;
        short preY = y;
        x += vx;
        lastX = x;
        y += vy;
        lastY = y;

        short[] points = bulletManager.getCollisionPoint();
        if (points != null) {
            collect = true;
        }
    }

    public static boolean isFlagBull(int bulletId) {
        return bulletId == 4 || bulletId == 14 || bulletId == 16 || bulletId == 23 || bulletId == 28;
    }

    public static boolean isDoubleBull(int bulletId) {
        return bulletId == 17 || bulletId == 19;
    }

}
