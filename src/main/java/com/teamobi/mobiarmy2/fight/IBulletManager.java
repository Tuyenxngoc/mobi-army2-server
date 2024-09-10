package com.teamobi.mobiarmy2.fight;

import java.util.List;

/**
 * @author tuyen
 */
public interface IBulletManager {

    IFightManager getFightManager();

    void updateBulletPositions();

    void addShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot);

    void clearBullets();

    List<Bullet> getBullets();

    byte getMgtAddX();

    byte getMgtAddY();

    byte getTypeSC();

    short getXSC();

    short getYSC();

    short getArg();

    short[] getCollisionPoint(short preY, short preX, short x, short y, boolean isXuyenPlayer, boolean isXuyenMap);
}
