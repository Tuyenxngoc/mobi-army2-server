package com.teamobi.mobiarmy2.config.Impl;

import com.google.gson.Gson;
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
    private String[] bossRoomName;
    private byte[][] bossRoomMapLimit;
    private byte[] roomQuantity;
    private int[] roomMaxXu;
    private int[] roomMinXu;
    private byte[] roomMaxMap;
    private byte[] roomMinMap;
    private byte roomIconType;
    private byte[] bossRoomBossId;
    private byte numArea;
    private byte maxPlayerFight;
    private byte maxElementFight;
    private byte numPlayer;
    private byte numPlayerInitRoom;
    private byte startMapBoss;
    private byte[] bossRoomMapId;
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
        Gson gson = GsonUtil.GSON;
        try {
            debug = Boolean.parseBoolean(configMap.getProperty("debug", "false"));
            port = Short.parseShort(configMap.getProperty("port", "8122"));

            equipVersion2 = Byte.parseByte(configMap.getProperty("equip_version_2", "1"));
            iconVersion2 = Byte.parseByte(configMap.getProperty("icon_version_2", "1"));
            levelCVersion2 = Byte.parseByte(configMap.getProperty("levelc_version_2", "1"));
            valuesVersion2 = Byte.parseByte(configMap.getProperty("values_version_2", "1"));
            playerVersion2 = Byte.parseByte(configMap.getProperty("player_version_2", "1"));

            roomNameVi = gson.fromJson(configMap.getProperty("room_name_vi", "[]"), String[].class);
            roomNameEn = gson.fromJson(configMap.getProperty("room_name_en", "[]"), String[].class);
            roomQuantity = gson.fromJson(configMap.getProperty("room_quantity", "[]"), byte[].class);

            bossRoomName = gson.fromJson(configMap.getProperty("boss_room_name", "[]"), String[].class);
            bossRoomMapLimit = gson.fromJson(configMap.getProperty("boss_room_map_limit", "[]"), byte[][].class);
            bossRoomBossId = gson.fromJson(configMap.getProperty("boss_room_boss_id", "[]"), byte[].class);
            bossRoomMapId = gson.fromJson(configMap.getProperty("boss_room_map_id", "[]"), byte[].class);

            roomMaxXu = gson.fromJson(configMap.getProperty("room_max_xu", "[]"), int[].class);
            roomMinXu = gson.fromJson(configMap.getProperty("room_min_xu", "[]"), int[].class);
            roomMaxMap = gson.fromJson(configMap.getProperty("room_max_map", "[]"), byte[].class);
            roomMinMap = gson.fromJson(configMap.getProperty("room_min_map", "[]"), byte[].class);
            roomIconType = gson.fromJson(configMap.getProperty("room_icon_type", "0"), byte.class);

            for (int i = 0; i < roomNameVi.length - 2; i++) {
                startMapBoss += roomQuantity[i];
            }

            numArea = Byte.parseByte(configMap.getProperty("num_area", "20"));
            maxPlayerFight = Byte.parseByte(configMap.getProperty("max_player_fight", "8"));
            maxElementFight = Byte.parseByte(configMap.getProperty("max_element_fight", "100"));
            numPlayer = Byte.parseByte(configMap.getProperty("num_player", "12"));
            numPlayerInitRoom = Byte.parseByte(configMap.getProperty("num_player_init_room", "4"));
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
            message = gson.fromJson(configMap.getProperty("message", "[]"), String[].class);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void validateConfigProperties() {
        int totalRoomTypes = roomNameVi.length;
        if (roomNameEn.length != totalRoomTypes ||
                roomQuantity.length != totalRoomTypes ||
                roomMaxXu.length != totalRoomTypes ||
                roomMinXu.length != totalRoomTypes ||
                roomMinMap.length != totalRoomTypes ||
                roomMaxMap.length != totalRoomTypes
        ) {
            System.out.println("room_name_vi, room_name_en, room_quantity, room_max_xu, room_min_xu, room_max_map, room_min_map must have the same length");
            System.out.println("room_name_vi: " + roomNameVi.length);
            System.out.println("room_name_en:" + roomNameEn.length);
            System.out.println("room_quantity:" + roomQuantity.length);
            System.out.println("room_max_xu:" + roomMaxXu.length);
            System.out.println("room_min_xu:" + roomMinXu.length);
            System.out.println("room_max_map:" + roomMinMap.length);
            System.out.println("room_min_map:" + roomMaxMap.length);
            System.exit(1);
        }

        if (bossRoomMapId.length != bossRoomBossId.length) {
            System.out.println("room_boss_id, map_boss_id must have the same length");
            System.exit(1);
        }
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    public byte[][] getBossRoomMapLimit() {
        return bossRoomMapLimit;
    }

    public byte getRoomIconType() {
        return roomIconType;
    }

    @Override
    public short getPort() {
        return port;
    }

    @Override
    public byte getEquipVersion2() {
        return equipVersion2;
    }

    @Override
    public byte getIconVersion2() {
        return iconVersion2;
    }

    @Override
    public byte getLevelCVersion2() {
        return levelCVersion2;
    }

    @Override
    public byte getValuesVersion2() {
        return valuesVersion2;
    }

    @Override
    public byte getPlayerVersion2() {
        return playerVersion2;
    }

    @Override
    public String[] getRoomNameVi() {
        return roomNameVi;
    }

    @Override
    public String[] getRoomNameEn() {
        return roomNameEn;
    }

    @Override
    public String[] getBossRoomName() {
        return bossRoomName;
    }

    @Override
    public byte[] getRoomQuantity() {
        return roomQuantity;
    }

    @Override
    public int[] getRoomMaxXu() {
        return roomMaxXu;
    }

    @Override
    public int[] getRoomMinXu() {
        return roomMinXu;
    }

    @Override
    public byte[] getRoomMaxMap() {
        return roomMaxMap;
    }

    @Override
    public byte[] getRoomMinMap() {
        return roomMinMap;
    }

    @Override
    public byte[] getBossRoomBossId() {
        return bossRoomBossId;
    }

    @Override
    public byte getNumArea() {
        return numArea;
    }

    @Override
    public byte getMaxPlayerFight() {
        return maxPlayerFight;
    }

    @Override
    public byte getMaxElementFight() {
        return maxElementFight;
    }

    @Override
    public byte getNumPlayer() {
        return numPlayer;
    }

    @Override
    public byte getNumPlayerInitRoom() {
        return numPlayerInitRoom;
    }

    @Override
    public byte getStartMapBoss() {
        return startMapBoss;
    }

    @Override
    public byte getInitMapId() {
        return initMapId;
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
    public String getAddInfoUrl() {
        return addInfoUrl;
    }

    @Override
    public String getRegTeamUrl() {
        return regTeamUrl;
    }

    @Override
    public String getDownloadTitle() {
        return downloadTitle;
    }

    @Override
    public String getDownloadInfo() {
        return downloadInfo;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public int getMaxClients() {
        return maxClients;
    }

    @Override
    public byte getMaxRuongTrangBi() {
        return maxRuongTrangBi;
    }

    @Override
    public byte getMaxRuongItem() {
        return maxRuongItem;
    }

    @Override
    public byte getMaxItem() {
        return maxItem;
    }

    @Override
    public byte getMaxFriends() {
        return maxFriends;
    }

    @Override
    public String getMessageLogin() {
        return messageLogin;
    }

    @Override
    public String[] getMessage() {
        return message;
    }

    @Override
    public byte[] getBossRoomMapId() {
        return bossRoomMapId;
    }
}