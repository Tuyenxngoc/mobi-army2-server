package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.ICountdownTimer;
import com.teamobi.mobiarmy2.fight.IFightManager;

/**
 * @author tuyen
 */
public class CountdownTimer implements ICountdownTimer {

    private final IFightManager fightManager;

    public CountdownTimer(IFightManager fightManager) {
        this.fightManager = fightManager;
    }
}
