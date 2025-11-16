package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemChuotGanBom extends Bullet {
    private final int nStep;
    private final boolean addX;
    private int nYRoi;

    public ItemChuotGanBom(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, byte force, boolean addX) {
        super(bullMNG, bullId, satThuong, pl, addX ? (X + 1) : (X - 1), Y, 0, 0, 0, 0);
        this.nStep = force * 3;
        this.addX = addX;
        this.nYRoi = 0;
    }

    @Override
    public void update() {
        nYRoi++;
        for (int i = 0; i < nYRoi; i++) {
            if (bulletManager.getFightManager().getMapManger().isCollision(x, y)) {
                nYRoi = 0;
                break;
            }
            y++;
        }
        byte step = 4;
        if (addX) {
            x += step;
        } else {
            x -= step;
        }
        if (bulletManager.getFightManager().getMapManger().isCollision(x, (short) (y - 5))) {
            if (addX) {
                x -= step;
            } else {
                x += step;
            }
        } else {
            for (int i = 4; i >= 0; i--) {
                if (bulletManager.getFightManager().getMapManger().isCollision(x, (short) (y - i))) {
                    y -= i;
                    break;
                }
            }
        }
        if (this.y > bulletManager.getFightManager().getMapManger().getHeight() + 100) {
            XArray.add(x);
            YArray.add(y);
            this.isCollected = true;
            return;
        }
        XArray.add(x);
        YArray.add(y);
        if (super.frame == nStep) {
            super.isCollected = true;
            bulletManager.getFightManager().getMapManger().collision(x, y, this);
            return;
        }
        super.frame++;
    }
}
