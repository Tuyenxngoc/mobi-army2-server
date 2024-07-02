package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MapManager {

    public final FightManager fightManager;
    public byte mapId;
    public short width;
    public short height;
    public short[] playerInitXPositions;
    public short[] playerInitYPositions;
    public List<MapEffectManager> mapEffects;

    public MapManager(FightManager fightManager) {
        this.fightManager = fightManager;
        this.mapId = 0;
        this.width = 0;
        this.height = 0;
        this.mapEffects = new ArrayList<>();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setMapId(byte mapId) {
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

            MapEffectManager mapEffect = new MapEffectManager(
                    brickId,
                    Utils.getShort(mapData, offset + 1),
                    Utils.getShort(mapData, offset + 3),
                    mapBrick.getData(),
                    (short) mapBrick.getWidth(),
                    (short) mapBrick.getHeight(),
                    MapData.isCollision(brickId)
            );

            mapEffects.add(mapEffect);
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

    public void addEntry(MapEffectManager mapEffect) {
        this.mapEffects.add(mapEffect);
    }

    public boolean isCollision(short x, short y) {
        for (MapEffectManager mapEffect : mapEffects) {
            if (mapEffect.isCollision(x, y)) {
                return true;
            }
        }
        return false;
    }

    public void handleCollision(short x, short y, Bullet bullet) {
        for (MapEffectManager mapEffect : mapEffects) {
            mapEffect.handleCollision(x, y, bullet);
        }

        for (int i = 0; i < fightManager.allCount; i++) {
            Player player = fightManager.players[i];
            if (player != null && player.idNV != 17) {
                player.collision(x, y, bullet);
            }
        }

        for (int i = 0; i < fightManager.bulletManager.boms.size(); i++) {
            BulletManager.BomHenGio bomb = fightManager.bulletManager.boms.get(i);
            while (!fightManager.mapManager.isCollision((short) bomb.x, (short) bomb.y)) {
                bomb.y++;
                for (int j = 0; j < 14; j++) {
                    if (fightManager.mapManager.isCollision((short) ((bomb.x - 7) + j), (short) bomb.y)) {
                        break;
                    }
                }
                if (bomb.y > fightManager.mapManager.height) {
                    fightManager.bulletManager.removeBom(i);
                    break;
                }
            }
        }
    }
}
