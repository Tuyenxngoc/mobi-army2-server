package com.teamobi.mobiarmy2.fight;

import java.util.List;

/**
 * @author tuyen
 */
public interface IMapManager {

    short getWidth();

    short getHeight();

    //Test
    List<MapTile> getMapTiles();

    List<short[]> getRandomPlayerPositions(int numPlayers);

    void loadMapId(byte mapId);

    boolean isCollision(short x, short y);

    void collision(short x, short y, Bullet bullet);

    void refresh();

    void addNewTiles(MapTile mapTile);

}
