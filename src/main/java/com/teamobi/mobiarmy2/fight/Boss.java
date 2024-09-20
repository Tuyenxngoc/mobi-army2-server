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

    public Boss(IFightManager fightManager, byte index, byte characterId, String name, short x, short y, short width, short height, short maxHp, int xpExist) {
        super(fightManager, index, characterId, x, y, width, height, maxHp, xpExist);
        this.name = name;
    }

    public abstract void turnAction();

}
