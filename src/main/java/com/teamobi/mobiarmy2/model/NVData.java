package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.json.CharacterData;
import com.teamobi.mobiarmy2.model.equip.EquipmentData;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.equip.NVEntry;

import java.util.ArrayList;

/**
 * @author tuyen
 */
public class NVData {

    public static ArrayList<NVEntry> entrys = new ArrayList<>();
    public static ArrayList<EquipmentEntry> equips = new ArrayList<>();
    public static int nSaleEquip = 0;

    public static void addEquip(EquipmentEntry equip) {

    }

    public static void addEquipEntryById(int nvId, int equipDatId, int equipId, EquipmentEntry eqEntry) {
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
            if (equipEntry.index == equipId) {
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
            if (equipEntry.characterId == nvId && equipEntry.equipType == equipDatId && equipEntry.index == equipId) {
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
            EquipmentEntry entry = NVData.getEquipEntryById(equipmentData.getNvId(), equipmentData.getEquipType(), equipmentData.getId());
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
                    data[i] = User.nvEquipDefault[nvUsed - 1][i].index;
                } else {
                    data[i] = -1;
                }
            }
        }
        return data;
    }
}
