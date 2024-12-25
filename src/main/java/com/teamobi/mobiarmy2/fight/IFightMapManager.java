package com.teamobi.mobiarmy2.fight;

import java.util.List;

/**
 * @author tuyen
 */
public interface IFightMapManager {

    short getWidth();

    short getHeight();

    List<MapTile> getMapTiles();

    List<short[]> getRandomPlayerPositions(int numPlayers);

    void loadMapId(byte mapId);

    boolean isCollision(short x, short y);

    void collision(short x, short y, Bullet bullet);

    void addNewTiles(MapTile mapTile);

    short[] getRandomPosition(int leftMargin, int rightMargin, int topMargin, int bottomMargin);
}
