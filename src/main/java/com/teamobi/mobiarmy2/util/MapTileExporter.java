package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.fight.MapTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author tuyen
 */
public class MapTileExporter {

    public static void saveMapTilesToFile(List<MapTile> mapTiles, int imageWidth, int imageHeight, String filePath) throws IOException {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        for (MapTile tile : mapTiles) {
            drawTile(g2d, tile);
        }

        g2d.dispose();

        File file = new File(filePath);
        ImageIO.write(image, "png", file);
    }

    private static void drawTile(Graphics2D g2d, MapTile tile) {
        int[] data = tile.getImage().getPixelData();
        int width = tile.getImage().getWidth();
        int height = tile.getImage().getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int colorIndex = data[y * width + x];
                g2d.setColor(new Color(colorIndex));
                g2d.fillRect(tile.getX() + x, tile.getY() + y, 1, 1);
            }
        }
    }

}
