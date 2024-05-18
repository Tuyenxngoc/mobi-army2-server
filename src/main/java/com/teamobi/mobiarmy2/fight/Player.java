package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import lombok.Getter;

/**
 * @author tuyen
 */
@Getter
public class Player {

    private final User user;
    public short X;
    public short Y;
    public short width;
    public short height;

    public Player(User user) {
        this.user = user;
    }
}