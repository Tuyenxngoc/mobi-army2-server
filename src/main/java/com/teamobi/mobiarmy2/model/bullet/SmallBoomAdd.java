package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.IFightMapManager;
import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.model.boss.SmallBoom;

public class SmallBoomAdd extends Bullet {

    public SmallBoomAdd(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.isCanCollision = false;
        super.isXuyenPlayer = true;
    }

    @Override
    public void nextXY() {
        super.nextXY();
        IFightManager fightManager = bulletManager.getFightManager();
        IFightMapManager mapManager = fightManager.getMapManger();
        if (super.collect && X > 0 && X < mapManager.getWidth() && Y < mapManager.getHeight()) {
            Boss smallBoom = new SmallBoom(fightManager, (byte) fightManager.getTotalPlayers(), X, Y, (short) 1000);
            bulletManager.getAddBosses().add(smallBoom);
        }
    }
}
