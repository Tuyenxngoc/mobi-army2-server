package com.teamobi.mobiarmy2.fight.bullet;

import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.fight.boss.VenomousSpider;
import com.teamobi.mobiarmy2.server.EffectManager;

public class SpiderDropSilk extends Bullet {
    public SpiderDropSilk(BulletManager bulletManager, int damage, Player player) {
        super(bulletManager, (byte) 8, damage, player, player.getX(), player.getY() + 22, 0, 0, 0, 0);
    }

    @Override
    public void update() {
        isCollected = true;
        trajectory.add(new Point(x, y));
        y += 2;
        trajectory.add(new Point(x, y));

        // Cập nhật lại tọa độ của người chơi mục tiêu
        VenomousSpider boss = (VenomousSpider) player;
        boss.getTargetPlayer().setXY(x, y);
        ((IFightManager) bulletManager.getFightManager()).sendUpdateCoordinates(boss.getTargetPlayer().getIndex());

        // Thêm mạng nhện vào bản đồ
        bulletManager.getFightMapManager().addNewTiles(new MapTile(-1, (short) (x - 21), (short) (y - 20), EffectManager.spiderWebData, true));
    }
}