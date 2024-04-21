package com.teamobi.mobiarmy2.config.Impl;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.CommonConstant;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * @author tuyen
 */
public class ServerConfig implements IServerConfig {
    private final Properties configMap;

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
    private byte maxElementFight;
    private byte maxPlayers;
    private byte numbPlayers;
    private boolean mgtBullNew;
    private byte nPlayersInitRoom;
    private byte ltapMap;
    private short[] Xltap, Yltap;
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
    private final String[] roomTypes
            = {"PHÒNG SƠ CẤP", "PHÒNG TRUNG CẤP", "PHÒNG VIP", "PHÒNG ĐẤU TRƯỜNG", "PHÒNG TỰ DO", "PHÒNG ĐẤU TRÙM", "PHÒNG ĐẤU ĐỘI"};
    private final String[] roomTypesEng
            = {"NEWBIE ROOM", "INTERMEDIATE ROOM", "VIP ROOM", "ARENA", "FREEDOM ROOM", "BOSS BATTLE ROOM", "CLAN BATTLE ROOM"};
    private int[] roomTypeStartNum;
    private final String[] nameRooms = {"Bom", "Nhện máy", "Người máy", "T-rex máy", "UFO", "Khí cầu", "Nhện độc", "Ma", "Liên-Hoàn", "Vùng Cấm Địa", "Super Boss", "BOSS Thế Giới", "Hơi Thở Cuối Cùng", "Địa Sơn Vực", "Hoa Quả Sơn"};
    private final int[] nameRoomNumbers = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private final int[] nameRoomTypes = {5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5};
    private int startRoomBoss;
    private int startMapBoss;
    private int numMapBoss;
    private final byte[] mapIdBoss = new byte[]{12, 12, 13, 14, 15, 16, 17, 18, 22, 25, 26, 17, 15, 15, 3, 3};
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

