package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.model.User;
import lombok.Getter;

/**
 * @author tuyen
 */
@Getter
public class Player {

    private final User user;
    private short x;
    private short y;
    private short maxHp;
    private short hp;

    public Player(User user) {
        this.user = user;
    }

}
