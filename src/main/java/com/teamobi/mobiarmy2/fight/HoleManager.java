package com.teamobi.mobiarmy2.fight;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author tuyen
 */
public class HoleManager {
    public static final int[][] holePixelData = new int[10][];
    public static final int[] holeWidths = new int[10];
    public static final int[] holeHeights = new int[10];

    static {
        loadHoleData(0, "res/effect/hole/h32x26.png");
        loadHoleData(1, "res/effect/hole/smallhole.png");
        loadHoleData(2, "res/effect/hole/smallhole.png");
        loadHoleData(3, "res/effect/hole/h36x30.png");
        loadHoleData(4, "res/effect/hole/rocket.png");
        loadHoleData(5, "res/effect/hole/rangehole.png");
        loadHoleData(6, "res/effect/hole/hrangcua.png");
        loadHoleData(7, "res/effect/hole/hgrenade.png");
        loadHoleData(8, "res/effect/hole/h14x12.png");
        loadHoleData(9, "res/effect/hole/h55x50.png");
    }

    private static void loadHoleData(int index, String filePath) {
        try {
            BufferedImage img = ImageIO.read(new File(filePath));
            holeWidths[index] = img.getWidth();
            holeHeights[index] = img.getHeight();
            holePixelData[index] = new int[holeWidths[index] * holeHeights[index]];
            img.getRGB(0, 0, holeWidths[index], holeHeights[index], holePixelData[index], 0, holeWidths[index]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
