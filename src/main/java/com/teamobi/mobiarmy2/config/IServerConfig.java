package com.teamobi.mobiarmy2.config;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
public interface IServerConfig {

    byte[][] getBossRoomMapLimit();

    byte getRoomIconType();

    byte getTrainingMapId();

    boolean isDebug();

    short getPort();

    byte getEquipVersion2();

    byte getIconVersion2();

    byte getLevelCVersion2();

    byte getValuesVersion2();

    byte getPlayerVersion2();

    String[] getRoomNameVi();

    String[] getRoomNameEn();

    String[] getBossRoomName();

    byte[] getRoomQuantity();

    int[] getRoomMaxXu();

    int[] getRoomMinXu();

    byte[] getRoomMaxMap();

    byte[] getRoomMinMap();

    byte[] getBossRoomBossId();

    byte getNumArea();

    byte getMaxPlayerFight();

    byte getMaxElementFight();

    byte getNumPlayer();

    byte getNumPlayerInitRoom();

    byte getStartMapBoss();

    String getAddInfo();

    String getAddInfoUrl();

    String getRegTeamUrl();

    String getDownloadTitle();

    String getDownloadInfo();

    String getDownloadUrl();

    int getMaxClients();

    byte getMaxEquipmentSlots();

    byte getMaxSpecialItemSlots();

    byte getMaxItem();

    byte getMaxFriends();

    String getMessageLogin();

    String[] getMessage();

    byte[] getBossRoomMapId();

    int getPriceChatServer();

    int getMinXuContributeClan();

    int[] getTopBonus();

    int getSpinXuCost();

    int getSpinLuongCost();

    int[][] getSpinItemCounts();

    int[][] getSpinXuCounts();

    int[][] getSpinXpCounts();

    int[] getSpinTypeProbabilities();

    LocalDateTime getTetStartTime();

    LocalDateTime getTetEndTime();
}
