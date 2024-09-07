package com.teamobi.mobiarmy2.fight;

import java.util.List;

public interface IMapManager {

    short getWidth();

    short getHeight();

    List<short[]> getRandomPlayerPositions(int numPlayers);

    void loadMapId(byte mapId);

}
