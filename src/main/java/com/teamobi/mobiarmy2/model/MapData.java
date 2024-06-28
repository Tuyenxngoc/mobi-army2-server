package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;
import com.teamobi.mobiarmy2.util.Utils;

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
        byte selectedId;
        do {
            selectedId = MAP_ENTRIES.get(Utils.nextInt(MAP_ENTRIES.size())).getId();
        } while (selectedId == idNotSelect);

        return selectedId;
    }

    public static String getMapNames(byte... ids) {
        StringBuilder result = new StringBuilder();
        for (byte id : ids) {
            int index = MAP_ENTRIES.indexOf(new MapEntry(id));
            if (index != -1) {
                result.append(MAP_ENTRIES.get(index).getName()).append(", ");
            }
        }
        if (result.length() > 0) {
            result.setLength(result.length() - 2);
        }
        return result.toString();
    }

}
