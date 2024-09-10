package com.teamobi.mobiarmy2.fight;

import java.util.List;

/**
 * @author tuyen
 */
public interface IMapManager {

    short getWidth();

    short getHeight();

    List<short[]> getRandomPlayerPositions(int numPlayers);

    void loadMapId(byte mapId);

    boolean isCollision(short x, short y);

    void collision(Bullet bull);
}
