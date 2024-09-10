package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.IBulletManager;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;

import java.util.List;

/**
 * @author tuyen
 */
public class BulletManager implements IBulletManager {

    private final IFightManager fightManager;
    private List<Bullet> bullets;
    public byte mgtAddX;
    public byte mgtAddY;
    public byte typeSC;
    public short XSC;
    public short YSC;
    public short arg;

    public BulletManager(IFightManager fightManager) {
        this.fightManager = fightManager;
    }

    @Override
    public IFightManager getFightManager() {
        return fightManager;
    }

    @Override
    public short[] getCollisionPoint() {
        return new short[0];
    }

    @Override
    public void updateBulletPositions() {
        boolean hasActiveBullet = false;
        do {
            for (Bullet bullet : bullets) {
                if (bullet == null || bullet.isCollect()) {
                    continue;
                }
                hasActiveBullet = true;
                bullet.nextXY();
            }
        } while (hasActiveBullet);
    }

    @Override
    public void addShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot) {
        typeSC = 0;

    }

    @Override
    public void clearBullets() {
        bullets.clear();
    }

    @Override
    public List<Bullet> getBullets() {
        return bullets;
    }

    @Override
    public byte getMgtAddX() {
        return mgtAddX;
    }

    @Override
    public byte getMgtAddY() {
        return mgtAddY;
    }

    @Override
    public byte getTypeSC() {
        return typeSC;
    }

    @Override
    public short getXSC() {
        return XSC;
    }

    @Override
    public short getYSC() {
        return YSC;
    }

    @Override
    public short getArg() {
        return arg;
    }
}
