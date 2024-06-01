package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class MapData {
    public static final List<MapEntry> MAP_ENTRIES = new ArrayList<>();
    public static final List<MapBrick> MAP_BRICKS = new ArrayList<>();
    public static final short[] idNotCollisions = new short[]{70, 71, 73, 74, 75, 77, 78, 79, 97};

    public static byte randomMap(int idNotSelect) {
        return 0;
    }
}
