package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemChuotGanBom extends Bullet {

    private final int nStep;
    private final boolean addX;
    private int nYRoi;

    public ItemChuotGanBom(BulletManager bullMNG, byte bullId, long satThuong, Player pl, int X, int Y, byte force, boolean addX) {
        super(bullMNG, bullId, satThuong, pl, addX ? (X + 1) : (X - 1), Y, 0, 0, 0, 0);
        this.nStep = force * 3;
        this.addX = addX;
        this.nYRoi = 0;
    }

    @Override
    public void nextXY() {
        nYRoi++;
        for (int i = 0; i < nYRoi; i++) {
            if (this.fightManager.mapManager.isCollision(X, Y)) {
                nYRoi = 0;
                break;
            }
            Y++;
        }
        byte step = 4;
        if (addX) {
            X += step;
        } else {
            X -= step;
        }
        if (this.fightManager.mapManager.isCollision(X, (short) (Y - 5))) {
            if (addX) {
                X -= step;
            } else {
                X += step;
            }
        } else {
            for (int i = 4; i >= 0; i--) {
                if (this.fightManager.mapManager.isCollision(X, (short) (Y - i))) {
                    Y -= i;
                    break;
                }
            }
        }
        if (this.Y > this.fightManager.mapManager.getHeight() + 100) {
            XArray.add((short) X);
            YArray.add((short) Y);
            this.collect = true;
            return;
        }
        XArray.add((short) X);
        YArray.add((short) Y);
        if (super.frame == nStep) {
            super.collect = true;
            fightManager.mapManager.handleCollision(X, Y, this);
            return;
        }
        super.frame++;
    }

}
