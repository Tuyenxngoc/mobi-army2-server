package com.teamobi.mobiarmy2.constant;

/**
 * @author tuyen
 */
public class GameConstants {
    private static final int MAX_INT_VALUE = 2_000_000_000;

    public static final int MAX_XU = MAX_INT_VALUE;
    public static final int MIN_XU = -MAX_INT_VALUE;
    public static final int MAX_LUONG = MAX_INT_VALUE;
    public static final int MIN_LUONG = -MAX_INT_VALUE;
    public static final int MAX_CUP = MAX_INT_VALUE;
    public static final int MIN_CUP = -MAX_INT_VALUE;
    public static final int MAX_XP = MAX_INT_VALUE;
    public static final int MIN_XP = -MAX_INT_VALUE;

    public static final byte POINT_ON_LEVEL = 3;

    public static final String RESOURCE_BASE_URL = "src/main/resources";

    public static final String BIG_IMAGE_PATH = RESOURCE_BASE_URL + "/bigImage/bigImage%d.png";
    public static final String BULLET_IMAGE_PATH = RESOURCE_BASE_URL + "/bullet/bullet%d.png";
    public static final String EFFECT_PATH = RESOURCE_BASE_URL + "/effect/hole";
    public static final String CLAN_ICON_PATH = RESOURCE_BASE_URL + "/icon/clan/%d.png";
    public static final String ITEM_ICON_PATH = RESOURCE_BASE_URL + "/icon/item/%d.png";
    public static final String MAP_ICON_PATH = RESOURCE_BASE_URL + "/icon/map/%d.png";
    public static final String MAP_PATH = RESOURCE_BASE_URL + "/map";
    public static final String MAP_LOGO_PATH = RESOURCE_BASE_URL + "/map/icon";
    public static final String PLAYER_PATH = RESOURCE_BASE_URL + "/player";
}
