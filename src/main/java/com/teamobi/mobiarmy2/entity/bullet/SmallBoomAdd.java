package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Boss;
import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.entity.boss.SmallBoom;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.FightManager;
import com.teamobi.mobiarmy2.fight.FightMapManager;

public class SmallBoomAdd extends Bullet {
    public SmallBoomAdd(BulletManager bullMNG, byte bullId, int satThuong, Player pl, int X, int Y, int vx, int vy, int msg, int g100) {
        super(bullMNG, bullId, satThuong, pl, X, Y, vx, vy, msg, g100);
        super.canCollide = false;
        super.canPassThroughPlayers = true;
    }

    @Override
    public void update() {
        super.update();
        FightManager fightManager = bulletManager.getFightManager();
        FightMapManager mapManager = fightManager.getMapManger();
        if (super.isCollected && x > 0 && x < mapManager.getWidth() && y < mapManager.getHeight()) {
            Boss smallBoom = new SmallBoom(fightManager, (byte) fightManager.getTotalPlayers(), x, y, (short) 1000);
            bulletManager.getAddBosses().add(smallBoom);
        }
    }
}
