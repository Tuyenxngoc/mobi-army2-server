package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class ItemData {

    @Getter
    @Setter
    public static final class Item {
        String name;
        int buyXu;
        int buyLuong;
        byte carriedItemCount;
    }

    public static final List<Item> items = new ArrayList<>();
}
