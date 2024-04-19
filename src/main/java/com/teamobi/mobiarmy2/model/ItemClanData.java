package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class ItemClanData {

    public static final class ItemClanEntry {

        public int id;
        public int level;
        public String name;
        public short time;
        public byte onsole;
        public int xu = 0;
        public int luong = 0;
    }

    public static ArrayList<ItemClanEntry> entrys = new ArrayList<>();

    public static final ItemClanEntry getItemClanId(int id) {
        ItemClanEntry idEntry = null;
        for (int i = 0; i < entrys.size(); i++) {
            ItemClanEntry idEntry2 = entrys.get(i);
            if (idEntry2.id == id) {
                idEntry = idEntry2;
                break;
            }
        }
        return idEntry;
    }

}
