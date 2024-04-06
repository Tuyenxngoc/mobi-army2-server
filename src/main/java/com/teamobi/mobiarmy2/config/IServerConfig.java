package com.teamobi.mobiarmy2.config;

public interface IServerConfig {

    int getPort();

    byte getMaxElementFight();

    boolean isDebug();

    byte getNumMapBoss();

    byte getStartMapBoss();

    byte getNumbPlayers();

    String[] getRoomTypes();

    String[] getRoomTypesEng();

    String[] getNameRooms();

    int[] getNameRoomNumbers();

    int[] getNameRoomTypes();

    byte[] getNRoom();

    int[] getRoomTypeStartNum();

    byte[] getMapIdBoss();
}
