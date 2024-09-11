package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.fight.item.VoiRong;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class Bullet {
    private byte bulletId;
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
    private short vxTemp;
    private short vyTemp;
    private short vyTemp2;
    private boolean isMaxY;
    private short XmaxY;
    private short maxY;
    private byte typeSC;
    private boolean isXuyenPlayer;
    private boolean isXuyenMap;
    private boolean isCanCollision;
    private final List<Short> xArrays = new ArrayList<>();
    private final List<Short> yArrays = new ArrayList<>();
    private final IBulletManager bulletManager;
    private final Player player;

    public Bullet(IBulletManager bulletManager, int bulletId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        this.bulletManager = bulletManager;
        this.bulletId = (byte) bulletId;
        this.damage = (short) ((damage * player.getDamage()) / 100);
        this.player = player;
        this.x = (short) x;
        this.y = (short) y;
        this.lastX = this.x;
        this.lastY = this.y;
        this.vx = (short) vx;
        this.vy = (short) vy;
        IFightManager fightManager = bulletManager.getFightManager();
        this.ax100 = (short) (fightManager.getWindX() * msg / 100);
        this.ay100 = (short) (fightManager.getWindY() * msg / 100);
        this.g100 = (short) g100;
        this.vxTemp = 0;
        this.vyTemp = 0;
        this.vyTemp2 = 0;
        this.collect = false;
        this.isMaxY = false;
        this.XmaxY = -1;
        this.maxY = -1;
        this.frame = 0;
        this.typeSC = 0;
        this.isXuyenPlayer = false;
        this.isXuyenMap = false;
        this.isCanCollision = true;
    }

    public void nextXY() {
        IFightManager fightManager = bulletManager.getFightManager();
        IMapManager mapManager = fightManager.getMapManger();

        frame++;
        xArrays.add(x);
        yArrays.add(y);
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
        short[] points = bulletManager.getCollisionPoint(preY, preX, x, y, isXuyenPlayer, isXuyenMap);
        if (points != null) {
            collect = true;
            x = points[0];
            y = points[1];
            xArrays.add(x);
            yArrays.add(y);
            if (player.getUsedItemId() == -1 && !player.isUsePow()) {
                if (this.isMaxY) {
                    if (this.y - this.maxY > 350 && this.y - this.maxY < 450) {
                        this.typeSC = 1;
                    } else if (this.y - this.maxY >= 450) {
                        this.typeSC = 2;
                    }
                }
                if ((player.getGunId() == 2 || player.getGunId() == 3) && (Math.abs(lastX - xArrays.get(0)) > 375)) {
                    this.typeSC = 4;
                }
            }
            if (this.isCanCollision) {
                mapManager.collision(x, y, this);
            }
            return;
        }
        vxTemp += Math.abs(ax100);
        vyTemp += Math.abs(ay100);
        vyTemp2 += g100;
        if (Math.abs(vxTemp) >= 100) {
            if (ax100 > 0) {
                vx += vxTemp / 100;
            } else {
                vx -= vxTemp / 100;
            }
            vxTemp %= 100;
        }
        if (Math.abs(vyTemp) >= 100) {
            if (ay100 > 0) {
                vy += vyTemp / 100;
            } else {
                vy -= vyTemp / 100;
            }
            vyTemp %= 100;
        }
        if (Math.abs(vyTemp2) >= 100) {
            vy += vyTemp2 / 100;
            vyTemp2 %= 100;
        }
        if (vy > 0 && !isMaxY) {
            isMaxY = true;
            XmaxY = x;
            maxY = y;
        }
        if (bulletManager.hasVoiRong()) {
            for (VoiRong vr : bulletManager.getVoiRongs()) {
                if (this.x >= vr.X - 5 && this.x <= vr.X + 10) {
                    this.vx -= 2;
                    this.vy -= 2;
                    break;
                }
            }
        }
    }

    public static boolean isFlagBull(int bulletId) {
        return bulletId == 4 || bulletId == 14 || bulletId == 16 || bulletId == 23 || bulletId == 28;
    }

    public static boolean isDoubleBull(int bulletId) {
        return bulletId == 17 || bulletId == 19;
    }

    public static byte getHoleByBulletId(int bulletId) {
        return 0;
    }

    public static int getImpactRangeByBulletId(int bulletId) {
        return 0;
    }

}
