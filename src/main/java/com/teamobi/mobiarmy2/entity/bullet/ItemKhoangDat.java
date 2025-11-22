package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.fight.BulletManager;

public class ItemKhoangDat extends Bullet {
    public ItemKhoangDat(BulletManager bulletManager, byte bullId, int damage, Player player, int x, int y, int vx, int vy, int msg, int g100) {
        super(bulletManager, bullId, damage, player, x, y, vx, vy, msg, g100);
    }

    public ItemKhoangDat(BulletManager bulletManager, byte bullId, Player pl, short x, short y, byte force) {
        super();
    }
}