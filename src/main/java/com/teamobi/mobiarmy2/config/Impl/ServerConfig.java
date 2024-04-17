package com.teamobi.mobiarmy2.config.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ServerConfig implements IServerConfig {
    private final String directoryPath = "src/main/resources/";
    private boolean debug;
    private byte n_area;
    private String host;
    private short port;
    private String mysql_host;
    private String mysql_user;
    private String mysql_pass;
    private String mysql_database;
    private byte equipVersion2;
    private byte iconversion2;
    private byte levelCVersion2;
    private byte valuesversion2;
    private byte playerVersion2;
    private byte[] nRoom;
    private byte nRoomAll;
    public byte maxElementFight;
    public byte maxPlayers;
    public byte numbPlayers;
    public boolean mgtBullNew;
    private byte nPlayersInitRoom;
    private byte ltapMap;
    public short[] Xltap, Yltap;
    private byte initMap;
    private byte initMapBoss;
    private String addInfo;
    private String addInfoURL;
    private String regTeamURL;
    private String taiGameName;
    private String taiGameInfo;
    private String taiGameURL;
    private int max_clients;
    private int max_ruong_tb;
    private int max_ruong_item;
    private int max_ruong_itemslot;
    private int max_item;
    private int max_friends;
    private int numClients;
    private boolean start;
    private int id;
    private final String[] roomTypes = {"PHÒNG SƠ CẤP", "PHÒNG TRUNG CẤP", "PHÒNG VIP", "PHÒNG ĐẤU TRƯỜNG", "PHÒNG TỰ DO", "PHÒNG ĐẤU TRÙM", "PHÒNG ĐẤU ĐỘI"};
    private final String[] roomTypesEng = {"NEWBIE ROOM", "INTERMEDIATE ROOM", "VIP ROOM", "ARENA", "FREEDOM ROOM", "BOSS BATTLE ROOM", "CLAN BATTLE ROOM"};
    private final String[] nameRooms = {"Bom", "Nhện máy", "Người máy", "T-rex máy", "UFO", "Khí cầu", "Nhện độc", "Ma", "Đấu trùm liên hoàn"};
    private int[] roomTypeStartNum;
    private final int[] nameRoomNumbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private final int[] nameRoomTypes = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
    public byte[] mapIdBoss = new byte[]{12, 12, 13, 14, 15, 16, 17, 18, 22, 25, 26, 17, 15, 15, 3, 3};
    public int startRoomBoss;
    public byte startMapBoss;
    public byte numMapBoss;
    private int MIN_XU_BOSS;
    private int MAX_XU_BOSS;
    private int MIN_XU_SO_CAP;
    private int MAX_XU_SO_CAP;
    private int MIN_XU_TRUNG_CAP;
    private int MAX_XU_TRUNG_CAP;
    private int MIN_XU_CAO_CAP;
    private int MAX_XU_CAO_CAP;
    private int MIN_XU_DAU_TRUONG;
    private int MAX_XU_DAU_TRUONG;
    private int MIN_XU_TU_DO;
    private int MAX_XU_TU_DO;
    private int MIN_XU_CLAN;
    private int MAX_XU_CLAN;
    private int MIN_TIME_X6_1;
    private int MAX_TIME_X6_1;
    private int MIN_TIME_X6_2;
    private int MAX_TIME_X6_2;
    private int MIN_TIME_X6_3;
    private int MAX_TIME_X6_3;
    private int XU_NHAN_DUOC;
    private int LUONG_NHAN_DUOC;
    private int XU_BI_TRU;
    private int LUONG_BI_TRU;
    private String SEND_CHAT_LOGIN;
    private String SEND_THU1;
    private String SEND_THU2;
    private String SEND_THU3;
    private String SEND_THU4;
    private String SEND_THU5;
    private int KINH_NGHIEM_UP;

    private final Properties properties;

    public ServerConfig(String resourceName) {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(directoryPath + resourceName)) {
            properties.load(fis);
            initializeConfigProperties();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initializeConfigProperties() {
        debug = Boolean.parseBoolean(properties.getProperty("debug"));
        n_area = Byte.parseByte(properties.getProperty("n_area"));
        host = properties.getProperty("host");
        port = Short.parseShort(properties.getProperty("port"));
        mysql_host = properties.getProperty("mysql_host");
        mysql_user = properties.getProperty("mysql_user");
        mysql_pass = properties.getProperty("mysql_pass");
        mysql_database = properties.getProperty("mysql_database");
        equipVersion2 = Byte.parseByte(properties.getProperty("equipVersion2"));
        iconversion2 = Byte.parseByte(properties.getProperty("iconversion2"));
        levelCVersion2 = Byte.parseByte(properties.getProperty("levelCVersion2"));
        valuesversion2 = Byte.parseByte(properties.getProperty("valuesversion2"));
        playerVersion2 = Byte.parseByte(properties.getProperty("playerVersion2"));
        nRoom = properties.getProperty("nRoom").getBytes();
        nRoomAll = Byte.parseByte(properties.getProperty("nRoomAll"));
        maxElementFight = Byte.parseByte(properties.getProperty("maxElementFight"));
        maxPlayers = Byte.parseByte(properties.getProperty("maxPlayers"));
        numbPlayers = Byte.parseByte(properties.getProperty("numbPlayers"));
        mgtBullNew = Boolean.parseBoolean(properties.getProperty("mgtBullNew"));
        nPlayersInitRoom = Byte.parseByte(properties.getProperty("nPlayersInitRoom"));
        ltapMap = Byte.parseByte(properties.getProperty("ltapMap"));
        Xltap = parseShortArray(properties.getProperty("Xltap"));
        Yltap = parseShortArray(properties.getProperty("Yltap"));
        initMap = Byte.parseByte(properties.getProperty("initMap"));
        initMapBoss = Byte.parseByte(properties.getProperty("initMapBoss"));
        addInfo = properties.getProperty("addInfo");
        addInfoURL = properties.getProperty("addInfoURL");
        regTeamURL = properties.getProperty("regTeamURL");
        taiGameName = properties.getProperty("taiGameName");
        taiGameInfo = properties.getProperty("taiGameInfo");
        taiGameURL = properties.getProperty("taiGameURL");
        max_clients = Integer.parseInt(properties.getProperty("max_clients"));
        max_ruong_tb = Integer.parseInt(properties.getProperty("max_ruong_tb"));
        max_ruong_item = Integer.parseInt(properties.getProperty("max_ruong_item"));
        max_ruong_itemslot = Integer.parseInt(properties.getProperty("max_ruong_itemslot"));
        max_item = Integer.parseInt(properties.getProperty("max_item"));
        max_friends = Integer.parseInt(properties.getProperty("max_friends"));
        numClients = Integer.parseInt(properties.getProperty("numClients"));
        start = Boolean.parseBoolean(properties.getProperty("start"));
        id = Integer.parseInt(properties.getProperty("id"));
        startRoomBoss = Integer.parseInt(properties.getProperty("startRoomBoss"));
        startMapBoss = Byte.parseByte(properties.getProperty("startMapBoss"));
        numMapBoss = Byte.parseByte(properties.getProperty("numMapBoss"));
        MIN_XU_BOSS = Integer.parseInt(properties.getProperty("MIN_XU_BOSS"));
        MAX_XU_BOSS = Integer.parseInt(properties.getProperty("MAX_XU_BOSS"));
        MIN_XU_SO_CAP = Integer.parseInt(properties.getProperty("MIN_XU_SO_CAP"));
        MAX_XU_SO_CAP = Integer.parseInt(properties.getProperty("MAX_XU_SO_CAP"));
        MIN_XU_TRUNG_CAP = Integer.parseInt(properties.getProperty("MIN_XU_TRUNG_CAP"));
        MAX_XU_TRUNG_CAP = Integer.parseInt(properties.getProperty("MAX_XU_TRUNG_CAP"));
        MIN_XU_CAO_CAP = Integer.parseInt(properties.getProperty("MIN_XU_CAO_CAP"));
        MAX_XU_CAO_CAP = Integer.parseInt(properties.getProperty("MAX_XU_CAO_CAP"));
        MIN_XU_DAU_TRUONG = Integer.parseInt(properties.getProperty("MIN_XU_DAU_TRUONG"));
        MAX_XU_DAU_TRUONG = Integer.parseInt(properties.getProperty("MAX_XU_DAU_TRUONG"));
        MIN_XU_TU_DO = Integer.parseInt(properties.getProperty("MIN_XU_TU_DO"));
        MAX_XU_TU_DO = Integer.parseInt(properties.getProperty("MAX_XU_TU_DO"));
        MIN_XU_CLAN = Integer.parseInt(properties.getProperty("MIN_XU_CLAN"));
        MAX_XU_CLAN = Integer.parseInt(properties.getProperty("MAX_XU_CLAN"));
        MIN_TIME_X6_1 = Integer.parseInt(properties.getProperty("MIN_TIME_X6_1"));
        MAX_TIME_X6_1 = Integer.parseInt(properties.getProperty("MAX_TIME_X6_1"));
        MIN_TIME_X6_2 = Integer.parseInt(properties.getProperty("MIN_TIME_X6_2"));
        MAX_TIME_X6_2 = Integer.parseInt(properties.getProperty("MAX_TIME_X6_2"));
        MIN_TIME_X6_3 = Integer.parseInt(properties.getProperty("MIN_TIME_X6_3"));
        MAX_TIME_X6_3 = Integer.parseInt(properties.getProperty("MAX_TIME_X6_3"));
        XU_NHAN_DUOC = Integer.parseInt(properties.getProperty("XU_NHAN_DUOC"));
        LUONG_NHAN_DUOC = Integer.parseInt(properties.getProperty("LUONG_NHAN_DUOC"));
        XU_BI_TRU = Integer.parseInt(properties.getProperty("XU_BI_TRU"));
        LUONG_BI_TRU = Integer.parseInt(properties.getProperty("LUONG_BI_TRU"));
        SEND_CHAT_LOGIN = properties.getProperty("SEND_CHAT_LOGIN");
        SEND_THU1 = properties.getProperty("SEND_THU1");
        SEND_THU2 = properties.getProperty("SEND_THU2");
        SEND_THU3 = properties.getProperty("SEND_THU3");
        SEND_THU4 = properties.getProperty("SEND_THU4");
        SEND_THU5 = properties.getProperty("SEND_THU5");
        KINH_NGHIEM_UP = Integer.parseInt(properties.getProperty("KINH_NGHIEM_UP"));
    }

    private short[] parseShortArray(String property) {
        String[] tokens = property.split(",");
        short[] array = new short[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            array[i] = Short.parseShort(tokens[i].trim());
        }
        return array;
    }

    @Override
    public int getPort() {
        return port;
    }


    @Override
    public byte getMaxElementFight() {
        return maxElementFight;
    }


    @Override
    public boolean isDebug() {
        return debug;
    }


    @Override
    public byte getNumMapBoss() {
        return numMapBoss;
    }


    @Override
    public byte getStartMapBoss() {
        return startMapBoss;
    }


    @Override
    public byte getNumbPlayers() {
        return numbPlayers;
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
    public String[] getNameRooms() {
        return nameRooms;
    }

    @Override
    public int[] getNameRoomNumbers() {
        return nameRoomNumbers;
    }

    @Override
    public int[] getNameRoomTypes() {
        return nameRoomTypes;
    }

    @Override
    public byte[] getNRoom() {
        return nRoom;
    }

    @Override
    public int[] getRoomTypeStartNum() {
        return roomTypeStartNum;
    }

    @Override
    public byte[] getMapIdBoss() {
        return mapIdBoss;
    }
}
