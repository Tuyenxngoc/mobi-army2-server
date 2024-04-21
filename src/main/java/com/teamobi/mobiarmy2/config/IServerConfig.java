package com.teamobi.mobiarmy2.config;

public interface IServerConfig {

    boolean isDebug();

    byte getN_area();

    String getHost();

    short getPort();

    String getMysql_host();

    String getMysql_user();

    String getMysql_pass();

    String getMysql_database();

    byte getEquipVersion2();

    byte getIconversion2();

    byte getLevelCVersion2();

    byte getValuesversion2();

    byte getPlayerVersion2();

    byte[] getnRoom();

    byte getnRoomAll();

    byte getMaxElementFight();

    byte getMaxPlayers();

    byte getNumbPlayers();

    boolean isMgtBullNew();

    byte getnPlayersInitRoom();

    byte getLtapMap();

    short[] getXltap();

    short[] getYltap();

    byte getInitMap();

    byte getInitMapBoss();

    String getAddInfo();

    String getAddInfoURL();

    String getRegTeamURL();

    String getTaiGameName();

    String getTaiGameInfo();

    String getTaiGameURL();

    int getMax_clients();

    int getMax_ruong_tb();

    int getMax_ruong_item();

    int getMax_ruong_itemslot();

    int getMax_item();

    int getMax_friends();

    int getNumClients();

    boolean isStart();

    int getId();

    String[] getRoomTypes();

    String[] getRoomTypesEng();

    int[] getRoomTypeStartNum();

    void setRoomTypeStartNum(int[] roomTypeStartNum);

    String[] getNameRooms();

    int[] getNameRoomNumbers();

    int[] getNameRoomTypes();

    int getStartRoomBoss();

    int getStartMapBoss();

    int getNumMapBoss();

    byte[] getMapIdBoss();

    int getMIN_XU_BOSS();

    int getMAX_XU_BOSS();

    int getMIN_XU_SO_CAP();

    int getMAX_XU_SO_CAP();

    int getMIN_XU_TRUNG_CAP();

    int getMAX_XU_TRUNG_CAP();

    int getMIN_XU_CAO_CAP();

    int getMAX_XU_CAO_CAP();

    int getMIN_XU_DAU_TRUONG();

    int getMAX_XU_DAU_TRUONG();

    int getMIN_XU_TU_DO();

    int getMAX_XU_TU_DO();

    int getMIN_XU_CLAN();

    int getMAX_XU_CLAN();

    int getMIN_TIME_X6_1();

    int getMAX_TIME_X6_1();

    int getMIN_TIME_X6_2();

    int getMAX_TIME_X6_2();

    int getMIN_TIME_X6_3();

    int getMAX_TIME_X6_3();

    int getXU_NHAN_DUOC();

    int getLUONG_NHAN_DUOC();

    int getXU_BI_TRU();

    int getLUONG_BI_TRU();

    String getSEND_CHAT_LOGIN();

    String getSEND_THU1();

    String getSEND_THU2();

    String getSEND_THU3();

    String getSEND_THU4();

    String getSEND_THU5();

    int getKINH_NGHIEM_UP();

}
