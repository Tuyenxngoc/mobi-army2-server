package com.teamobi.mobiarmy2.constant;

/**
 * @author tuyen
 */
public class CommonConstant {
    public static final String ALPHANUMERIC_PATTERN = "^[a-zA-Z0-9]+$";
    public static final String RESOURCES_PATH = "src/main/resources/";

    public static final String cacheFolder = "cache";

    public static final String mapCacheName = cacheFolder + "/valuesdata2";
    public static final String playerCacheName = cacheFolder + "/playerdata2";
    public static final String equipCacheName = cacheFolder + "/equipdata2";
    public static final String levelCacheName = cacheFolder + "/levelCData2";
    public static final String iconCacheName = cacheFolder + "/icondata2";

    public static final int PRICE_CHAT = 10_000;

    public static final int MAX_XU = 2_000_000_000;
    public static final int MIN_XU = -2_000_000_000;
    public static final int MAX_LUONG = MAX_XU;
    public static final int MIN_LUONG = MIN_XU;
    public static final int MAX_DANH_VONG = MAX_XU;
    public static final int MIN_DANH_VONG = MIN_XU;
}
