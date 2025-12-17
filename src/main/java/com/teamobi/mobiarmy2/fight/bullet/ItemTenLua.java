package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemTenLua extends Bullet {
    public ItemTenLua(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, byte force) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, 30, 60);

    }
}