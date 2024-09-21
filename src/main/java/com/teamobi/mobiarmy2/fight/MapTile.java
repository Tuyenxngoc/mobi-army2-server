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
        this.collision = collision;
        copyImageData(image);
    }

    private void copyImageData(ImageData image) {
        if (image != null) {
            int[] newPixelData = new int[image.getPixelData().length];
            System.arraycopy(image.getPixelData(), 0, newPixelData, 0, image.getPixelData().length);

            this.image = new ImageData(image.getWidth(), image.getHeight(), newPixelData);
        }
    }

    public boolean isCollision(short x, short y) {
        return (Utils.inRegion(x, y, this.x, this.y, image.getWidth(), image.getHeight()))
                && Utils.isNotAlpha(this.getARGB(x - this.x, y - this.y));
    }

    public int getARGB(int x, int y) {
        return image.getPixelData()[y * image.getWidth() + x];
    }

    public void collision(int bx, int by, Bullet bullet) {
        ImageData bulletHoleImage = EffectManager.getHoleImageByBulletId(bullet.getBullId());
        int w = bulletHoleImage.getWidth();
        int h = bulletHoleImage.getHeight();
        int[] argbS = bulletHoleImage.getPixelData();
        if (!this.collision || !Utils.intersectRegions(bx - w / 2, by - h / 2, w, h, this.x, this.y, image.getWidth(), image.getHeight())) {
            return;
        }
        bx -= x + w / 2;
        by -= y + h / 2;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                if (Utils.inRegion(bx + j, by + i, 0, 0, image.getWidth(), image.getHeight())) {
                    if (argbS[i * w + j] == 0xffff0000 && Utils.isNotAlpha(getARGB(bx + j, by + i))) {
                        image.getPixelData()[(by + i) * image.getWidth() + bx + j] = 0xff000000;
                    } else if (argbS[i * w + j] == 0xff000000) {
                        image.getPixelData()[(by + i) * image.getWidth() + bx + j] = 0;
                    }
                }
            }
        }
    }

}
