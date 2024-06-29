package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.MapData;
import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.util.Utils;

import java.util.ArrayList;

public class MapManager {

    private final FightManager fm;
    public short Width;
    public short Height;
    protected int Id;
    protected ArrayList<MapEntry> entrys;
    protected short[] XPlayerInit;
    protected short[] YPlayerInit;

    public MapManager(FightManager fm) {
        this.fm = fm;
        this.Id = 0;
        this.entrys = new ArrayList<>();
        this.Width = 0;
        this.Height = 0;
    }

    public MapManager(FightManager fm, int map) {
        this.fm = fm;
        this.entrys = new ArrayList<>();
        this.Id = map;
    }

    public int getWidth() {
        return this.Width;
    }

    public int getHeight() {
        return this.Height;
    }

    public void setMapId(int id) {
        this.Id = id;
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
        this.Width = Utils.getShort(ab, off);
        off += 2;
        this.Height = Utils.getShort(ab, off);
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
            entrys.add(me);
            off += 5;
        }
        int nPlayerPoint = ab[off++];
        this.XPlayerInit = new short[nPlayerPoint];
        this.YPlayerInit = new short[nPlayerPoint];
        for (i = 0; i < nPlayerPoint; i++) {
            this.XPlayerInit[i] = Utils.getShort(ab, off);
            off += 2;
            this.YPlayerInit[i] = Utils.getShort(ab, off);
            off += 2;
        }
    }

    public final void addEntry(MapEntry me) {
        this.entrys.add(me);
    }

    public final boolean isCollision(short X, short Y) {
        for (MapEntry m : entrys) {
            if (m.isCollision(X, Y)) {
                return true;
            }
        }
        return false;
    }

    public final void collision(short X, short Y, Bullet bull) {
        for (MapEntry m : entrys) {
            m.collision(X, Y, bull);
        }
        for (int i = 0; i < fm.allCount; i++) {
            Player pl = fm.players[i];
            if (pl != null && pl.idNV != 17) {
                pl.collision(X, Y, bull);
            }
        }
        for (int i = 0; i < fm.bullMNG.boms.size(); i++) {
            BulletManager.BomHenGio bom = fm.bullMNG.boms.get(i);
            while (!fm.mapMNG.isCollision((short) bom.X, (short) bom.Y)) {
                bom.Y++;
                for (int j = 0; j < 14; j++) {
                    if (fm.mapMNG.isCollision((short) ((bom.X - 7) + i), (short) bom.Y)) {
                        break;
                    }
                }
                if (bom.Y > fm.mapMNG.Height) {
                    fm.bullMNG.removeBom(i);
                    break;
                }
            }
        }
    }

}
