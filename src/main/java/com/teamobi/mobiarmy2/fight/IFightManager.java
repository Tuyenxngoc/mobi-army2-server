package com.teamobi.mobiarmy2.fight;

/**
 * @author tuyen
 */
public interface IFightManager {

    short[] getForceArgXY(int idGun, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100);

    void setNextTurn(boolean nextTurn);

    void nextTurn();

    void leave(int playerId);

    void startGame(short teamPointsBlue, short teamPointsRed);

    void newShoot(int index, byte bullId, short angle, byte force, byte force2, byte numShoot);

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
