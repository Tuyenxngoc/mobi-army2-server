package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class ItemData {

    public static final class Item {
        public String name;
        public int buyXu;
        public int buyLuong;
    }

    public static List<Item> items = new ArrayList<>();
    public static byte[] carriedItemCount = {2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, -1, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
}