    public ServerConfig(String resourceName) {
        configMap = new Properties();
        try (FileInputStream fis = new FileInputStream(CommonConstant.RESOURCES_PATH + resourceName)) {
            configMap.load(fis);
            initializeConfigProperties();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeConfigProperties() {
        Gson gson = new Gson();
        try {
            if (configMap.containsKey("debug")) {
                debug = Boolean.parseBoolean(configMap.getProperty("debug"));
            } else {
                debug = false;
            }
            if (configMap.containsKey("host")) {
                host = configMap.getProperty("host");
            } else {
                host = "localhost";
            }
            if (configMap.containsKey("post")) {
                port = Short.parseShort(configMap.getProperty("port"));
            } else {
                port = 8122;
            }
            if (configMap.containsKey("mysql-host")) {
                mysql_host = configMap.getProperty("mysql-host");
            } else {
                mysql_host = "localhost";
            }
            if (configMap.containsKey("mysql-user")) {
                mysql_user = configMap.getProperty("mysql-user");
            } else {
                mysql_user = "root";
            }
            if (configMap.containsKey("mysql-password")) {
                mysql_pass = configMap.getProperty("mysql-password");
            } else {
                mysql_pass = "";
            }
            if (configMap.containsKey("mysql-database")) {
                mysql_database = configMap.getProperty("mysql-database");
            } else {
                mysql_database = "dbarmy2";
            }
            if (configMap.containsKey("equipVersion2")) {
                equipVersion2 = Byte.parseByte(configMap.getProperty("equipVersion2"));
            } else {
                equipVersion2 = 1;
            }
            if (configMap.containsKey("iconversion2")) {
                iconversion2 = Byte.parseByte(configMap.getProperty("iconversion2"));
            } else {
                iconversion2 = 1;
            }
            if (configMap.containsKey("levelCVersion2")) {
                levelCVersion2 = Byte.parseByte(configMap.getProperty("levelCVersion2"));
            } else {
                levelCVersion2 = 1;
            }
            if (configMap.containsKey("valuesversion2")) {
                valuesversion2 = Byte.parseByte(configMap.getProperty("valuesversion2"));
            } else {
                valuesversion2 = 1;
            }
            if (configMap.containsKey("playerVersion2")) {
                playerVersion2 = Byte.parseByte(configMap.getProperty("playerVersion2"));
            } else {
                playerVersion2 = 1;
            }
            nRoom = new byte[roomTypes.length];
            nRoomAll = 0;
            startRoomBoss = 0;
            for (int i = 0; i < roomTypes.length; i++) {
                if (configMap.containsKey("n-room-" + i)) {
                    nRoom[i] = Byte.parseByte(configMap.getProperty("n-room-" + i));
                    nRoomAll += nRoom[i];
                    if (i < 5) {
                        startMapBoss += nRoom[i];
                    }
                } else {
                    nRoom[i] = 0;
                }
            }
            if (configMap.containsKey("n-area")) {
                n_area = Byte.parseByte(configMap.getProperty("n-area"));
            } else {
                n_area = 101;
            }
            if (configMap.containsKey("max-player")) {
                maxPlayers = Byte.parseByte(configMap.getProperty("max-player"));
            } else {
                maxPlayers = 8;
            }
            if (configMap.containsKey("max-fight")) {
                maxElementFight = Byte.parseByte(configMap.getProperty("max-fight"));
            } else {
                maxElementFight = 100;
            }
            if (configMap.containsKey("numb-player")) {
                numbPlayers = Byte.parseByte(configMap.getProperty("numb-player"));
            } else {
                numbPlayers = 100;
            }
            if (configMap.containsKey("n-players-init-room")) {
                nPlayersInitRoom = Byte.parseByte(configMap.getProperty("n-players-init-room"));
            } else {
                nPlayersInitRoom = 4;
            }
            Xltap = new short[2];
            Yltap = new short[2];
            if (configMap.containsKey("luyen-tap-map")) {
                ltapMap = Byte.parseByte(configMap.getProperty("luyen-tap-map"));
                Xltap[0] = Short.parseShort(configMap.getProperty("x-ltap"));
                Xltap[1] = Short.parseShort(configMap.getProperty("x-ltap1"));
                Yltap[0] = Short.parseShort(configMap.getProperty("y-ltap"));
                Yltap[1] = Short.parseShort(configMap.getProperty("y-ltap1"));
            } else {
                ltapMap = 0;
            }
            if (configMap.containsKey("init-map")) {
                initMap = Byte.parseByte(configMap.getProperty("init-map"));
            } else {
                initMap = 5;
            }
            if (configMap.containsKey("init-map-boss")) {
                initMapBoss = Byte.parseByte(configMap.getProperty("init-map-boss"));
            } else {
                initMapBoss = (byte) startMapBoss;
            }
            if (configMap.containsKey("start-map-boss")) {
                startMapBoss = Byte.parseByte(configMap.getProperty("start-map-boss"));
            } else {
                startMapBoss = 30;
            }
            if (configMap.containsKey("num-map-boss")) {
                numMapBoss = Byte.parseByte(configMap.getProperty("num-map-boss"));
            } else {
                numMapBoss = 10;
            }
            if (configMap.containsKey("add-info")) {
                addInfo = configMap.getProperty("add-info");
            } else {
                addInfo = "";
            }
            if (configMap.containsKey("add-info-url")) {
                addInfoURL = configMap.getProperty("add-info-url");
            } else {
                addInfoURL = "";
            }
            if (configMap.containsKey("reg-team-url")) {
                regTeamURL = configMap.getProperty("reg-team-url");
            } else {
                regTeamURL = "";
            }
            if (configMap.containsKey("tai-game-name")) {
                taiGameName = configMap.getProperty("tai-game-name");
            } else {
                taiGameName = "";
            }
            if (configMap.containsKey("tai-game-info")) {
                taiGameInfo = configMap.getProperty("tai-game-info");
            } else {
                taiGameInfo = "";
            }
            if (configMap.containsKey("tai-game-url")) {
                taiGameURL = configMap.getProperty("tai-game-url");
            } else {
                taiGameURL = "";
            }
            if (configMap.containsKey("max-clients")) {
                max_clients = Integer.parseInt(configMap.getProperty("max-clients"));
            } else {
                max_clients = 1000;
            }
            if (configMap.containsKey("max-ruong-trang-bi")) {
                max_ruong_tb = Integer.parseInt(configMap.getProperty("max-ruong-trang-bi"));
            } else {
                max_ruong_tb = 100;
            }
            if (configMap.containsKey("max-ruong-item")) {
                max_ruong_item = Integer.parseInt(configMap.getProperty("max-ruong-item"));
            } else {
                max_ruong_item = 100;
            }
            if (configMap.containsKey("max-ruong-itemslot")) {
                max_ruong_itemslot = Integer.parseInt(configMap.getProperty("max-ruong-itemslot"));
            } else {
                max_ruong_itemslot = 30000;
            }
            if (configMap.containsKey("max-item")) {
                max_item = Integer.parseInt(configMap.getProperty("max-item"));
            } else {
                max_item = 99;
            }
            if (configMap.containsKey("max-friends")) {
                max_friends = Integer.parseInt(configMap.getProperty("max-friends"));
            } else {
                max_friends = 60;
            }
            if (configMap.containsKey("mgt-bull-new")) {
                mgtBullNew = Boolean.parseBoolean(configMap.getProperty("mgt-bull-new"));
            } else {
                mgtBullNew = true;
            }
            if (configMap.containsKey("KINH_NGHIEM_UP")) {
                KINH_NGHIEM_UP = Integer.parseInt(configMap.getProperty("KINH_NGHIEM_UP"));
            } else {
                KINH_NGHIEM_UP = 1;
            }
            if (configMap.containsKey("MIN_XU_SO_CAP")) {
                MIN_XU_SO_CAP = Integer.parseInt(configMap.getProperty("MIN_XU_SO_CAP"));
            } else {
                MIN_XU_SO_CAP = 0;
            }
            if (configMap.containsKey("MIN_XU_TRUNG_CAP")) {
                MIN_XU_TRUNG_CAP = Integer.parseInt(configMap.getProperty("MIN_XU_TRUNG_CAP"));
            } else {
                MIN_XU_TRUNG_CAP = 0;
            }
            if (configMap.containsKey("MIN_XU_CAO_CAP")) {
                MIN_XU_CAO_CAP = Integer.parseInt(configMap.getProperty("MIN_XU_CAO_CAP"));
            } else {
                MIN_XU_CAO_CAP = 0;
            }
            if (configMap.containsKey("MIN_XU_DAU_TRUONG")) {
                MIN_XU_DAU_TRUONG = Integer.parseInt(configMap.getProperty("MIN_XU_DAU_TRUONG"));
            } else {
                MIN_XU_DAU_TRUONG = 0;
            }
            if (configMap.containsKey("MIN_XU_TU_DO")) {
                MIN_XU_TU_DO = Integer.parseInt(configMap.getProperty("MIN_XU_TU_DO"));
            } else {
                MIN_XU_TU_DO = 0;
            }
            if (configMap.containsKey("MIN_XU_CLAN")) {
                MIN_XU_CLAN = Integer.parseInt(configMap.getProperty("MIN_XU_CLAN"));
            } else {
                MIN_XU_CLAN = 0;
            }
            if (configMap.containsKey("MIN_XU_BOSS")) {
                MIN_XU_BOSS = Integer.parseInt(configMap.getProperty("MIN_XU_BOSS"));
            } else {
                MIN_XU_BOSS = 0;
            }
            if (configMap.containsKey("MAX_XU_SO_CAP")) {
                MAX_XU_SO_CAP = Integer.parseInt(configMap.getProperty("MAX_XU_SO_CAP"));
            } else {
                MAX_XU_SO_CAP = 0;
            }
            if (configMap.containsKey("MAX_XU_TRUNG_CAP")) {
                MAX_XU_TRUNG_CAP = Integer.parseInt(configMap.getProperty("MAX_XU_TRUNG_CAP"));
            } else {
                MAX_XU_TRUNG_CAP = 0;
            }
            if (configMap.containsKey("MAX_XU_CAO_CAP")) {
                MAX_XU_CAO_CAP = Integer.parseInt(configMap.getProperty("MAX_XU_CAO_CAP"));
            } else {
                MAX_XU_CAO_CAP = 0;
            }
            if (configMap.containsKey("MAX_XU_DAU_TRUONG")) {
                MAX_XU_DAU_TRUONG = Integer.parseInt(configMap.getProperty("MAX_XU_DAU_TRUONG"));
            } else {
                MAX_XU_DAU_TRUONG = 0;
            }
            if (configMap.containsKey("MAX_XU_TU_DO")) {
                MAX_XU_TU_DO = Integer.parseInt(configMap.getProperty("MAX_XU_TU_DO"));
            } else {
                MAX_XU_TU_DO = 0;
            }
            if (configMap.containsKey("MAX_XU_CLAN")) {
                MAX_XU_CLAN = Integer.parseInt(configMap.getProperty("MAX_XU_CLAN"));
            } else {
                MAX_XU_CLAN = 0;
            }
            if (configMap.containsKey("MAX_XU_BOSS")) {
                MAX_XU_BOSS = Integer.parseInt(configMap.getProperty("MAX_XU_BOSS"));
            } else {
                MAX_XU_BOSS = 0;
            }
            if (configMap.containsKey("MIN_TIME_X6_1")) {
                MIN_TIME_X6_1 = Integer.parseInt(configMap.getProperty("MIN_TIME_X6_1"));
            } else {
                MIN_TIME_X6_1 = 0;
            }
            if (configMap.containsKey("MIN_TIME_X6_2")) {
                MIN_TIME_X6_2 = Integer.parseInt(configMap.getProperty("MIN_TIME_X6_2"));
            } else {
                MIN_TIME_X6_2 = 0;
            }
            if (configMap.containsKey("MIN_TIME_X6_3")) {
                MIN_TIME_X6_3 = Integer.parseInt(configMap.getProperty("MIN_TIME_X6_3"));
            } else {
                MIN_TIME_X6_3 = 0;
            }
            if (configMap.containsKey("MAX_TIME_X6_1")) {
                MAX_TIME_X6_1 = Integer.parseInt(configMap.getProperty("MAX_TIME_X6_1"));
            } else {
                MAX_TIME_X6_1 = 0;
            }
            if (configMap.containsKey("MAX_TIME_X6_2")) {
                MAX_TIME_X6_2 = Integer.parseInt(configMap.getProperty("MAX_TIME_X6_2"));
            } else {
                MAX_TIME_X6_2 = 0;
            }
            if (configMap.containsKey("MAX_TIME_X6_3")) {
                MAX_TIME_X6_3 = Integer.parseInt(configMap.getProperty("MAX_TIME_X6_3"));
            } else {
                MAX_TIME_X6_3 = 0;
            }
            if (configMap.containsKey("XU_NHAN_DUOC")) {
                XU_NHAN_DUOC = Integer.parseInt(configMap.getProperty("XU_NHAN_DUOC"));
            } else {
                XU_NHAN_DUOC = 0;
            }
            if (configMap.containsKey("LUONG_NHAN_DUOC")) {
                LUONG_NHAN_DUOC = Integer.parseInt(configMap.getProperty("LUONG_NHAN_DUOC"));
            } else {
                LUONG_NHAN_DUOC = 0;
            }
            if (configMap.containsKey("XU_BI_TRU")) {
                XU_BI_TRU = Integer.parseInt(configMap.getProperty("XU_BI_TRU"));
            } else {
                XU_BI_TRU = 0;
            }
            if (configMap.containsKey("LUONG_BI_TRU")) {
                LUONG_BI_TRU = Integer.parseInt(configMap.getProperty("LUONG_BI_TRU"));
            } else {
                LUONG_BI_TRU = 0;
            }
            if (configMap.containsKey("SEND_CHAT_LOGIN")) {
                SEND_CHAT_LOGIN = configMap.getProperty("SEND_CHAT_LOGIN");
            } else {
                SEND_CHAT_LOGIN = "";
            }
            if (configMap.containsKey("SEND_THU1")) {
                SEND_THU1 = configMap.getProperty("SEND_THU1");
            } else {
                SEND_THU1 = "";
            }
            if (configMap.containsKey("SEND_THU2")) {
                SEND_THU2 = configMap.getProperty("SEND_THU2");
            } else {
                SEND_THU2 = "";
            }
            if (configMap.containsKey("SEND_THU3")) {
                SEND_THU3 = configMap.getProperty("SEND_THU3");
            } else {
                SEND_THU3 = "";
            }
            if (configMap.containsKey("SEND_THU4")) {
                SEND_THU4 = configMap.getProperty("SEND_THU4");
            } else {
                SEND_THU4 = "";
            }
            if (configMap.containsKey("SEND_THU5")) {
                SEND_THU5 = configMap.getProperty("SEND_THU5");
            } else {
                SEND_THU5 = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public byte getN_area() {
        return n_area;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public short getPort() {
        return port;
    }

    @Override
    public String getMysql_host() {
        return mysql_host;
    }

    @Override
    public String getMysql_user() {
        return mysql_user;
    }

    @Override
    public String getMysql_pass() {
        return mysql_pass;
    }

    @Override
    public String getMysql_database() {
        return mysql_database;
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
    public byte[] getnRoom() {
        return nRoom;
    }

    @Override
    public byte getnRoomAll() {
        return nRoomAll;
    }

    @Override
    public byte getMaxElementFight() {
        return maxElementFight;
    }

    @Override
    public byte getMaxPlayers() {
        return maxPlayers;
    }

    @Override
    public byte getNumbPlayers() {
        return numbPlayers;
    }

    @Override
    public boolean isMgtBullNew() {
        return mgtBullNew;
    }

    @Override
    public byte getnPlayersInitRoom() {
        return nPlayersInitRoom;
    }

    @Override
    public byte getLtapMap() {
        return ltapMap;
    }

    @Override
    public short[] getXltap() {
        return Xltap;
    }

    @Override
    public short[] getYltap() {
        return Yltap;
    }

    @Override
    public byte getInitMap() {
        return initMap;
    }

    @Override
    public byte getInitMapBoss() {
        return initMapBoss;
    }

    @Override
    public String getAddInfo() {
        return addInfo;
    }

    @Override
    public String getAddInfoURL() {
        return addInfoURL;
    }

    @Override
    public String getRegTeamURL() {
        return regTeamURL;
    }

    @Override
    public String getTaiGameName() {
        return taiGameName;
    }

    @Override
    public String getTaiGameInfo() {
        return taiGameInfo;
    }

    @Override
    public String getTaiGameURL() {
        return taiGameURL;
    }

    @Override
    public int getMax_clients() {
        return max_clients;
    }

    @Override
    public int getMax_ruong_tb() {
        return max_ruong_tb;
    }

    @Override
    public int getMax_ruong_item() {
        return max_ruong_item;
    }

    @Override
    public int getMax_ruong_itemslot() {
        return max_ruong_itemslot;
    }

    @Override
    public int getMax_item() {
        return max_item;
    }

    @Override
    public int getMax_friends() {
        return max_friends;
    }

    @Override
    public int getNumClients() {
        return numClients;
    }

    @Override
    public boolean isStart() {
        return start;
    }

    @Override
    public int getId() {
        return id;
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
    public int[] getRoomTypeStartNum() {
        return roomTypeStartNum;
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
    public int getStartRoomBoss() {
        return startRoomBoss;
    }

    @Override
    public int getStartMapBoss() {
        return startMapBoss;
    }

    @Override
    public int getNumMapBoss() {
        return numMapBoss;
    }

    @Override
    public byte[] getMapIdBoss() {
        return mapIdBoss;
    }

    @Override
    public int getMIN_XU_BOSS() {
        return MIN_XU_BOSS;
    }

    @Override
    public int getMAX_XU_BOSS() {
        return MAX_XU_BOSS;
    }

    @Override
    public int getMIN_XU_SO_CAP() {
        return MIN_XU_SO_CAP;
    }

    @Override
    public int getMAX_XU_SO_CAP() {
        return MAX_XU_SO_CAP;
    }

    @Override
    public int getMIN_XU_TRUNG_CAP() {
        return MIN_XU_TRUNG_CAP;
    }

    @Override
    public int getMAX_XU_TRUNG_CAP() {
        return MAX_XU_TRUNG_CAP;
    }

    @Override
    public int getMIN_XU_CAO_CAP() {
        return MIN_XU_CAO_CAP;
    }

    @Override
    public int getMAX_XU_CAO_CAP() {
        return MAX_XU_CAO_CAP;
    }

    @Override
    public int getMIN_XU_DAU_TRUONG() {
        return MIN_XU_DAU_TRUONG;
    }

    @Override
    public int getMAX_XU_DAU_TRUONG() {
        return MAX_XU_DAU_TRUONG;
    }

    @Override
    public int getMIN_XU_TU_DO() {
        return MIN_XU_TU_DO;
    }

    @Override
    public int getMAX_XU_TU_DO() {
        return MAX_XU_TU_DO;
    }

    @Override
    public int getMIN_XU_CLAN() {
        return MIN_XU_CLAN;
    }

    @Override
    public int getMAX_XU_CLAN() {
        return MAX_XU_CLAN;
    }

    @Override
    public int getMIN_TIME_X6_1() {
        return MIN_TIME_X6_1;
    }

    @Override
    public int getMAX_TIME_X6_1() {
        return MAX_TIME_X6_1;
    }

    @Override
    public int getMIN_TIME_X6_2() {
        return MIN_TIME_X6_2;
    }

    @Override
    public int getMAX_TIME_X6_2() {
        return MAX_TIME_X6_2;
    }

    @Override
    public int getMIN_TIME_X6_3() {
        return MIN_TIME_X6_3;
    }

    @Override
    public int getMAX_TIME_X6_3() {
        return MAX_TIME_X6_3;
    }

    @Override
    public int getXU_NHAN_DUOC() {
        return XU_NHAN_DUOC;
    }

    @Override
    public int getLUONG_NHAN_DUOC() {
        return LUONG_NHAN_DUOC;
    }

    @Override
    public int getXU_BI_TRU() {
        return XU_BI_TRU;
    }

    @Override
    public int getLUONG_BI_TRU() {
        return LUONG_BI_TRU;
    }

    @Override
    public String getSEND_CHAT_LOGIN() {
        return SEND_CHAT_LOGIN;
    }

    @Override
    public String getSEND_THU1() {
        return SEND_THU1;
    }

    @Override
    public String getSEND_THU2() {
        return SEND_THU2;
    }

    @Override
    public String getSEND_THU3() {
        return SEND_THU3;
    }

    @Override
    public String getSEND_THU4() {
        return SEND_THU4;
    }

    @Override
    public String getSEND_THU5() {
        return SEND_THU5;
    }

    @Override
    public int getKINH_NGHIEM_UP() {
        return KINH_NGHIEM_UP;
    }
}
