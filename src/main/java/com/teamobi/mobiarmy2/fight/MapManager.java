package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;

public class MapManager {

    private final FightManager fightManager;
    protected int id;
    public short width;
    public short height;
    protected ArrayList<MapEntry> mapEntries;
    protected short[] xPlayerInit;
    protected short[] yPlayerInit;

    public MapManager(FightManager fightManager) {
        this.fightManager = fightManager;
        this.id = 0;
        this.mapEntries = new ArrayList<>();
        this.width = 0;
        this.height = 0;
    }

    public MapManager(FightManager fightManager, int map) {
        this.fightManager = fightManager;
        this.mapEntries = new ArrayList<>();
        this.id = map;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setMapId(int id) {
        this.id = id;
        int i, off;

        byte ab[] = null;
        for (i = 0; i < MapData.MAP_ENTRIES.size(); i++) {
            com.teamobi.mobiarmy2.model.entry.map.MapEntry mEntry = MapData.MAP_ENTRIES.get(i);
            if (mEntry.getId() == id) {
                ab = mEntry.getData();
            }
        }
        if (ab == null) {
            return;
        }

        off = 0;
        this.width = Utils.getShort(ab, off);
        off += 2;
        this.height = Utils.getShort(ab, off);
        off += 2;
        byte len = ab[off++];

        for (i = 0; i < len; i++) {
            int brickId = ab[off];

            if (!MapData.existsMapBrick(brickId)) {
                MapData.loadMapBrick(brickId);
            }

            MapEntry me;
            if (MapData.existsMapBrick(brickId)) {
                MapBrick mB = MapData.getMapBrickEntry(brickId);
                me = new MapEntry(brickId, Utils.getShort(ab, off + 1), Utils.getShort(ab, off + 3), mB.getData(), (short) mB.getWidth(), (short) mB.getHeight(), !MapData.isNotColision(brickId));
            } else {
                me = new MapEntry(brickId, Utils.getShort(ab, off + 1), Utils.getShort(ab, off + 3), null, (short) 0, (short) 0, !MapData.isNotColision(brickId));
            }
            mapEntries.add(me);
            off += 5;
        }
        int nPlayerPoint = ab[off++];
        this.xPlayerInit = new short[nPlayerPoint];
        this.yPlayerInit = new short[nPlayerPoint];
        for (i = 0; i < nPlayerPoint; i++) {
            this.xPlayerInit[i] = Utils.getShort(ab, off);
            off += 2;
            this.yPlayerInit[i] = Utils.getShort(ab, off);
            off += 2;
        }
    }

    public final void addEntry(MapEntry me) {
        this.mapEntries.add(me);
    }

    public final boolean isCollision(short X, short Y) {
        for (MapEntry m : mapEntries) {
            if (m.isCollision(X, Y)) {
                return true;
            }
        }
        return false;
    }

    public final void collision(short X, short Y, Bullet bull) {
        for (MapEntry m : mapEntries) {
            m.collision(X, Y, bull);
        }
        for (int i = 0; i < fightManager.allCount; i++) {
            Player pl = fightManager.players[i];
            if (pl != null && pl.idNV != 17) {
                pl.collision(X, Y, bull);
            }
        }
        for (int i = 0; i < fightManager.bullMNG.boms.size(); i++) {
            BulletManager.BomHenGio bom = fightManager.bullMNG.boms.get(i);
            while (!fightManager.mapMNG.isCollision((short) bom.X, (short) bom.Y)) {
                bom.Y++;
                for (int j = 0; j < 14; j++) {
                    if (fightManager.mapMNG.isCollision((short) ((bom.X - 7) + i), (short) bom.Y)) {
                        break;
                    }
                }
                if (bom.Y > fightManager.mapMNG.height) {
                    fightManager.bullMNG.removeBom(i);
                    break;
                }
            }
        }
    }

}
