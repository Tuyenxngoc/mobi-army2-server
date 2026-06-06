package com.teamobi.mobiarmy2.fight;

public interface IFightBase {

    void startGame();

    void handlePlayerShoot(int userId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    void sendMessageUpdateXY(int index);

    FightMapManager getFightMapManager();

    Player[] getPlayers();

    int getTotalPlayers();

    byte getWindX();

    byte getWindY();

    void doNextTurn();

    Player getPlayerTurn();
}
