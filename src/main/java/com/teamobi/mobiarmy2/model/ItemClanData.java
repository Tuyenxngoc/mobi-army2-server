package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tuyen
 */
public class ItemClanData {

    @Getter
    @Setter
    public static final class ClanItemDetail {
        byte id;
        byte level;
        String name;
        byte time;
        byte onSale;
        int xu;
        int luong;
    }

    public static final Map<Byte, ClanItemDetail> clanItemsMap = new HashMap<>();

    public static ClanItemDetail getItemClanById(byte id) {
        return clanItemsMap.get(id);
    }

}
