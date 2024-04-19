package com.teamobi.mobiarmy2.model;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class SpecialItemData {

    public static final class SpecialItemEntry {

        public int id;
        public int indexSale;
        public String name;
        public String detail;
        public short[] ability;
        public int buyXu;
        public int buyLuong;
        public short hanSD;
        public boolean showChon;
        public boolean onSale;
    }

    public static ArrayList<SpecialItemEntry> entrys = new ArrayList<>();
    public static int nSaleItem;

    public static SpecialItemEntry getSpecialItemById(int id) {
        SpecialItemEntry spiEntry = null;
        for (SpecialItemEntry spiEntry2 : entrys) {
            if (spiEntry2.id == id) {
                spiEntry = spiEntry2;
                break;
            }
        }
        return spiEntry;
    }

    public static final SpecialItemEntry getSpecialItemByIndexSale(int indexSale) {
        SpecialItemEntry spiEntry = null;
        for (int i = 0; i < entrys.size(); i++) {
            SpecialItemEntry spiEntry2 = entrys.get(i);
            if (spiEntry2.onSale && spiEntry2.indexSale == indexSale) {
                spiEntry = spiEntry2;
                break;
            }
        }
        return spiEntry;
    }


    public static String getItemName(byte id) {
        return getSpecialItemById(id).name;
    }
}
