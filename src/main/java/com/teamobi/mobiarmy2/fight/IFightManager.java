package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;

public interface IFightManager {

    void leave(int playerId);

    void chatMessage(int playerId, String message);

    void startGame();

    void changeLocation(User user, short x, short y);

    void addShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    void skipTurn(User user);

    void useItem(byte itemIndex);

}
