package com.teamobi.mobiarmy2.model.bullet;

import com.teamobi.mobiarmy2.fight.impl.BulletManager;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.MapTile;
import com.teamobi.mobiarmy2.model.Player;
import com.teamobi.mobiarmy2.model.boss.VenomousSpider;
import com.teamobi.mobiarmy2.server.EffectManager;

public class SpiderDropSilk extends Bullet {

    public SpiderDropSilk(BulletManager bulletManager, byte bullId, int damage, Player pl) {
        super(bulletManager, bullId, damage, pl, pl.getX(), pl.getY() - 12, 0, 0, 0, 0);
    }

    @Override
    public void nextXY() {
        collect = true;
        XArray.add(X);
        YArray.add(Y);
        Y += 38;
        XArray.add(X);
        YArray.add(Y);

        //Cập nhật vị tri người chơi
        VenomousSpider spider = (VenomousSpider) pl;
        Player targetPlayer = spider.getTargetPlayer();
        targetPlayer.setY((short) (Y - 3));
        bulletManager.getFightManager().sendMessageUpdateXY(targetPlayer.getIndex());

        //Thêm mạng nhện
        bulletManager.getFightManager().getMapManger().addNewTiles(new MapTile(bulletManager.getMangNhenId(), (short) (X - 21), (short) (Y - 20), EffectManager.spiderWebData, true));
        bulletManager.decreaseSpiderWebCount();
    }
}
