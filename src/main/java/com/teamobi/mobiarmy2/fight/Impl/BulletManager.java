package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.Bullet;
import com.teamobi.mobiarmy2.fight.IBulletManager;
import com.teamobi.mobiarmy2.fight.IFightManager;

import java.util.List;

public class BulletManager implements IBulletManager {

    private final IFightManager fightManager;
    private List<Bullet> bullets;

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

}
