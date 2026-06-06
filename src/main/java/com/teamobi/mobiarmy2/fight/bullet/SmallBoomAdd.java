package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.fight.boss.SmallBoom;

public class SmallBoomAdd extends Bullet {
    public SmallBoomAdd(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 34, damage, player, x, y, vx, vy, 0, 80);
        this.canCollide = false;
        this.canSuperType = false;
        this.canPassThroughPlayers = true;
    }

    @Override
    public void update() {
        super.update();

        // Kiểm tra vị trí hợp lệ để thêm SmallBoom
        FightMapManager map = bulletManager.getFightMapManager();
        if (isCollected && x > 0 && x < map.getWidth() && y < map.getHeight()) {
            IFightManager fightManager = (IFightManager) bulletManager.getFightManager();
            Boss boss = new SmallBoom(fightManager, x, y, (short) 1000);
            fightManager.addPendingBoss(boss);
        }
    }
}