package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.entity.Bullet;
import com.teamobi.mobiarmy2.entity.MapBrick;
import com.teamobi.mobiarmy2.entity.MapTile;
import com.teamobi.mobiarmy2.server.MapManager;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FightMapManager {
    @Getter
    private final List<MapTile> mapTiles = new ArrayList<>();
    @Getter
    private short width;
    @Getter
    private short height;
    private short[] playerInitXPositions;
    private short[] playerInitYPositions;

    public FightMapManager() {
    }

    public List<short[]> getRandomPlayerPositions(int numPlayers) {
        //Kiểm tra nếu số người chơi lớn hơn số vị trí khả dụng
        if (numPlayers > playerInitXPositions.length || numPlayers > playerInitYPositions.length) {
            throw new IllegalArgumentException("Số người chơi vượt quá số lượng vị trí khả dụng");
        }

        //Khởi tạo danh sách chỉ số vị trí
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < playerInitXPositions.length; i++) {
            indices.add(i);
        }

        //Trộn ngẫu nhiên các chỉ số
        Collections.shuffle(indices);

        //Tạo danh sách vị trí người chơi dựa trên chỉ số đã trộn
        List<short[]> randomPositions = new ArrayList<>();
        for (int i = 0; i < numPlayers; i++) {
            short x = playerInitXPositions[indices.get(i)];
            short y = playerInitYPositions[indices.get(i)];
            randomPositions.add(new short[]{x, y});
        }

        return randomPositions;
    }

    public void loadMapId(byte mapId) {
        mapTiles.clear();
        byte[] mapData = MapManager.getMapData(mapId);

        if (mapData == null) {
            return;
        }

        int offset = 0;
        width = Utils.getShort(mapData, offset);
        offset += 2;
        height = Utils.getShort(mapData, offset);
        offset += 2;
        byte count = mapData[offset++];

        for (int i = 0; i < count; i++) {
            int brickId = mapData[offset];

            MapBrick mapBrick = MapManager.loadMapBrick(brickId);
            if (mapBrick == null) {
                continue;
            }

            MapTile mapTile = new MapTile(
                    brickId,
                    Utils.getShort(mapData, offset + 1),
                    Utils.getShort(mapData, offset + 3),
                    mapBrick.getImage(),
                    MapManager.isCollision(brickId)
            );

            mapTiles.add(mapTile);
            offset += 5;
        }

        int playerPointCount = mapData[offset++];
        this.playerInitXPositions = new short[playerPointCount];
        this.playerInitYPositions = new short[playerPointCount];
        for (int i = 0; i < playerPointCount; i++) {
            this.playerInitXPositions[i] = Utils.getShort(mapData, offset);
            offset += 2;
            this.playerInitYPositions[i] = Utils.getShort(mapData, offset);
            offset += 2;
        }
    }

    public boolean isCollision(int x, int y) {
        for (MapTile tile : mapTiles) {
            if (tile.isCollision(x, y)) {
                return true;
            }
        }
        return false;
    }

    public void handleCollision(Bullet bullet) {
        for (MapTile mapTile : mapTiles) {
            mapTile.collision(bullet);
        }
    }

    public void addNewTiles(MapTile mapTile) {
        mapTiles.add(mapTile);
    }

    public short[] getRandomPosition(int leftMargin, int rightMargin, int topMargin, int bottomMargin) {
        short x = (short) Utils.nextInt(leftMargin, (width - rightMargin));
        short y = (short) Utils.nextInt(topMargin, (height - bottomMargin));
        return new short[]{x, y};
    }
}
