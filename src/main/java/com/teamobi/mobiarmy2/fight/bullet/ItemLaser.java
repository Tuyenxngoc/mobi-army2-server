package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class ItemLaser extends Bullet {
    public ItemLaser(BulletManager bulletManager, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, (byte) 14, damage, player, x, y, vx, vy, msg, g100);
    }
}