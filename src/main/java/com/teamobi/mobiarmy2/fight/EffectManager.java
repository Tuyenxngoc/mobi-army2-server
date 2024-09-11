package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.ImageData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author tuyen
 */
public class EffectManager {
    public static final int HOLE_COUNT = 10;
    public static final ImageData[] holeData = new ImageData[HOLE_COUNT];
    public static ImageData spiderWebData;

    static {
        try {
            holeData[0] = loadImageData("res/effect/hole/h32x26.png");
            holeData[1] = loadImageData("res/effect/hole/smallhole.png");
            holeData[2] = loadImageData("res/effect/hole/smallhole.png");
            holeData[3] = loadImageData("res/effect/hole/h36x30.png");
            holeData[4] = loadImageData("res/effect/hole/rocket.png");
            holeData[5] = loadImageData("res/effect/hole/rangehole.png");
            holeData[6] = loadImageData("res/effect/hole/hrangcua.png");
            holeData[7] = loadImageData("res/effect/hole/hgrenade.png");
            holeData[8] = loadImageData("res/effect/hole/h14x12.png");
            holeData[9] = loadImageData("res/effect/hole/h55x50.png");

            spiderWebData = loadImageData("res/effect/hole/mangnhen.png");
        } catch (IOException e) {
            throw new RuntimeException("Error loading hole or spider web data: " + e.getMessage(), e);
        }
    }

    /**
     * Load image data from a file.
     *
     * @param filePath the path to the image file
     * @return the loaded ImageData
     * @throws IOException if there is an error reading the file
     */
    private static ImageData loadImageData(String filePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(filePath));
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixelData = new int[width * height];
        img.getRGB(0, 0, width, height, pixelData, 0, width);
        return new ImageData(width, height, pixelData);
    }

    public static ImageData getHoleImageByBulletId(byte bulletId) {
        return holeData[0];
    }
}
