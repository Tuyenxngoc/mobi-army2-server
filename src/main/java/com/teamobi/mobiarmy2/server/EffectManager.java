package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.fight.ImageData;
import com.teamobi.mobiarmy2.util.ImageUtils;

import java.io.IOException;

public class EffectManager {
    public static final ImageData[] HOLE_DATA = new ImageData[10];
    public static ImageData spiderWebData;

    static {
        try {
            HOLE_DATA[0] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/h32x26.png");
            HOLE_DATA[1] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/smallhole.png");
            HOLE_DATA[2] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/smallhole.png");
            HOLE_DATA[3] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/h36x30.png");
            HOLE_DATA[4] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/rocket.png");
            HOLE_DATA[5] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/rangehole.png");
            HOLE_DATA[6] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/hrangcua.png");
            HOLE_DATA[7] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/hgrenade.png");
            HOLE_DATA[8] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/h14x12.png");
            HOLE_DATA[9] = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/h55x50.png");

            spiderWebData = ImageUtils.loadImageData(GameConstants.EFFECT_PATH + "/mangnhen.png");
        } catch (IOException e) {
            throw new RuntimeException("Error loading hole or spider web data: " + e.getMessage(), e);
        }
    }

    public static byte getHoleIndexByBulletId(byte bullId) {
        return switch (bullId) {
            case 1, 27 -> 1;
            case 11, 17, 18, 19, 21, 44 -> 2;
            case 0, 32, 24, 48, 52 -> 3;
            case 10 -> 4;
            case 9 -> 5;
            case 6, 12 -> 6;
            case 7, 31, 37, 15, 22, 42, 43, 45, 57 -> 7;
            case 25, 47 -> 8;
            case 3 -> 9;
            default -> 0;
        };
    }

    public static ImageData getHoleImageByBulletId(byte bulletId) {
        byte holeIndex = getHoleIndexByBulletId(bulletId);
        return HOLE_DATA[holeIndex];
    }
}
