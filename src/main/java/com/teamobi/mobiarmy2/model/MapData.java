package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.server.ServerManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapData {

    public static final class MapDataEntry {

        public byte id;
        public String name;
        public String file;
        public byte[] data;
        public short bg;
        public short mapAddY;
        public short cl2AddY;
        public short inWaterAddY;
        public short bullEffShower;
        public short[] XPlayer;
        public short[] YPlayer;
    }

    public static final class MapBrickEntry {

        public int id;
        public int[] data;
        public int Width;
        public int Height;

        MapBrickEntry(int id, int[] dat, int W, int H) {
            this.id = id;
            this.data = dat;
            this.Width = W;
            this.Height = H;
        }

    }

    public static final List<MapDataEntry> entries = new ArrayList<>();
    public static final List<MapBrickEntry> brickEntries = new ArrayList<>();
    public static final short[] idNotCollisions = new short[]{70, 71, 73, 74, 75, 77, 78, 79, 97};

    public static boolean isNotCollision(int id) {
        for (short idNotCollision : idNotCollisions) {
            if (id == idNotCollision) {
                return true;
            }
        }
        return false;
    }

    public static MapBrickEntry getMapBrickEntry(int id) {
        for (MapBrickEntry me : brickEntries) {
            if (me.id == id) {
                return me;
            }
        }
        return null;
    }

    public static void loadMapBrick(int id) {
        ServerManager.getInstance().logger().logMessage("Load Map Brick id=" + id);
        try {
            BufferedImage img = ImageIO.read(new File("res/icon/map/" + id + ".png"));
            int W = img.getWidth();
            int H = img.getHeight();
            int[] argb = new int[W * H];
            img.getRGB(0, 0, W, H, argb, 0, W);
            MapBrickEntry me = new MapBrickEntry(id, argb, W, H);
            brickEntries.add(me);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean existsMapBrick(int id) {
        for (MapBrickEntry me : brickEntries) {
            if (me.id == id) {
                return true;
            }
        }
        return false;
    }

}
