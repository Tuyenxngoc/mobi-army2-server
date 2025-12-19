package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemXuyenDat extends Bullet {
    public ItemXuyenDat(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, byte force) {
        super(bulletManager, (byte) 25, damage, player, x, y, vx, vy, 0, -50);
    }
}