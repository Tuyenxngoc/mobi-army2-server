package com.teamobi.mobiarmy2.fight.impl;

import com.teamobi.mobiarmy2.fight.*;
import com.teamobi.mobiarmy2.model.map.MapBrick;
import com.teamobi.mobiarmy2.repository.MapRepository;
import com.teamobi.mobiarmy2.util.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class MapManager implements IMapManager {

    private short width;
    private short height;
    private List<MapTile> mapTiles = new ArrayList<>();
    private short[] playerInitXPositions;
    private short[] playerInitYPositions;
    private final IFightManager fightManager;

    public MapManager(IFightManager fightManager) {
        this.fightManager = fightManager;
    }

    @Override
    public short getHeight() {
        return height;
    }

    @Override
    public short getWidth() {
        return width;
    }

    @Override
    public List<MapTile> getMapTiles() {
        return mapTiles;
    }

    @Override
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

    @Override
    public void loadMapId(byte mapId) {
        byte[] mapData = MapRepository.getMapData(mapId);

        if (mapData == null) {
            return;
        }

        int offset = 0;
        width = Utils.getShort(mapData, offset);
        offset += 2;
        height = Utils.getShort(mapData, offset);
        offset += 2;
        byte entryCount = mapData[offset++];

        for (int i = 0; i < entryCount; i++) {
            int brickId = mapData[offset];

            MapBrick mapBrick = MapRepository.loadMapBrick(brickId);
            if (mapBrick == null) {
                continue;
            }

            MapTile mapTile = new MapTile(
                    brickId,
                    Utils.getShort(mapData, offset + 1),
                    Utils.getShort(mapData, offset + 3),
                    mapBrick.getImage(),
                    MapRepository.isCollision(brickId)
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

    @Override
    public boolean isCollision(short x, short y) {
        for (MapTile tile : mapTiles) {
            if (tile.isCollision(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void collision(short X, short Y, Bullet bull) {
        for (MapTile mapTile : mapTiles) {
            mapTile.collision(X, Y, bull);
        }
        for (int i = 0; i < fightManager.getTotalPlayers(); i++) {
            Player pl = fightManager.getPlayers()[i];
            if (pl != null && pl.getCharacterId() != 17) {
                pl.collision(X, Y, bull);
            }
        }
        //Todo: bom hen gio
    }

    @Override
    public void refresh() {

    }

    @Override
    public void addNewTiles(MapTile mapTile) {
        mapTiles.add(mapTile);
    }

    @Override
    public short[] getRandomPosition(short leftMargin, short rightMargin, short topMargin, short bottomMargin) {
        short x = (short) Utils.nextInt(leftMargin, (short) (width - rightMargin));
        short y = (short) Utils.nextInt(topMargin, (short) (height - bottomMargin));
        return new short[]{x, y};
    }

}
