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
}
