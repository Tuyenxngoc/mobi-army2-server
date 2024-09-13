package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.fight.impl.FightManager;
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
    private FightManager fightManager;
    private BulletManager bulletManager;
    private boolean collect;
    private byte bullId;
    private int damage;
    private short X;
    private short Y;
    private short lastX;
    private short lastY;
    private short vx;
    private short vy;
    private short ax100;
    private short ay100;
    private short g100;
    private short vxTemp;
    private short vyTemp;
    private short vyTemp2;
    private boolean isMaxY;
    private short XmaxY;
    private short maxY;
    private short frame;
    private byte typeSC;
    private Player pl;
    private boolean isXuyenPlayer;
    private boolean isXuyenMap;
    private boolean isCanCollision;
    private List<Short> XArray;
    private List<Short> YArray;

    public Bullet(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        this.fightManager = bulletManager.getFightManager();
        this.bulletManager = bulletManager;
        this.bullId = bullId;
        this.damage = (damage * pl.getDamage()) / 100;
        this.pl = pl;
        this.X = (short) X;
        this.Y = (short) Y;
        this.lastX = (short) X;
        this.lastY = (short) Y;
        this.vx = (short) vx;
        this.vy = (short) vy;
        this.ax100 = (short) (bulletManager.getFightManager().getWindX() * msg / 100);
        this.ay100 = (short) (bulletManager.getFightManager().getWindY() * msg / 100);
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
        this.XArray = new ArrayList<>();
        this.YArray = new ArrayList<>();
        this.isXuyenPlayer = false;
        this.isXuyenMap = false;
        this.isCanCollision = true;
    }

    public void nextXY() {
        frame++;
        this.XArray.add(X);
        this.YArray.add(Y);
        if ((X < -200) || (X > fightManager.getMapManger().getWidth() + 200) || (Y > fightManager.getMapManger().getHeight() + 200)) {
            collect = true;
            return;
        }
        short preX = X, preY = Y;
        X += vx;
        lastX = X;
        Y += vy;
        lastY = Y;
        short[] collisionPoint = bulletManager.getCollisionPoint(preX, preY, X, Y, isXuyenPlayer, isXuyenMap);
        if (collisionPoint != null) {
            collect = true;
            X = collisionPoint[0];
            Y = collisionPoint[1];
            XArray.add(X);
            YArray.add(Y);
            if (pl.getUsedItemId() == -1 && !pl.isUsePow()) {
                if (this.isMaxY) {
                    if (this.Y - this.maxY > 350 && this.Y - this.maxY < 450) {
                        this.typeSC = 1;
                    } else if (this.Y - this.maxY >= 450) {
                        this.typeSC = 2;
                    }
                }
                if ((pl.getGunId() == 2 || pl.getGunId() == 3) && (Math.abs(lastX - XArray.get(0)) > 375)) {
                    this.typeSC = 4;
                }
            }
            if (this.isCanCollision) {
                fightManager.getMapManger().collision(X, Y, this);
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
            XmaxY = X;
            maxY = Y;
        }
        if (this.bulletManager.isHasVoiRong()) {
            for (BulletManager.VoiRong vr : this.bulletManager.getVoiRongs()) {
                if (this.X >= vr.X - 5 && this.X <= vr.X + 10) {
                    this.vx -= 2;
                    this.vy -= 2;
                    break;
                }
            }
        }
    }

    public static int getImpactRadiusByBullId(int bullId) {
        return switch (bullId) {
            case 0 -> 21;
            case 1, 11, 17, 18, 19, 21, 26 -> 13;
            case 2, 9, 20, 24, 48, 49 -> 18;
            case 3 -> 100;
            case 6, 8 -> 22;
            case 7, 14, 22, 40, 41, 50, 51, 54, 55 -> 30;
            case 10, 16, 23 -> 19;
            case 12, 29, 52 -> 20;
            case 15, 45 -> 28;
            case 25 -> 8;
            case 27, 44 -> 11;
            case 30, 59 -> 16;
            case 31 -> 40;
            case 32, 35 -> 50;
            case 33 -> 25;
            case 37 -> 150;
            case 42, 43 -> 32;
            case 47 -> 7;
            case 57 -> 70;
            default -> 0;
        };
    }
}
