package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.fight.Player;

public class BigRocKet extends Bullet {
    public BigRocKet(BulletManager bulletManager, int damage, Player player) {
        super(bulletManager, (byte) 37, damage, player, player.getX() - 20, player.getY() - 120, 0, 0, 0, 0);
    }
}