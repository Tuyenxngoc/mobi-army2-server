package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.User;

public interface IFightManager {

    void leave(int playerId);

    void startGame(short teamPointsBlue, short teamPointsRed);

    void changeLocation(User user, short x, short y);

    void addShoot(User user, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    void skipTurn(User user);

    void useItem(byte itemIndex);

    IMapManager getMapManger();

}
