package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;
import lombok.Getter;
import lombok.Setter;


/**
 * @author tuyen
 */
@Setter
@Getter
public class Player {
    private User user;
    private short x;
    private short y;
    private short width;
    private short height;

    public Player(User user) {
        this.user = user;
        this.x = 0;
        this.y = 0;
        this.width = 32;
        this.height = 32;
    }

}
