package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;
import java.util.List;

public class ItemData {

    public static final class ItemEntry {

        public String name;
        public int buyXu;
        public int buyLuong;
    }

    public static List<ItemEntry> entrys = new ArrayList<>();
    public static byte[] nItemDcMang = {2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
}
