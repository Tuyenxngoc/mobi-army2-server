package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class ItemClanData {

    @Getter
    @Setter
    public static final class ItemClan {
        public int id;
        public int level;
        public String name;
        public short time;
        public byte onSale;
        public int xu;
        public int luong;
    }

    public static ArrayList<ItemClan> itemClans = new ArrayList<>();

    public static ItemClan getItemClanById(int id) {
        ItemClan itemClan = null;
        for (ItemClan tmp : itemClans) {
            if (tmp.id == id) {
                itemClan = tmp;
                break;
            }
        }
        return itemClan;
    }

}
