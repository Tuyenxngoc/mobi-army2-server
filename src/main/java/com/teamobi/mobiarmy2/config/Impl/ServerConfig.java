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

    private final Properties properties;

    private int port;
    private byte iconversion2;
    private byte valuesversion2;
    private byte equipVersion2;
    private byte levelCVersion2;
    private byte playerVersion2;
    private boolean debug;
    private int nRoom0;
    private int nRoom1;
    private int nRoom2;
    private int nRoom3;
    private int nRoom4;
    private int nRoom5;
    private int nRoom6;
    private int nArea;
    private int maxPlayer;
    private int maxFight;
    private int numbPlayer;
    private int nPlayersInitRoom;
    private int luyenTapMap;
    private int xLtap;
    private int yLtap;
    private int xLtap1;
    private int yLtap1;
    private int startMapBoss;
    private int numMapBoss;
    private int initMap;
    private int initMapBoss;
    private String addInfo;
    private String addInfoUrl;
    private String regTeamUrl;
    private String taiGameName;
    private String taiGameInfo;
    private String taiGameUrl;
    private int maxClients;
    private int maxRuongTrangBi;
    private int maxRuongItem;
    private int maxItem;
    private boolean mgtBullNew;
    private int maxFriends;
    private int kinhNghiemUp;
    private String sendChatLogin;
    private String sendThu1;
    private String sendThu2;
    private String sendThu3;
    private String sendThu4;
    private String sendThu5;
    private int minXuSoCap;
    private int minXuTrungCap;
    private int minXuCaoCap;
    private int minXuDauTruong;
    private int minXuTuDo;
    private int minXuBoss;
    private int minXuClan;
    private int maxXuSoCap;
    private int maxXuTrungCap;
    private int maxXuCaoCap;
    private int maxXuDauTruong;
    private int maxXuTuDo;
    private int maxXuBoss;
    private int maxXuClan;
    private int minTimeX61;
    private int minTimeX62;
    private int minTimeX63;
    private int maxTimeX61;
    private int maxTimeX62;
    private int maxTimeX63;
    private int kinhNghiemUp2;
    private int xuNhanDuoc;
    private int xuBiTru;
    private int luongNhanDuoc;
    private int luongBiTru;
    private String[] roomTypes;
    private String[] roomTypesEng;

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
        port = Integer.parseInt(properties.getProperty("port"));
        iconversion2 = Byte.parseByte(properties.getProperty("iconversion2"));
        valuesversion2 = Byte.parseByte(properties.getProperty("valuesversion2"));
        equipVersion2 = Byte.parseByte(properties.getProperty("equipVersion2"));
        levelCVersion2 = Byte.parseByte(properties.getProperty("levelCVersion2"));
        playerVersion2 = Byte.parseByte(properties.getProperty("playerVersion2"));
        debug = Boolean.parseBoolean(properties.getProperty("debug"));
        nRoom0 = Integer.parseInt(properties.getProperty("n-room-0"));
        nRoom1 = Integer.parseInt(properties.getProperty("n-room-1"));
        nRoom2 = Integer.parseInt(properties.getProperty("n-room-2"));
        nRoom3 = Integer.parseInt(properties.getProperty("n-room-3"));
        nRoom4 = Integer.parseInt(properties.getProperty("n-room-4"));
        nRoom5 = Integer.parseInt(properties.getProperty("n-room-5"));
        nRoom6 = Integer.parseInt(properties.getProperty("n-room-6"));
        nArea = Integer.parseInt(properties.getProperty("n-area"));
        maxPlayer = Integer.parseInt(properties.getProperty("max-player"));
        maxFight = Integer.parseInt(properties.getProperty("max-fight"));
        numbPlayer = Integer.parseInt(properties.getProperty("numb-player"));
        nPlayersInitRoom = Integer.parseInt(properties.getProperty("n-players-init-room"));
        luyenTapMap = Integer.parseInt(properties.getProperty("luyen-tap-map"));
        xLtap = Integer.parseInt(properties.getProperty("x-ltap"));
        yLtap = Integer.parseInt(properties.getProperty("y-ltap"));
        xLtap1 = Integer.parseInt(properties.getProperty("x-ltap1"));
        yLtap1 = Integer.parseInt(properties.getProperty("y-ltap1"));
        startMapBoss = Integer.parseInt(properties.getProperty("start-map-boss"));
        numMapBoss = Integer.parseInt(properties.getProperty("num-map-boss"));
        initMap = Integer.parseInt(properties.getProperty("init-map"));
        initMapBoss = Integer.parseInt(properties.getProperty("init-map-boss"));
        addInfo = properties.getProperty("add-info");
        addInfoUrl = properties.getProperty("add-info-url");
        regTeamUrl = properties.getProperty("reg-team-url");
        taiGameName = properties.getProperty("tai-game-name");
        taiGameInfo = properties.getProperty("tai-game-info");
        taiGameUrl = properties.getProperty("tai-game-url");
        maxClients = Integer.parseInt(properties.getProperty("max-clients"));
        maxRuongTrangBi = Integer.parseInt(properties.getProperty("max-ruong-trang-bi"));
        maxRuongItem = Integer.parseInt(properties.getProperty("max-ruong-item"));
        maxItem = Integer.parseInt(properties.getProperty("max-item"));
        mgtBullNew = Boolean.parseBoolean(properties.getProperty("mgt-bull-new"));
        maxFriends = Integer.parseInt(properties.getProperty("max-friends"));
        kinhNghiemUp = Integer.parseInt(properties.getProperty("kinh-nghiem-up"));
        sendChatLogin = properties.getProperty("send-chat-login");
        sendThu1 = properties.getProperty("send-thu1");
        sendThu2 = properties.getProperty("send-thu2");
        sendThu3 = properties.getProperty("send-thu3");
        sendThu4 = properties.getProperty("send-thu4");
        sendThu5 = properties.getProperty("send-thu5");
        minXuSoCap = Integer.parseInt(properties.getProperty("MIN_XU_SO_CAP"));
        minXuTrungCap = Integer.parseInt(properties.getProperty("MIN_XU_TRUNG_CAP"));
        minXuCaoCap = Integer.parseInt(properties.getProperty("MIN_XU_CAO_CAP"));
        minXuDauTruong = Integer.parseInt(properties.getProperty("MIN_XU_DAU_TRUONG"));
        minXuTuDo = Integer.parseInt(properties.getProperty("MIN_XU_TU_DO"));
        minXuBoss = Integer.parseInt(properties.getProperty("MIN_XU_BOSS"));
        minXuClan = Integer.parseInt(properties.getProperty("MIN_XU_CLAN"));
        maxXuSoCap = Integer.parseInt(properties.getProperty("MAX_XU_SO_CAP"));
        maxXuTrungCap = Integer.parseInt(properties.getProperty("MAX_XU_TRUNG_CAP"));
        maxXuCaoCap = Integer.parseInt(properties.getProperty("MAX_XU_CAO_CAP"));
        maxXuDauTruong = Integer.parseInt(properties.getProperty("MAX_XU_DAU_TRUONG"));
        maxXuTuDo = Integer.parseInt(properties.getProperty("MAX_XU_TU_DO"));
        maxXuBoss = Integer.parseInt(properties.getProperty("MAX_XU_BOSS"));
        maxXuClan = Integer.parseInt(properties.getProperty("MAX_XU_CLAN"));
        minTimeX61 = Integer.parseInt(properties.getProperty("MIN_TIME_X6_1"));
        minTimeX62 = Integer.parseInt(properties.getProperty("MIN_TIME_X6_2"));
        minTimeX63 = Integer.parseInt(properties.getProperty("MIN_TIME_X6_3"));
        maxTimeX61 = Integer.parseInt(properties.getProperty("MAX_TIME_X6_1"));
        maxTimeX62 = Integer.parseInt(properties.getProperty("MAX_TIME_X6_2"));
        maxTimeX63 = Integer.parseInt(properties.getProperty("MAX_TIME_X6_3"));
        kinhNghiemUp2 = Integer.parseInt(properties.getProperty("KINH_NGHIEM_UP"));
        xuNhanDuoc = Integer.parseInt(properties.getProperty("XU_NHAN_DUOC"));
        xuBiTru = Integer.parseInt(properties.getProperty("XU_BI_TRU"));
        luongNhanDuoc = Integer.parseInt(properties.getProperty("LUONG_NHAN_DUOC"));
        luongBiTru = Integer.parseInt(properties.getProperty("LUONG_BI_TRU"));

        roomTypes = gson.fromJson(properties.getProperty("room-names-vi"), String[].class);
        roomTypesEng = gson.fromJson(properties.getProperty("room-names-en"), String[].class);
        if (roomTypes.length != roomTypesEng.length) {
            throw new IllegalStateException("Số lượng phòng trong Tiếng Việt và Tiếng Anh không khớp nhau.");
        }
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public byte getIconversion2() {
        return iconversion2;
    }

    @Override
    public byte getValuesversion2() {
        return valuesversion2;
    }

    @Override
    public byte getEquipVersion2() {
        return equipVersion2;
    }

    @Override
    public byte getLevelCVersion2() {
        return levelCVersion2;
    }

    @Override
    public byte getPlayerVersion2() {
        return playerVersion2;
    }

    @Override
    public boolean isDebug() {
        return debug;
    }

    @Override
    public int getnRoom0() {
        return nRoom0;
    }

    @Override
    public int getnRoom1() {
        return nRoom1;
    }

    @Override
    public int getnRoom2() {
        return nRoom2;
    }

    @Override
    public int getnRoom3() {
        return nRoom3;
    }

    @Override
    public int getnRoom4() {
        return nRoom4;
    }

    @Override
    public int getnRoom5() {
        return nRoom5;
    }

    @Override
    public int getnRoom6() {
        return nRoom6;
    }

    @Override
    public int getnArea() {
        return nArea;
    }

    @Override
    public int getMaxPlayer() {
        return maxPlayer;
    }

    @Override
    public int getMaxFight() {
        return maxFight;
    }

    @Override
    public int getNumbPlayer() {
        return numbPlayer;
    }

    @Override
    public int getnPlayersInitRoom() {
        return nPlayersInitRoom;
    }

    @Override
    public int getLuyenTapMap() {
        return luyenTapMap;
    }

    @Override
    public int getxLtap() {
        return xLtap;
    }

    @Override
    public int getyLtap() {
        return yLtap;
    }

    @Override
    public int getxLtap1() {
        return xLtap1;
    }

    @Override
    public int getyLtap1() {
        return yLtap1;
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
    public int getInitMap() {
        return initMap;
    }

    @Override
    public int getInitMapBoss() {
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
    public String getTaiGameName() {
        return taiGameName;
    }

    @Override
    public String getTaiGameInfo() {
        return taiGameInfo;
    }

    @Override
    public String getTaiGameUrl() {
        return taiGameUrl;
    }

    @Override
    public int getMaxClients() {
        return maxClients;
    }

    @Override
    public int getMaxRuongTrangBi() {
        return maxRuongTrangBi;
    }

    @Override
    public int getMaxRuongItem() {
        return maxRuongItem;
    }

    @Override
    public int getMaxItem() {
        return maxItem;
    }

    @Override
    public boolean isMgtBullNew() {
        return mgtBullNew;
    }

    @Override
    public int getMaxFriends() {
        return maxFriends;
    }

    @Override
    public int getKinhNghiemUp() {
        return kinhNghiemUp;
    }

    @Override
    public String getSendChatLogin() {
        return sendChatLogin;
    }

    @Override
    public String getSendThu1() {
        return sendThu1;
    }

    @Override
    public String getSendThu2() {
        return sendThu2;
    }

    @Override
    public String getSendThu3() {
        return sendThu3;
    }

    @Override
    public String getSendThu4() {
        return sendThu4;
    }

    @Override
    public String getSendThu5() {
        return sendThu5;
    }

    @Override
    public int getMinXuSoCap() {
        return minXuSoCap;
    }

    @Override
    public int getMinXuTrungCap() {
        return minXuTrungCap;
    }

    @Override
    public int getMinXuCaoCap() {
        return minXuCaoCap;
    }

    @Override
    public int getMinXuDauTruong() {
        return minXuDauTruong;
    }

    @Override
    public int getMinXuTuDo() {
        return minXuTuDo;
    }

    @Override
    public int getMinXuBoss() {
        return minXuBoss;
    }

    @Override
    public int getMinXuClan() {
        return minXuClan;
    }

    @Override
    public int getMaxXuSoCap() {
        return maxXuSoCap;
    }

    @Override
    public int getMaxXuTrungCap() {
        return maxXuTrungCap;
    }

    @Override
    public int getMaxXuCaoCap() {
        return maxXuCaoCap;
    }

    @Override
    public int getMaxXuDauTruong() {
        return maxXuDauTruong;
    }

    @Override
    public int getMaxXuTuDo() {
        return maxXuTuDo;
    }

    @Override
    public int getMaxXuBoss() {
        return maxXuBoss;
    }

    @Override
    public int getMaxXuClan() {
        return maxXuClan;
    }

    @Override
    public int getMinTimeX61() {
        return minTimeX61;
    }

    @Override
    public int getMinTimeX62() {
        return minTimeX62;
    }

    @Override
    public int getMinTimeX63() {
        return minTimeX63;
    }

    @Override
    public int getMaxTimeX61() {
        return maxTimeX61;
    }

    @Override
    public int getMaxTimeX62() {
        return maxTimeX62;
    }

    @Override
    public int getMaxTimeX63() {
        return maxTimeX63;
    }

    @Override
    public int getKinhNghiemUp2() {
        return kinhNghiemUp2;
    }

    @Override
    public int getXuNhanDuoc() {
        return xuNhanDuoc;
    }

    @Override
    public int getXuBiTru() {
        return xuBiTru;
    }

    @Override
    public int getLuongNhanDuoc() {
        return luongNhanDuoc;
    }

    @Override
    public int getLuongBiTru() {
        return luongBiTru;
    }

    @Override
    public String[] getRoomTypes() {
        return roomTypes;
    }

    @Override
    public String[] getRoomTypesEng() {
        return roomTypesEng;
    }
}
