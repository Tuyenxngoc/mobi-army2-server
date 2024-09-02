package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.IMapManager;

public class MapManager implements IMapManager {

    private byte mapId;
    private short width;
    private short height;

    @Override
    public void loadMapId(byte mapId) {
        this.mapId = mapId;

    }

}
