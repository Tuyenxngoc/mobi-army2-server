package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemBomMu extends Bullet {
    public ItemBomMu(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy) {
        super(bulletManager, (byte) 51, damage, player, x, y, vx, vy, 5, 60);
    }
}