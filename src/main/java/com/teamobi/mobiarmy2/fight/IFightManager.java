package com.teamobi.mobiarmy2.fight;

/**
 * @author tuyen
 */
public interface IFightManager {

    void nextTurn();

    void leave(int playerId);

    void startGame(short teamPointsBlue, short teamPointsRed);

    void changeLocation(int playerId, short x, short y);

    void addShoot(int playerId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    void sendMessageUpdateXY(int index);

    void skipTurn(int playerId);

    void useItem(int playerId, byte itemIndex);

    IMapManager getMapManger();

    void onTimeUp();

    int getTotalPlayers();

    byte getWindY();

    byte getWindX();

    Player[] getPlayers();

    Player getPlayerTurn();

    Player findClosestPlayer(short targetX, short targetY);

    void updateCantMove(Player pl);

    void updateBiDoc(Player pl);

    void updateCantSee(Player pl);

}
