package com.teamobi.mobiarmy2.config;

public interface IServerConfig {

    int getPort();

    boolean isDebug();

    String[] getRoomTypes();

    String[] getRoomTypesEng();

    byte getEquipVersion2();

    byte getIconversion2();

    byte getLevelCVersion2();

    byte getValuesversion2();

    byte getPlayerVersion2();

    String getGameInfo();

    String getGameInfoUrl();

    String getGameClanUrl();

    int getMaxPlayerFight();

    int getNumMapBoss();

    int getStartMapBoss();

    byte[] getMapIdBoss();

    byte getNumbPlayers();

    String[] getNameRooms();

    byte getnPlayersInitRoom();

    int getMin_xu_so_cap();

    int getMin_xu_trung_cap();

    int getMin_xu_cao_cap();

    int getMin_xu_dau_truong();

    int getMin_xu_tu_do();

    int getMin_xu_boss();

    int getMin_xu_clan();

    int getMax_xu_so_cap();

    int getMax_xu_trung_cap();

    int getMax_xu_cao_cap();

    int getMax_xu_dau_truong();

    int getMax_xu_tu_do();

    int getMax_xu_boss();

    int getMax_xu_clan();

    byte getInitMap();

    byte getMaxPlayers();
}
