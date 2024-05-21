package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.util.Until;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class ItemFightData {

    @Getter
    @Setter
    public static final class ItemFight {
        String name;
        int buyXu;
        int buyLuong;
        byte carriedItemCount;
    }

    public static final List<ItemFight> ITEM_FIGHTS = new ArrayList<>();

    public static byte randomItem() {
        return (byte) (Until.nextInt(ITEM_FIGHTS.size() - 2) + 2);
    }
}
