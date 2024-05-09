package com.teamobi.mobiarmy2.server;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
public class BangXHManager {

    @Getter
    @Setter
    public static class BangXHEntry {
        int playerId;
        String username;
        byte nvUsed;
        short clanId;
        byte level;
        byte levelPt;
        byte index;
        short[] data;
        String detail;
    }

    private static BangXHManager instance;

    public static BangXHManager getInstance() {
        if (instance == null) {
            instance = new BangXHManager();
        }
        return instance;
    }

    @Getter
    private final boolean isComplete = true;
    @Getter
    private final String[] bangXHString = new String[]{"DANH DỰ", "CAO THỦ", "ĐẠI GIA XU", "ĐẠI GIA LƯỢNG", "DANH DỰ TUẦN", "ĐẠI GIA TUẦN"};
    @Getter
    private final String[] bangXHString1 = new String[]{"Danh dự", "XP", "Xu", "Lượng", "Danh dự", "Xu"};

    public BangXHEntry[] getBangXH(int type, int page) {
        return new BangXHEntry[0];
    }

}
