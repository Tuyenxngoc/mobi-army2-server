package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.fight.Impl.MapTile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class MapTileExporter {

    public static void saveMapTilesToFile(List<MapTile> mapTiles, String filePath) throws IOException {
        // Tìm kích thước của bức ảnh
        int imageWidth = 0;
        int imageHeight = 0;
        for (MapTile tile : mapTiles) {
            imageWidth = Math.max(imageWidth, tile.getX() + tile.getWidth());
            imageHeight = Math.max(imageHeight, tile.getY() + tile.getHeight());
        }

        // Tạo BufferedImage với kích thước phù hợp
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, imageWidth, imageHeight);

        // Vẽ các tile lên BufferedImage
        for (MapTile tile : mapTiles) {
            drawTile(g2d, tile);
        }

        g2d.dispose();

        // Lưu BufferedImage vào file
        File file = new File(filePath);
        ImageIO.write(image, "png", file);
    }

    private static void drawTile(Graphics2D g2d, MapTile tile) {
        int[] data = tile.getData();
        int width = tile.getWidth();
        int height = tile.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int colorIndex = data[y * width + x];
                g2d.setColor(new Color(colorIndex));
                g2d.fillRect(tile.getX() + x, tile.getY() + y, 1, 1);
            }
        }
    }

}
