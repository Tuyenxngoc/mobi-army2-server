package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.json.CharacterData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class NVData {

    public static class EquipmentData {
        public byte id;
        public List<EquipmentEntry> entrys = new ArrayList<>();
    }

    public static class EquipmentEntry {
        public int indexEquip;
        public int indexSale;
        public byte idNV;
        public byte idEquipDat;
        public short id;
        public String name;
        public int giaXu;
        public int giaLuong;
        public int hanSD;
        public byte bullId;
        public short frame;
        public byte lvRequire;
        public short[] bigImageCutX;
        public short[] bigImageCutY;
        public byte[] bigImageSizeX;
        public byte[] bigImageSizeY;
        public byte[] bigImageAlignX;
        public byte[] bigImageAlignY;
        public byte[] invAdd;
        public byte[] percenAdd;
        public boolean onSale;
        public boolean isSet;
        public short[] arraySet;
        public byte cap;
    }

    public static final class NVEntry {
        public byte id;
        public String name;
        public int buyXu;
        public int buyLuong;
        public byte ma_sat_gio;
        public byte goc_min;
        public byte so_dan;
        public short sat_thuong;
        public byte sat_thuong_dan;
        public ArrayList<EquipmentData> trangbis = new ArrayList<>();
    }

    public static ArrayList<NVEntry> entrys = new ArrayList<>();
    public static ArrayList<EquipmentEntry> equips = new ArrayList<>();
    public static int nSaleEquip = 0;

    public static void addEquipEntryById(int nvId, int equipDatId, int equipId, EquipmentEntry eqEntry) {
        System.out.println("Set equip nv=" + nvId + " equipType=" + equipDatId + " id=" + equipId);
        NVEntry nvEntry = null;
        for (NVEntry nvEntry1 : entrys) {
            if (nvEntry1.id == nvId) {
                nvEntry = nvEntry1;
                break;
            }
        }
        if (nvEntry == null) {
            return;
        }
        EquipmentData equipDataEntry = null;
        for (EquipmentData equipDataEntry2 : nvEntry.trangbis) {
            if (equipDataEntry2.id == equipDatId) {
                equipDataEntry = equipDataEntry2;
                break;
            }
        }
        // Create equipData if not exists
        if (equipDataEntry == null) {
            equipDataEntry = new EquipmentData();
            equipDataEntry.id = (byte) equipDatId;
            equipDataEntry.entrys = new ArrayList<>();
            nvEntry.trangbis.add(equipDataEntry);
        }
        for (EquipmentEntry equipEntry : equipDataEntry.entrys) {
            // Neu ton tai => thoat
            if (equipEntry.id == equipId) {
                return;
            }
        }
        // Neu ko ton tai => Tao moi        
        if (eqEntry.onSale) {
            eqEntry.indexSale = nSaleEquip;
            nSaleEquip++;
        }
        eqEntry.indexEquip = equips.size();
        equipDataEntry.entrys.add(eqEntry);
        equips.add(eqEntry);
    }

    public static EquipmentEntry getEquipEntryById(int nvId, int equipDatId, int equipId) {
        for (EquipmentEntry equipEntry : equips) {
            if (equipEntry.idNV == nvId && equipEntry.idEquipDat == equipDatId && equipEntry.id == equipId) {
                return equipEntry;
            }
        }
        return null;
    }

    public static EquipmentEntry getEquipEntryByIndexSale(int indexSale) {
        for (EquipmentEntry equipEntry : equips) {
            if (equipEntry.onSale && equipEntry.indexSale == indexSale) {
                return equipEntry;
            }
        }
        return null;
    }

    public static short[] getEquipData(com.teamobi.mobiarmy2.json.EquipmentData[] trangBi, CharacterData character, byte nvUsed) {
        int index = character.getData().get(5);
        short[] data = new short[5];
        if (index >= 0 && index < trangBi.length) {
            com.teamobi.mobiarmy2.json.EquipmentData equipmentData = trangBi[index];
            NVData.EquipmentEntry entry = NVData.getEquipEntryById(equipmentData.getNvId(), equipmentData.getEquipType(), equipmentData.getId());
            if (entry != null && entry.arraySet != null) {
                data[0] = entry.arraySet[0];
                data[1] = entry.arraySet[1];
                data[2] = entry.arraySet[2];
                data[3] = entry.arraySet[3];
                data[4] = entry.arraySet[4];
            }
        } else {
            for (byte i = 0; i < 5; i++) {
                index = character.getData().get(i);
                if (index >= 0 && index < trangBi.length) {
                    data[i] = (short) trangBi[index].getId();
                } else if (User.nvEquipDefault[nvUsed - 1][i] != null) {
                    data[i] = User.nvEquipDefault[nvUsed - 1][i].id;
                } else {
                    data[i] = -1;
                }
            }
        }
        return data;
    }
}
