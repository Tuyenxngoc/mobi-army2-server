package com.teamobi.mobiarmy2.fight;

import java.util.concurrent.Future;
import java.util.function.Predicate;

public interface IFightManager {


    void startGame();

    Future<?> leaveGame(int userId);

    void handlePlayerShoot(int userId, byte bullId, short x, short y, short angle, byte force, byte force2, byte numShoot);

    void changeLocation(int userId, short x, short y);

    void skipTurn(int userId);

    void updatePlayerCoordinates(int userId, short x, short y);

    void useItem(int userId, byte itemIndex);

    void addPendingBoss(Boss player);

    void sendUpdateCoordinates(byte index);

    void sendCapture(byte index, byte toIndex);

    void sendBulletHit(byte index, byte toIndex);

    void sendRewardMessage(Player player, Reward reward);

    void sendPlayerFlyPosition(byte index);

    void sendGhostAttackInfo(byte index, byte toIndex);

    void sendMessageUpdateXY(int index);

    FightMapManager getFightMapManager();

    Player[] getPlayers();

    int getTotalPlayers();

    int getTurnCount();

    byte getWindX();

    byte getWindY();

    short[] getForceArgXY(int idGun, boolean isXuyenMap, short X, short Y, short toX, short toY, short Mx, short My, int arg, int force, int msg, int g100);

    void nextTurn();

    void doNextTurn();

    void giveXpToTeammates(boolean isTeamBlue, int addXP, Player sharer);

    void createShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot);

    void createShoot(Player player, byte bullId, short angle, byte force, byte force2, byte numShoot, boolean isNextTurn);

    void handlePlayerShootResult(int userId);

    Player getPlayerTurn();

    Player getRandomPlayer(Predicate<Player> condition);

    Player findClosestPlayer(short targetX, short targetY);

    Player getRandomPlayer();

    void collisionPlayers(short x, short y, Bullet bullet);

    void onBulletExplode(short bx, short by, Bullet bullet);
}
