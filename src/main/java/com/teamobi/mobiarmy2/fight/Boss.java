package com.teamobi.mobiarmy2.fight;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public abstract class Boss extends Player {

    private String name;

    public Boss(IFightManager fightManager, byte index, byte characterId, String name, short x, short y, short maxHp) {
        super(fightManager, index, characterId, x, y, maxHp);
        this.name = name;
    }

    public abstract void turnAction();

}
