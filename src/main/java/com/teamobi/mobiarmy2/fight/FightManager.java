package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.service.IFightService;

public class FightManager {

    private final IFightService fightService;

    public FightManager(IFightService fightService) {
        this.fightService = fightService;
    }
}
