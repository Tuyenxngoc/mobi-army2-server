package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.fight.MapTile;
import com.teamobi.mobiarmy2.fight.Player;

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

    public static void saveMapTilesToFile(List<MapTile> mapTiles, int imageWidth, int imageHeight, Player[] players, String filePath) throws IOException {
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        for (MapTile tile : mapTiles) {
            drawTile(g2d, tile);
        }

        for (Player player : players) {
            if (player == null) {
                continue;
            }

            int playerWidth = player.getWidth();
            int playerHeight = player.getHeight();

            int x = player.getX() - (playerWidth / 2);
            int y = player.getY() - playerHeight;

            g2d.setColor(Color.RED);
            g2d.fillRect(x, y, playerWidth, playerHeight);
            g2d.setColor(Color.BLACK);
            g2d.fillRect(player.getX(), player.getY(), 1, 1);
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
