package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.constant.GameConstants;
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
            holeData[0] = loadImageData(GameConstants.EFFECT_PATH + "/h32x26.png");
            holeData[1] = loadImageData(GameConstants.EFFECT_PATH + "/smallhole.png");
            holeData[2] = loadImageData(GameConstants.EFFECT_PATH + "/smallhole.png");
            holeData[3] = loadImageData(GameConstants.EFFECT_PATH + "/h36x30.png");
            holeData[4] = loadImageData(GameConstants.EFFECT_PATH + "/rocket.png");
            holeData[5] = loadImageData(GameConstants.EFFECT_PATH + "/rangehole.png");
            holeData[6] = loadImageData(GameConstants.EFFECT_PATH + "/hrangcua.png");
            holeData[7] = loadImageData(GameConstants.EFFECT_PATH + "/hgrenade.png");
            holeData[8] = loadImageData(GameConstants.EFFECT_PATH + "/h14x12.png");
            holeData[9] = loadImageData(GameConstants.EFFECT_PATH + "/h55x50.png");

            spiderWebData = loadImageData(GameConstants.EFFECT_PATH + "/mangnhen.png");
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

    public static byte getHoleByBulletId(byte bullId) {
        return switch (bullId) {
            case 0, 32, 24, 35, 48, 52 -> 3;
            case 1, 27 -> 1;
            case 6, 12 -> 6;
            case 7, 31, 37, 15, 22, 42, 43, 45, 57 -> 7;
            case 3 -> 9;
            case 9 -> 5;
            case 10 -> 4;
            case 11, 19, 17, 18, 21, 40, 41, 44 -> 2;
            case 25, 47 -> 8;
            default -> 0;
        };
    }

    public static ImageData getHoleImageByBulletId(byte bulletId) {
        byte holeIndex = getHoleByBulletId(bulletId);
        if (holeIndex >= 0 && holeIndex < HOLE_COUNT) {
            return holeData[holeIndex];
        }
        return holeData[0];
    }
}
