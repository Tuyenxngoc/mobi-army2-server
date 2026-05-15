package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemDongBang extends Bullet {
    public ItemDongBang(BulletManager bulletManager, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 54, 0, player, x, y, vx, vy, 0, 80);
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