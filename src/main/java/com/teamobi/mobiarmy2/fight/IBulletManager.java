package com.teamobi.mobiarmy2.fight;

import java.util.List;

/**
 * @author tuyen
 */
public interface IBulletManager {

    void refresh();

    void addShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot);

    void fillXY();

    List<Bullet> getBullets();

    byte getMgtAddX();

    byte getMgtAddY();

    byte getTypeSC();

    short getXSC();

    short getYSC();

}
