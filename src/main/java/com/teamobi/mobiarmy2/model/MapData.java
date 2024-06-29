package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.entry.map.MapBrick;
import com.teamobi.mobiarmy2.model.entry.map.MapEntry;
import com.teamobi.mobiarmy2.util.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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
            selectedId = MAP_ENTRIES.get(Utils.nextInt(30)).getId();
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

    public static boolean isNotColision(int id) {
        for (int i = 0; i < idNotCollisions.length; i++) {
            if (id == idNotCollisions[i]) {
                return true;
            }
        }
        return false;
    }

    public static MapBrick getMapBrickEntry(int id) {
        for (MapBrick me : MAP_BRICKS) {
            if (me.getId() == id) {
                return me;
            }
        }
        return null;
    }

    public static void loadMapBrick(int id) {
        try {
            BufferedImage img = ImageIO.read(new File("res/icon/map/" + id + ".png"));
            int W = img.getWidth();
            int H = img.getHeight();
            int[] argb = new int[W * H];
            img.getRGB(0, 0, W, H, argb, 0, W);
            MapBrick me = new MapBrick(id, argb, W, H);
            MAP_BRICKS.add(me);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean existsMapBrick(int id) {
        for (MapBrick me : MAP_BRICKS) {
            if (me.getId() == id) {
                return true;
            }
        }
        return false;
    }

}
