package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.model.User;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class Player {

    private final User user;
    private short x;
    private short y;
    private short maxHp;
    private short hp;

    public Player(User user) {
        this.user = user;
    }

    public void die() {
        this.hp = 0;
    }
}
