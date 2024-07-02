package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class MapManager {

    public final FightManager fightManager;
    public int mapId;
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

    public void setMapId(int mapId) {
        this.mapId = mapId;
        byte[] mapData = null;

        for (MapEntry mapEntry : MapData.MAP_ENTRIES) {
            if (mapEntry.getId() == mapId) {
                mapData = mapEntry.getData();
                break;
            }
        }

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

            if (!MapData.existsMapBrick(brickId)) {
                MapData.loadMapBrick(brickId);
            }

            MapEffectManager mapEffect;
            MapBrick mapBrick = MapData.getMapBrickEntry(brickId);
            if (mapBrick == null) {
                mapEffect = new MapEffectManager(
                        brickId,
                        Utils.getShort(mapData, offset + 1),
                        Utils.getShort(mapData, offset + 3),
                        null,
                        (short) 0,
                        (short) 0,
                        !MapData.isNotCollision(brickId)
                );
            } else {
                mapEffect = new MapEffectManager(
                        brickId,
                        Utils.getShort(mapData, offset + 1),
                        Utils.getShort(mapData, offset + 3),
                        mapBrick.getData(),
                        (short) mapBrick.getWidth(),
                        (short) mapBrick.getHeight(),
                        !MapData.isNotCollision(brickId)
                );
            }

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

    public final void addEntry(MapEffectManager mapEffect) {
        this.mapEffects.add(mapEffect);
    }

    public final boolean isCollision(short x, short y) {
        for (MapEffectManager mapEffect : mapEffects) {
            if (mapEffect.isCollision(x, y)) {
                return true;
            }
        }
        return false;
    }

    public final void handleCollision(short x, short y, Bullet bullet) {
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
