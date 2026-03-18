package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.fight.Boss;
import com.teamobi.mobiarmy2.fight.FightMapManager;
import com.teamobi.mobiarmy2.fight.MapTile;
import com.teamobi.mobiarmy2.fight.Player;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class MapTileExporter {
    private static final Path EXPORT_BASE_DIR = Paths.get("map_exports");

    /**
     * Xuất trạng thái hiện tại của bản đồ ra file ảnh PNG.
     */
    public static void export(FightMapManager mapManager, Player[] players, int mapId, int turnCount) throws IOException {
        // Đảm bảo thư mục export tồn tại
        if (!Files.exists(EXPORT_BASE_DIR)) {
            Files.createDirectories(EXPORT_BASE_DIR);
        }

        // Tạo tên file theo quy tắc chuẩn: map_ID_turn_STT_timestamp.png
        String fileName = String.format("map_%d_turn_%d_%d.png", mapId, turnCount, System.currentTimeMillis());
        Path targetPath = EXPORT_BASE_DIR.resolve(fileName);

        // Thực hiện vẽ và lưu
        saveToPath(
                mapManager.getMapTiles(),
                mapManager.getWidth(),
                mapManager.getHeight(),
                players,
                targetPath
        );
    }

    private static void saveToPath(List<MapTile> mapTiles, int width, int height, Player[] players, Path targetPath) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // Vẽ các mảnh bản đồ (Tiles)
        for (MapTile tile : mapTiles) {
            drawTile(g2d, tile);
        }

        // Vẽ người chơi
        for (Player player : players) {
            if (player == null) {
                continue;
            }

            Color color = Color.RED;
            if (player instanceof Boss) {
                color = Color.GREEN;
            }

            if (player.isDead()) {
                color = Color.BLUE;
            }

            int playerWidth = player.getWidth();
            int playerHeight = player.getHeight();

            int x = player.getX() - (playerWidth / 2);
            int y = player.getY() - playerHeight;

            g2d.setColor(color);
            g2d.fillRect(x, y, playerWidth, playerHeight);

            // Vẽ điểm checkpoint (tâm) của người chơi
            g2d.setColor(Color.BLACK);
            g2d.fillRect(player.getX(), player.getY(), 1, 1);
        }

        g2d.dispose();
        ImageIO.write(image, "png", targetPath.toFile());
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
