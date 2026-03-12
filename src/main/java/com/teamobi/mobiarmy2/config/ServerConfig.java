package com.teamobi.mobiarmy2.config;

import com.google.gson.Gson;
import com.teamobi.mobiarmy2.constant.GameConstants;
import com.teamobi.mobiarmy2.util.GsonUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Getter
@Setter
public class ServerConfig {
    private final Properties configMap;
    private boolean debug;
    private short port;
    private byte equipVersion2;
    private byte iconVersion2;
    private byte levelCVersion2;
    private byte valuesVersion2;
    private byte playerVersion2;
    private int maxClients;
    private String messageLogin;
    private String[] message;
    private String addInfo;
    private String addInfoUrl;
    private String regTeamUrl;
    private String downloadTitle;
    private String downloadInfo;
    private String downloadUrl;
    private LocalDateTime tetStartTime;
    private LocalDateTime tetEndTime;
    private boolean isTet;

    public ServerConfig() {
        configMap = new Properties();
        try (FileInputStream fis = new FileInputStream(GameConstants.CONFIG_BASE_URL + "/army2.properties");
             InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8)
        ) {
            configMap.load(isr);
            initConfig();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void initConfig() {
        Gson gson = GsonUtil.getInstance();
        try {
            debug = Boolean.parseBoolean(configMap.getProperty("debug", "false"));
            port = Short.parseShort(configMap.getProperty("port", "8122"));

            equipVersion2 = Byte.parseByte(configMap.getProperty("equip_version_2", "1"));
            iconVersion2 = Byte.parseByte(configMap.getProperty("icon_version_2", "1"));
            levelCVersion2 = Byte.parseByte(configMap.getProperty("levelc_version_2", "1"));
            valuesVersion2 = Byte.parseByte(configMap.getProperty("values_version_2", "1"));
            playerVersion2 = Byte.parseByte(configMap.getProperty("player_version_2", "1"));

            maxClients = Integer.parseInt(configMap.getProperty("max_clients", "1000"));

            messageLogin = configMap.getProperty("message_login", "");
            message = gson.fromJson(configMap.getProperty("message", "[]"), String[].class);

            addInfo = configMap.getProperty("add_info", "");
            addInfoUrl = configMap.getProperty("add_info_url", "");
            regTeamUrl = configMap.getProperty("reg_team_url", "");
            downloadTitle = configMap.getProperty("download_title", "");
            downloadInfo = configMap.getProperty("download_info", "");
            downloadUrl = configMap.getProperty("download_url", "");

            tetStartTime = LocalDateTime.parse(configMap.getProperty("tet.start"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            tetEndTime = LocalDateTime.parse(configMap.getProperty("tet.end"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            LocalDateTime now = LocalDateTime.now();
            isTet = !now.isBefore(tetStartTime) && !now.isAfter(tetEndTime);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}