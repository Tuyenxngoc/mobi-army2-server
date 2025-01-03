package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.Boss;
import com.teamobi.mobiarmy2.model.Bullet;
import com.teamobi.mobiarmy2.model.Player;

import java.util.List;

/**
 * @author tuyen
 */
public interface IBulletManager {

    List<Boss> getAddBosses();

    void decreaseSpiderWebCount();

    void addShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot);

    void fillXY();

    List<Bullet> getBullets();

    byte getMgtAddX();

    byte getMgtAddY();

    byte getTypeSC();

    short getXSC();

    short getYSC();

}
