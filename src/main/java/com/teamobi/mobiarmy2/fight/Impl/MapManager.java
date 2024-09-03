package com.teamobi.mobiarmy2.fight.Impl;

import com.teamobi.mobiarmy2.fight.IMapManager;
import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class MapManager implements IMapManager {

    private byte mapId;
    private short width;
    private short height;
    private List<MapTile> mapTiles;
    private short[] playerInitXPositions;
    private short[] playerInitYPositions;

    public MapManager() {
        mapTiles = new ArrayList<>();
    }

    @Override
    public void loadMapId(byte mapId) {
        this.mapId = mapId;
        byte[] mapData = MapData.getMapData(mapId);

        if (mapData == null) {
            return;
        }

        int offset = 0;
        width = Utils.getShort(mapData, offset);
        offset += 2;
        height = Utils.getShort(mapData, offset);
        offset += 2;
        byte entryCount = mapData[offset++];

        for (int i = 0; i < entryCount; i++) {
            int brickId = mapData[offset];

            MapBrick mapBrick = MapData.loadMapBrick(brickId);
            if (mapBrick == null) {
                continue;
            }

            MapTile mapTile = new MapTile(
                    brickId,
                    Utils.getShort(mapData, offset + 1),
                    Utils.getShort(mapData, offset + 3),
                    Arrays.copyOf(mapBrick.getData(), mapBrick.getData().length),
                    (short) mapBrick.getWidth(),
                    (short) mapBrick.getHeight(),
                    MapData.isCollision(brickId)
            );

            mapTiles.add(mapTile);
            offset += 5;
        }

        int playerPointCount = mapData[offset++];
        this.playerInitXPositions = new short[playerPointCount];
        this.playerInitYPositions = new short[playerPointCount];
        for (int i = 0; i < playerPointCount; i++) {
            this.playerInitXPositions[i] = Utils.getShort(mapData, offset);
            offset += 2;
            this.playerInitYPositions[i] = Utils.getShort(mapData, offset);
            offset += 2;
        }
    }

}
