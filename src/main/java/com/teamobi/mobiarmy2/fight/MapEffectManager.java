package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author tuyen
 */
@Getter
public class MapEffectManager {

    public static int[] spiderData;
    public static int spiderWidth;
    public static int spiderHeight;
    public static final int[][] holeDataArray;
    public static final int[] holeWidths;
    public static final int[] holeHeights;

    public int id;
    public int[] argbData;
    public short width;
    public short height;
    public short xPosition;
    public short yPosition;
    public boolean canCollide;

    static {
        holeDataArray = new int[10][];
        holeWidths = new int[10];
        holeHeights = new int[10];
        BufferedImage img;
        try {
            img = ImageIO.read(new File("res/effect/hole/mangnhen.png"));
            spiderWidth = img.getWidth();
            spiderHeight = img.getHeight();
            spiderData = new int[spiderWidth * spiderHeight];
            img.getRGB(0, 0, spiderWidth, spiderHeight, spiderData, 0, spiderWidth);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/h32x26.png"));
            holeWidths[0] = img.getWidth();
            holeHeights[0] = img.getHeight();
            holeDataArray[0] = new int[holeWidths[0] * holeHeights[0]];
            img.getRGB(0, 0, holeWidths[0], holeHeights[0], holeDataArray[0], 0, holeWidths[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/smallhole.png"));
            holeWidths[1] = img.getWidth();
            holeHeights[1] = img.getHeight();
            holeDataArray[1] = new int[holeWidths[1] * holeHeights[1]];
            img.getRGB(0, 0, holeWidths[1], holeHeights[1], holeDataArray[1], 0, holeWidths[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/smallhole.png"));
            holeWidths[2] = img.getWidth();
            holeHeights[2] = img.getHeight();
            holeDataArray[2] = new int[holeWidths[2] * holeHeights[2]];
            img.getRGB(0, 0, holeWidths[2], holeHeights[2], holeDataArray[2], 0, holeWidths[2]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/h36x30.png"));
            holeWidths[3] = img.getWidth();
            holeHeights[3] = img.getHeight();
            holeDataArray[3] = new int[holeWidths[3] * holeHeights[3]];
            img.getRGB(0, 0, holeWidths[3], holeHeights[3], holeDataArray[3], 0, holeWidths[3]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/rocket.png"));
            holeWidths[4] = img.getWidth();
            holeHeights[4] = img.getHeight();
            holeDataArray[4] = new int[holeWidths[4] * holeHeights[4]];
            img.getRGB(0, 0, holeWidths[4], holeHeights[4], holeDataArray[4], 0, holeWidths[4]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/rangehole.png"));
            holeWidths[5] = img.getWidth();
            holeHeights[5] = img.getHeight();
            holeDataArray[5] = new int[holeWidths[5] * holeHeights[5]];
            img.getRGB(0, 0, holeWidths[5], holeHeights[5], holeDataArray[5], 0, holeWidths[5]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/hrangcua.png"));
            holeWidths[6] = img.getWidth();
            holeHeights[6] = img.getHeight();
            holeDataArray[6] = new int[holeWidths[6] * holeHeights[6]];
            img.getRGB(0, 0, holeWidths[6], holeHeights[6], holeDataArray[6], 0, holeWidths[6]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/hgrenade.png"));
            holeWidths[7] = img.getWidth();
            holeHeights[7] = img.getHeight();
            holeDataArray[7] = new int[holeWidths[7] * holeHeights[7]];
            img.getRGB(0, 0, holeWidths[7], holeHeights[7], holeDataArray[7], 0, holeWidths[7]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/h14x12.png"));
            holeWidths[8] = img.getWidth();
            holeHeights[8] = img.getHeight();
            holeDataArray[8] = new int[holeWidths[8] * holeHeights[8]];
            img.getRGB(0, 0, holeWidths[8], holeHeights[8], holeDataArray[8], 0, holeWidths[8]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            img = ImageIO.read(new File("res/effect/hole/h55x50.png"));
            holeWidths[9] = img.getWidth();
            holeHeights[9] = img.getHeight();
            holeDataArray[9] = new int[holeWidths[9] * holeHeights[9]];
            img.getRGB(0, 0, holeWidths[9], holeHeights[9], holeDataArray[9], 0, holeWidths[9]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public MapEffectManager(int id, short xPosition, short yPosition, int[] data, short width, short height, boolean canCollide) {
        this.id = id;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.canCollide = canCollide;
        this.setData(data, width, height);
    }

    public void setData(int[] data, short width, short height) {
        this.width = width;
        this.height = height;
        this.argbData = new int[data.length];
        System.arraycopy(data, 0, this.argbData, 0, data.length);
    }

    public boolean isCollision(short X, short Y) {
        return (Utils.inRegion(X, Y, this.xPosition, this.yPosition, this.width, this.height)
                && Utils.isNotAlpha(this.getARGB(X - this.xPosition, Y - this.yPosition)));
    }

    public void handleCollision(short bulletX, short bulletY, Bullet bullet) {
        int holeIndex = Bullet.getHoleByBulletId(bullet.bullId);
        int holeWidth = holeWidths[holeIndex];
        int holeHeight = holeHeights[holeIndex];
        int[] holeArgbData = holeDataArray[holeIndex];
        if (!canCollide || !Utils.intersecRegions(bulletX - holeWidth / 2, bulletY - holeHeight / 2, holeWidth, holeHeight, xPosition, yPosition, width, height)) {
            return;
        }
        bulletX -= xPosition + holeWidth / 2;
        bulletY -= yPosition + holeHeight / 2;
        for (int i = 0; i < holeHeight; i++) {
            for (int j = 0; j < holeWidth; j++) {
                if (Utils.inRegion(bulletX + j, bulletY + i, 0, 0, width, height)) {
                    if (holeArgbData[i * holeWidth + j] == 0xffff0000 && Utils.isNotAlpha(getARGB(bulletX + j, bulletY + i))) {
                        argbData[(bulletY + i) * width + bulletX + j] = 0xff000000;
                    } else if (holeArgbData[i * holeWidth + j] == 0xff000000) {
                        argbData[(bulletY + i) * width + bulletX + j] = 0;
                    }
                }
            }
        }
    }

    public int getARGB(int x, int y) {
        return argbData[y * width + x];
    }

}
