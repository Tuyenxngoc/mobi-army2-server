package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemBomMu extends Bullet {
    public ItemBomMu(BulletManager bulletManager, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 51, 0, player, x, y, vx, vy, 5, 60);
        this.canSuperType = false;
        this.canCollide = false;
    }

    @Override
    public void update() {
        super.update();
        if (isCollected) {
            ((IFightManager) bulletManager.getFightManager()).onBulletExplode(x, y, this);
        }
    }
}