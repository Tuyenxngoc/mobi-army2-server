package com.teamobi.mobiarmy2.config;

public interface IServerConfig {

    boolean isDebug();

    short getPort();

    byte getEquipVersion2();

    byte getIconVersion2();

    byte getLevelCVersion2();

    byte getValuesVersion2();

    byte getPlayerVersion2();

    String[] getRoomNameVi();

    String[] getRoomNameEn();

    String[] getRoomBossName();

    byte[] getRoomQuantity();

    int[] getRoomMaxXu();

    int[] getRoomMinXu();

    byte[] getMapIdBoss();

    byte getNumArea();

    byte getMaxPlayerFight();

    byte getMaxElementFight();

    byte getNumPlayer();

    byte getNumPlayerInitRoom();

    byte getStartMapBoss();

    byte getNumMapBoss();

    byte getInitMapId();

    byte getInitMapBoss();

    String getAddInfo();

    String getAddInfoUrl();

    String getRegTeamUrl();

    String getDownloadTitle();

    String getDownloadInfo();

    String getDownloadUrl();

    int getMaxClients();

    byte getMaxRuongTrangBi();

    byte getMaxRuongItem();

    byte getMaxItem();

    byte getMaxFriends();

    String getMessageLogin();

    String[] getMessage();

}
