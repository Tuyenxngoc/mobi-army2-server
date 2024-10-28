package com.teamobi.mobiarmy2.fight;

import java.util.function.Predicate;

/**
 * @author tuyen
 */
public interface IFightManager {

    short[] getForceArgXY(int idGun, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100);

    void nextTurn();

    void addBoss(Boss boss);

    void leave(int playerId);

    boolean checkWin();

    void startGame(short teamPointsBlue, short teamPointsRed);

    void newShoot(int index, byte bullId, short angle, byte force, byte force2, byte numShoot, boolean isNextTurn);

    void changeLocation(int playerId, short x, short y);

    void addShoot(int playerId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    void sendMessageUpdateXY(int index);

    void skipTurn(int playerId);

    void useItem(int playerId, byte itemIndex);

    IMapManager getMapManger();

    void onTimeUp();

    int getTotalPlayers();

    int getTurnCount();

    byte getWindY();

    byte getWindX();

    Player[] getPlayers();

    Player getPlayerTurn();

    Player getRandomPlayer(Predicate<Player> condition);

    Player findClosestPlayer(short targetX, short targetY);

    void updateCantMove(Player pl);

    void updateCantSee(Player pl);

    void sendPlayerFlyPosition(byte index);

    void sendGhostAttackInfo(byte index, byte toIndex);

    void capture(byte index, byte toIndex);

    void sendBulletHit(byte index, byte toIndex);

    void giveXpToTeammates(boolean isTeamBlue, int addXP);
}
