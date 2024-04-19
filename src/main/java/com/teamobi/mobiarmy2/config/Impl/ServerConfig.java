package com.teamobi.mobiarmy2.config.Impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.CommonConstant;

import java.io.FileInputStream;
import java.util.Properties;

public class ServerConfig implements IServerConfig {

    private boolean debug;
    private short port;
    private String[] roomTypes;
    private String[] roomTypesEng;
    private byte equipVersion2;
    private byte iconversion2;
    private byte levelCVersion2;
    private byte valuesversion2;
    private byte playerVersion2;
    private String gameInfo;
    private String gameInfoUrl;
    private String gameClanUrl;
    private byte maxPlayersFight;
    public static byte maxPlayers;
    private byte numMapBoss;
    private byte startMapBoss;
    private byte[] mapIdBoss;
    private byte numbPlayers;
    private String[] nameRooms;
    private byte nPlayersInitRoom;
    private int min_xu_so_cap;
    private int min_xu_trung_cap;
    private int min_xu_cao_cap;
    private int min_xu_dau_truong;
    private int min_xu_tu_do;
    private int min_xu_boss;
    private int min_xu_clan;
    private int max_xu_so_cap;
    private int max_xu_trung_cap;
    private int max_xu_cao_cap;
    private int max_xu_dau_truong;
    private int max_xu_tu_do;
    private int max_xu_boss;
    private int max_xu_clan;
    private byte initMap;


    private final Properties properties;

