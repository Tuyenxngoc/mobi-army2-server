package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.IFightMapManager;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;
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
    protected BulletManager bulletManager;
    protected boolean collect;
    protected byte bullId;
    protected int damage;
    protected short X;
    protected short Y;
    protected short lastX;
    protected short lastY;
    protected short vx;
    protected short vy;
    protected short ax100;
    protected short ay100;
    protected short g100;
    protected short vxTemp;
    protected short vyTemp;
    protected short vyTemp2;
    protected boolean isMaxY;
    protected short XmaxY;
    protected short maxY;
    protected short frame;
    protected byte typeSC;
    protected Player pl;
    protected boolean isXuyenPlayer;
    protected boolean isXuyenMap;
    protected boolean isCanCollision;
    protected List<Short> XArray;
    protected List<Short> YArray;

    public Bullet(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
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
        this.XArray = new ArrayList<>();
        this.YArray = new ArrayList<>();
        this.isXuyenPlayer = false;
        this.isXuyenMap = false;
        this.isCanCollision = true;
    }

    public void nextXY() {
        IFightManager fightManager = bulletManager.getFightManager();
        IFightMapManager mapManager = fightManager.getMapManger();

        frame++;
        this.XArray.add(X);
        this.YArray.add(Y);
        if ((X < -200) || (X > mapManager.getWidth() + 200) || (Y > mapManager.getHeight() + 200)) {
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
                if ((pl.getGunId() == 2 || pl.getGunId() == 3) && (Math.abs(lastX - XArray.getFirst()) > 375)) {
                    this.typeSC = 4;
                }
            }
            if (this.isCanCollision) {
                mapManager.collision(X, Y, this);
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
