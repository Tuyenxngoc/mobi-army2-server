package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.IBulletManager;
import com.teamobi.mobiarmy2.fight.IFightManager;
import com.teamobi.mobiarmy2.fight.Player;
import com.teamobi.mobiarmy2.fight.item.VoiRong;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class BulletManager implements IBulletManager {

    private final IFightManager fightManager;
    private final List<Bullet> bullets;
    private byte mgtAddX;
    private byte mgtAddY;
    private byte typeSC;
    private short XSC;
    private short YSC;
    private short arg;

    public BulletManager(IFightManager fightManager) {
        this.fightManager = fightManager;
        this.bullets = new ArrayList<>();
    }

    @Override
    public IFightManager getFightManager() {
        return fightManager;
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
        if (numShoot > 2 || numShoot < 1) {
            return;
        }

        if (bullId == 49) {
            force += 5;
        }

        typeSC = 0;
        int x = player.getX() + (20 * Utils.cos(angle) >> 10);
        int y = player.getY() - 12 - (20 * Utils.sin(angle) >> 10);
        int vx = (force * Utils.cos(angle) >> 10);
        int vy = -(force * Utils.sin(angle) >> 10);

        byte characterId = player.getCharacterId();
        if (characterId == 13) {
            y -= 25;
        }

        for (int k = 0; k < numShoot; k++) {
            switch (bullId) {
                case 0 -> {
                    if (player.getUsedItemId() > 0 || (characterId != 0 && characterId != 14)) {
                        return;
                    }
                    bullets.add(new Bullet(this, 0, (player.isUsePow() ? 630 : (numShoot == 2 ? 210 : 280)), player, x, y, vx, vy, 80, 100));
                }
                case 1 -> {
                    if (player.getUsedItemId() > 0 || (characterId != 1)) {
                        return;
                    }
                    int n = player.isUsePow() ? 6 : 2;
                    for (int i = 0; i < n; i++) {
                        bullets.add(new Bullet(this, 1, numShoot == 2 ? 109 : 145, player, x, y, vx, vy, 50, 50));
                    }
                }
            }
        }
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

    @Override
    public short[] getCollisionPoint(short preY, short preX, short x, short y, boolean isXuyenPlayer, boolean isXuyenMap) {
        return null;
    }

    @Override
    public boolean hasVoiRong() {
        return false;
    }

    @Override
    public List<VoiRong> getVoiRongs() {
        return List.of();
    }

}
