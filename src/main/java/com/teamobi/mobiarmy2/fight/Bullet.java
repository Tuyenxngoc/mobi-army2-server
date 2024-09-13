package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.fight.impl.FightManager;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * @author tuyen
 */
@Getter
@Setter
public class Bullet {
    protected FightManager fightManager;
    protected BulletManager bulletManager;
    protected boolean collect;
    protected byte bullId;
    protected int damage;
    public short X;
    public short Y;
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
    public ArrayList<Short> XArray;
    public ArrayList<Short> YArray;

    public Bullet(BulletManager bulletManager, byte bullId, int damage, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        this.fightManager = bulletManager.fightManager;
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
        this.ax100 = (short) (bulletManager.fightManager.getWindX() * msg / 100);
        this.ay100 = (short) (bulletManager.fightManager.getWindY() * msg / 100);
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
        if (this.bulletManager.hasVoiRong) {
            for (BulletManager.VoiRong vr : this.bulletManager.voiRongs) {
                if (this.X >= vr.X - 5 && this.X <= vr.X + 10) {
                    this.vx -= 2;
                    this.vy -= 2;
                    break;
                }
            }
        }
    }
}
