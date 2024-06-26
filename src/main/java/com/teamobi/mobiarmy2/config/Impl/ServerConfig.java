package com.teamobi.mobiarmy2.config.Impl;

import com.teamobi.mobiarmy2.config.IServerConfig;
import com.teamobi.mobiarmy2.constant.CommonConstant;
import com.teamobi.mobiarmy2.util.GsonUtil;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author tuyen
 */
public class ServerConfig implements IServerConfig {

    private final Properties configMap;
    private boolean debug;
    private short port;
    private byte equipVersion2;
    private byte iconVersion2;
    private byte levelCVersion2;
    private byte valuesVersion2;
    private byte playerVersion2;
    private String[] roomNameVi;
    private String[] roomNameEn;
    private String[] roomBossName;
    private byte[] roomQuantity;
    private int[] roomMaxXu;
    private int[] roomMinXu;
    private byte[] mapIdBoss;
    private byte numArea;
    private byte maxPlayerFight;
    private byte maxElementFight;
    private byte numPlayer;
    private byte numPlayerInitRoom;
    private byte startMapBoss;
    private byte numMapBoss;
    private byte initMapId;
    private byte initMapBoss;
    private String addInfo;
    private String addInfoUrl;
    private String regTeamUrl;
    private String downloadTitle;
    private String downloadInfo;
    private String downloadUrl;
    private int maxClients;
    private byte maxRuongTrangBi;
    private byte maxRuongItem;
    private byte maxItem;
    private byte maxFriends;
    private String messageLogin;
    private String[] message;

    public ServerConfig(String resourceName) {
        configMap = new Properties();
        try (FileInputStream fis = new FileInputStream(CommonConstant.RESOURCES_PATH + resourceName);
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)
        ) {
            configMap.load(isr);
            initializeConfigProperties();
            validateConfigProperties();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initializeConfigProperties() {
        try {
            debug = Boolean.parseBoolean(configMap.getProperty("debug", "false"));
            port = Short.parseShort(configMap.getProperty("port", "8122"));

            equipVersion2 = Byte.parseByte(configMap.getProperty("equip_version_2", "1"));
            iconVersion2 = Byte.parseByte(configMap.getProperty("icon_version_2", "1"));
            levelCVersion2 = Byte.parseByte(configMap.getProperty("levelc_version_2", "1"));
            valuesVersion2 = Byte.parseByte(configMap.getProperty("values_version_2", "1"));
            playerVersion2 = Byte.parseByte(configMap.getProperty("player_version_2", "1"));

            roomNameVi = GsonUtil.GSON.fromJson(configMap.getProperty("room_name_vi", "[]"), String[].class);
            roomNameEn = GsonUtil.GSON.fromJson(configMap.getProperty("room_name_en", "[]"), String[].class);
            roomBossName = GsonUtil.GSON.fromJson(configMap.getProperty("room_boss_name", "[]"), String[].class);
            roomQuantity = GsonUtil.GSON.fromJson(configMap.getProperty("room_quantity", "[]"), byte[].class);
            roomMaxXu = GsonUtil.GSON.fromJson(configMap.getProperty("room_max_xu", "[]"), int[].class);
            roomMinXu = GsonUtil.GSON.fromJson(configMap.getProperty("room_min_xu", "[]"), int[].class);

            numArea = Byte.parseByte(configMap.getProperty("num_area", "20"));
            maxPlayerFight = Byte.parseByte(configMap.getProperty("max_player_fight", "8"));
            maxElementFight = Byte.parseByte(configMap.getProperty("max_element_fight", "100"));
            numPlayer = Byte.parseByte(configMap.getProperty("num_player", "12"));
            numPlayerInitRoom = Byte.parseByte(configMap.getProperty("num_player_init_room", "4"));
            startMapBoss = Byte.parseByte(configMap.getProperty("start_map_boss", "30"));
            numMapBoss = Byte.parseByte(configMap.getProperty("num_map_boss", "10"));
            initMapId = Byte.parseByte(configMap.getProperty("init_map_id", "0"));
            initMapBoss = Byte.parseByte(configMap.getProperty("init_map_boss", "30"));

            addInfo = configMap.getProperty("add_info", "ABOUT ME");
            addInfoUrl = configMap.getProperty("add_info_url", "http://localhost/about");
            regTeamUrl = configMap.getProperty("reg_team_url", "http://localhost/register");
            downloadTitle = configMap.getProperty("download_title", "TẢi GAME");
            downloadInfo = configMap.getProperty("download_info", "Ko có thông tin");
            downloadUrl = configMap.getProperty("download_url", "http://localhost");

            maxClients = Integer.parseInt(configMap.getProperty("max_clients", "1000"));
            maxRuongTrangBi = Byte.parseByte(configMap.getProperty("max_ruong_trang_bi", "100"));
            maxRuongItem = Byte.parseByte(configMap.getProperty("max_ruong_item", "100"));
            maxItem = Byte.parseByte(configMap.getProperty("max_item", "99"));
            maxFriends = Byte.parseByte(configMap.getProperty("max_friends", "10"));

            messageLogin = configMap.getProperty("message_login", "");
            message = GsonUtil.GSON.fromJson(configMap.getProperty("message", "[]"), String[].class);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void validateConfigProperties() {
    }

    public boolean isDebug() {
        return debug;
    }

    public short getPort() {
        return port;
    }

    public byte getEquipVersion2() {
        return equipVersion2;
    }

    public byte getIconVersion2() {
        return iconVersion2;
    }

    public byte getLevelCVersion2() {
        return levelCVersion2;
    }

    public byte getValuesVersion2() {
        return valuesVersion2;
    }

    public byte getPlayerVersion2() {
        return playerVersion2;
    }

    public String[] getRoomNameVi() {
        return roomNameVi;
    }

    public String[] getRoomNameEn() {
        return roomNameEn;
    }

    public String[] getRoomBossName() {
        return roomBossName;
    }

    public byte[] getRoomQuantity() {
        return roomQuantity;
    }

    public int[] getRoomMaxXu() {
        return roomMaxXu;
    }

    public int[] getRoomMinXu() {
        return roomMinXu;
    }

    public byte[] getMapIdBoss() {
        return mapIdBoss;
    }

    public byte getNumArea() {
        return numArea;
    }

    public byte getMaxPlayerFight() {
        return maxPlayerFight;
    }

    public byte getMaxElementFight() {
        return maxElementFight;
    }

    public byte getNumPlayer() {
        return numPlayer;
    }

    public byte getNumPlayerInitRoom() {
        return numPlayerInitRoom;
    }

    public byte getStartMapBoss() {
        return startMapBoss;
    }

    public byte getNumMapBoss() {
        return numMapBoss;
    }

    public byte getInitMapId() {
        return initMapId;
    }

    public byte getInitMapBoss() {
        return initMapBoss;
    }

    public String getAddInfo() {
        return addInfo;
    }

    public String getAddInfoUrl() {
        return addInfoUrl;
    }

    public String getRegTeamUrl() {
        return regTeamUrl;
    }

    public String getDownloadTitle() {
        return downloadTitle;
    }

    public String getDownloadInfo() {
        return downloadInfo;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public int getMaxClients() {
        return maxClients;
    }

    public byte getMaxRuongTrangBi() {
        return maxRuongTrangBi;
    }

    public byte getMaxRuongItem() {
        return maxRuongItem;
    }

    public byte getMaxItem() {
        return maxItem;
    }

    public byte getMaxFriends() {
        return maxFriends;
    }

    public String getMessageLogin() {
        return messageLogin;
    }

    public String[] getMessage() {
        return message;
    }
}