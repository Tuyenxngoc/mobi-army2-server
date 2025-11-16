package com.teamobi.mobiarmy2.entity.bullet;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.MapTile;
import com.teamobi.mobiarmy2.entity.Player;
import com.teamobi.mobiarmy2.entity.boss.VenomousSpider;
import com.teamobi.mobiarmy2.fight.BulletManager;
import com.teamobi.mobiarmy2.server.EffectManager;

public class SpiderDropSilk extends Bullet {
    public SpiderDropSilk(BulletManager bulletManager, byte bullId, int damage, Player pl) {
        super(bulletManager, bullId, damage, pl, pl.getX(), pl.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        isCollected = true;
        XArray.add(x);
        YArray.add(y);
        y += 38;
        XArray.add(x);
        YArray.add(y);

        //Cập nhật vị tri người chơi
        VenomousSpider spider = (VenomousSpider) player;
        Player targetPlayer = spider.getTargetPlayer();
        targetPlayer.setY((short) (y - 3));
        bulletManager.getFightManager().sendMessageUpdateXY(targetPlayer.getIndex());

        //Thêm mạng nhện
        bulletManager.getFightManager().getMapManger().addNewTiles(new MapTile(bulletManager.getMangNhenId(), (short) (x - 21), (short) (y - 20), EffectManager.spiderWebData, true));
        bulletManager.decreaseSpiderWebCount();
    }
}