    public ServerConfig(String resourceName) {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(CommonConstant.RESOURCES_PATH + resourceName)) {
            properties.load(fis);
            initializeConfigProperties();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeConfigProperties() {
        Gson gson = new Gson();

        debug = Boolean.parseBoolean(properties.getProperty("server.debug"));
        port = Short.parseShort(properties.getProperty("server.port"));

        roomTypes = gson.fromJson(properties.getProperty("room.names.vi"), String[].class);
        roomTypesEng = gson.fromJson(properties.getProperty("room.names.en"), String[].class);
        if (roomTypes.length != roomTypesEng.length) {
            throw new IllegalStateException("Số lượng phòng trong Tiếng Việt và Tiếng Anh không khớp nhau.");
        }

        equipVersion2 = Byte.parseByte(properties.getProperty("equipVersion2"));
        iconversion2 = Byte.parseByte(properties.getProperty("iconversion2"));
        levelCVersion2 = Byte.parseByte(properties.getProperty("levelCVersion2"));
        valuesversion2 = Byte.parseByte(properties.getProperty("valuesversion2"));
        playerVersion2 = Byte.parseByte(properties.getProperty("playerVersion2"));

        gameInfo = properties.getProperty("game.info");
        gameInfoUrl = properties.getProperty("game.info.url");
        gameClanUrl = properties.getProperty("game.clan.url");

        maxPlayersFight = Byte.parseByte(properties.getProperty("max_fight"));
        numMapBoss = Byte.parseByte(properties.getProperty("num_map_boss"));
        startMapBoss = Byte.parseByte(properties.getProperty("start_map_boss"));

        mapIdBoss = gson.fromJson(properties.getProperty("map_id_boss"), byte[].class);
        numbPlayers = Byte.parseByte(properties.getProperty("numb_player"));
        nameRooms = gson.fromJson(properties.getProperty("name_rooms"), String[].class);

        nPlayersInitRoom = Byte.parseByte(properties.getProperty("n_players_init_room"));

        min_xu_so_cap = Integer.parseInt(properties.getProperty("min_xu_so_cap"));
        min_xu_trung_cap = Integer.parseInt(properties.getProperty("min_xu_trung_cap"));
        min_xu_cao_cap = Integer.parseInt(properties.getProperty("min_xu_cao_cap"));
        min_xu_dau_truong = Integer.parseInt(properties.getProperty("min_xu_dau_truong"));
        min_xu_tu_do = Integer.parseInt(properties.getProperty("min_xu_tu_do"));
        min_xu_boss = Integer.parseInt(properties.getProperty("min_xu_boss"));
        min_xu_clan = Integer.parseInt(properties.getProperty("min_xu_clan"));

        max_xu_so_cap = Integer.parseInt(properties.getProperty("max_xu_so_cap"));
        max_xu_trung_cap = Integer.parseInt(properties.getProperty("max_xu_trung_cap"));
        max_xu_cao_cap = Integer.parseInt(properties.getProperty("max_xu_cao_cap"));
        max_xu_dau_truong = Integer.parseInt(properties.getProperty("max_xu_dau_truong"));
        max_xu_tu_do = Integer.parseInt(properties.getProperty("max_xu_tu_do"));
        max_xu_boss = Integer.parseInt(properties.getProperty("max_xu_boss"));
        max_xu_clan = Integer.parseInt(properties.getProperty("max_xu_clan"));

        initMap = Byte.parseByte(properties.getProperty("init_map"));
        maxPlayers = Byte.parseByte(properties.getProperty("max_players"));
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public String[] getRoomTypes() {
        return roomTypes;
    }

    @Override
    public String[] getRoomTypesEng() {
        return roomTypesEng;
    }

    @Override
    public byte getEquipVersion2() {
        return equipVersion2;
    }

    @Override
    public byte getIconversion2() {
        return iconversion2;
    }

    @Override
    public byte getLevelCVersion2() {
        return levelCVersion2;
    }

    @Override
    public byte getValuesversion2() {
        return valuesversion2;
    }

    @Override
    public byte getPlayerVersion2() {
        return playerVersion2;
    }

    @Override
    public String getGameInfo() {
        return gameInfo;
    }

    @Override
    public String getGameInfoUrl() {
        return gameInfoUrl;
    }

    @Override
    public String getGameClanUrl() {
        return gameClanUrl;
    }

    @Override
    public int getMaxPlayerFight() {
        return maxPlayersFight;
    }

    @Override
    public int getNumMapBoss() {
        return numMapBoss;
    }

    @Override
    public int getStartMapBoss() {
        return startMapBoss;
    }

    @Override
    public byte[] getMapIdBoss() {
        return mapIdBoss;
    }

    @Override
    public byte getNumbPlayers() {
        return numbPlayers;
    }

    public byte getMaxPlayersFight() {
        return maxPlayersFight;
    }

    @Override
    public String[] getNameRooms() {
        return nameRooms;
    }

    @Override
    public byte getnPlayersInitRoom() {
        return nPlayersInitRoom;
    }

    @Override
    public int getMin_xu_so_cap() {
        return min_xu_so_cap;
    }

    @Override
    public int getMin_xu_trung_cap() {
        return min_xu_trung_cap;
    }

    @Override
    public int getMin_xu_cao_cap() {
        return min_xu_cao_cap;
    }

    @Override
    public int getMin_xu_dau_truong() {
        return min_xu_dau_truong;
    }

    @Override
    public int getMin_xu_tu_do() {
        return min_xu_tu_do;
    }

    @Override
    public int getMin_xu_boss() {
        return min_xu_boss;
    }

    @Override
    public int getMin_xu_clan() {
        return min_xu_clan;
    }

    @Override
    public int getMax_xu_so_cap() {
        return max_xu_so_cap;
    }

    @Override
    public int getMax_xu_trung_cap() {
        return max_xu_trung_cap;
    }

    @Override
    public int getMax_xu_cao_cap() {
        return max_xu_cao_cap;
    }

    @Override
    public int getMax_xu_dau_truong() {
        return max_xu_dau_truong;
    }

    @Override
    public int getMax_xu_tu_do() {
        return max_xu_tu_do;
    }

    @Override
    public int getMax_xu_boss() {
        return max_xu_boss;
    }

    @Override
    public int getMax_xu_clan() {
        return max_xu_clan;
    }

    @Override
    public byte getInitMap() {
        return initMap;
    }

    @Override
    public byte getMaxPlayers() {
        return maxPlayers;
    }
}
