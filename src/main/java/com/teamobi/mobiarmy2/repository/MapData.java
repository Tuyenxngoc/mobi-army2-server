package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.model.map.MapBrick;
import com.teamobi.mobiarmy2.model.map.MapEntry;
import com.teamobi.mobiarmy2.util.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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

    public static byte[] getMapData(byte mapId) {
        for (MapEntry mapEntry : MapData.MAP_ENTRIES) {
            if (mapEntry.getId() == mapId) {
                return mapEntry.getData();
            }
        }
        return null;
    }

    public static String getMapNames(byte... ids) {
        StringBuilder result = new StringBuilder();
        for (byte id : ids) {
            int index = MAP_ENTRIES.indexOf(new MapEntry(id));
            if (index != -1) {
                result.append(MAP_ENTRIES.get(index).getName()).append(", ");
            }
        }
        if (!result.isEmpty()) {
            result.setLength(result.length() - 2);
        }
        return result.toString();
    }

    public static boolean isCollision(int id) {
        for (short idNotCollision : idNotCollisions) {
            if (id == idNotCollision) {
                return false;
            }
        }
        return true;
    }

    public static MapBrick loadMapBrick(int brickId) {
        int index = MAP_BRICKS.indexOf(new MapBrick(brickId));

        if (index != -1) {
            return MAP_BRICKS.get(index);
        }

        try {
            File imageFile = new File("res/icon/map/" + brickId + ".png");
            BufferedImage image = ImageIO.read(imageFile);

            if (image == null) {
                throw new IOException("Failed to read image: " + imageFile.getAbsolutePath());
            }

            int width = image.getWidth();
            int height = image.getHeight();
            int[] pixelData = new int[width * height];
            image.getRGB(0, 0, width, height, pixelData, 0, width);

            MapBrick mapBrick = new MapBrick(brickId, pixelData, width, height);
            MAP_BRICKS.add(mapBrick);

            return mapBrick;
        } catch (IOException e) {
            System.err.println("Error loading map brick with id " + brickId + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
