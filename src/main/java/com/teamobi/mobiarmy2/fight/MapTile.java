package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.model.ImageData;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class MapTile {
    private int brickId;
    private short x;
    private short y;
    private ImageData image;
    private boolean collision;

    public MapTile(int brickId, short x, short y, ImageData image, boolean collision) {
        this.brickId = brickId;
        this.x = x;
        this.y = y;
        this.image = image;
        this.collision = collision;
    }

    public boolean isCollision(short x, short y) {
        return (Utils.inRegion(x, y, this.x, this.y, image.getWidth(), image.getHeight())
                && Utils.isNotAlpha(getARGB(x - this.x, y - this.x)));
    }

    public int getARGB(int x, int y) {
        return image.getPixelData()[y * image.getWidth() + x];
    }

    public void collision(Bullet bullet) {
        ImageData bulletHoleImage = EffectManager.getHoleImageByBulletId(bullet.getBulletId());
        int holeWidth = bulletHoleImage.getWidth();
        int holeHeight = bulletHoleImage.getHeight();
        int[] bulletHolePixels = bulletHoleImage.getPixelData();
        int adjustedBulletX = bullet.getX() - x - holeWidth / 2;
        int adjustedBulletY = bullet.getY() - y - holeHeight / 2;

        if (!collision || !Utils.intersectRegions(adjustedBulletX + x, adjustedBulletY + y, holeWidth, holeHeight, x, y, image.getWidth(), image.getHeight())) {
            return;
        }

        int imageWidth = image.getWidth();
        int[] imagePixels = image.getPixelData();

        for (int holeY = 0; holeY < holeHeight; holeY++) {
            for (int holeX = 0; holeX < holeWidth; holeX++) {
                int imageX = adjustedBulletX + holeX;
                int imageY = adjustedBulletY + holeY;

                if (!Utils.inRegion(imageX, imageY, 0, 0, imageWidth, image.getHeight())) {
                    continue;
                }

                int bulletHolePixelIndex = holeY * holeWidth + holeX;
                int imagePixelIndex = imageY * imageWidth + imageX;

                if (bulletHolePixels[bulletHolePixelIndex] == 0xffff0000 && Utils.isNotAlpha(getARGB(imageX, imageY))) {
                    imagePixels[imagePixelIndex] = 0xff000000;
                } else if (bulletHolePixels[bulletHolePixelIndex] == 0xff000000) {
                    imagePixels[imagePixelIndex] = 0;
                }
            }
        }
    }

}
