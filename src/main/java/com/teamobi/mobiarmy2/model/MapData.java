package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.server.ServerManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class MapData {

    public static final class Map {
        public byte id;
        public String name;
        public String fileName;
        public byte[] data;
        public short bg;
        public short mapAddY;
        public short cl2AddY;
        public short inWaterAddY;
        public short bullEffShower;
        public short[] XPlayer;
        public short[] YPlayer;
    }

    public static final class MapBrick {
        public int id;
        public int[] data;
        public int Width;
        public int Height;

        MapBrick(int id, int[] dat, int W, int H) {
            this.id = id;
            this.data = dat;
            this.Width = W;
            this.Height = H;
        }
    }

    public static final List<Map> MAPS = new ArrayList<>();
    public static final List<MapBrick> MAP_BRICKS = new ArrayList<>();
    public static final short[] idNotCollisions = new short[]{70, 71, 73, 74, 75, 77, 78, 79, 97};

    public static boolean isNotCollision(int id) {
        for (short idNotCollision : idNotCollisions) {
            if (id == idNotCollision) {
                return true;
            }
        }
        return false;
    }

    public static MapBrick getMapBrickEntry(int id) {
        for (MapBrick mapBrick : MAP_BRICKS) {
            if (mapBrick.id == id) {
                return mapBrick;
            }
        }
        return null;
    }

    public static void loadMapBrickById(int id) {
        ServerManager.getInstance().logger().logMessage("Load Map Brick id=" + id);
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
        for (MapBrick mapBrick : MAP_BRICKS) {
            if (mapBrick.id == id) {
                return true;
            }
        }
        return false;
    }

}
