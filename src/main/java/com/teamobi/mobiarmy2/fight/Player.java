package com.teamobi.mobiarmy2.fight;

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
    private byte pixel;
    private byte angry;
    private short x;
    private short y;
    private short maxHp;
    private short hp;
    private boolean isUpdateHP;
    private boolean isUpdateAngry;
    private boolean isLucky;
    private boolean isPoisoned;
    private byte eyeSmokeCount;
    private byte freezeCount;
    private byte windStopCount;

    public Player(User user) {
        this.user = user;
    }

    public void die() {
        this.hp = 0;
    }

    public void nextLuck() {
        isLucky = Math.random() < 0.5;
    }

    public void decreaseWindStopCount() {
        if (windStopCount > 0) {
            windStopCount--;
        }
    }

}
