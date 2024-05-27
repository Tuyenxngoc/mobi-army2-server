package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.json.CharacterData;
import com.teamobi.mobiarmy2.json.EquipmentData;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
public class NVData {

    public static List<CharacterEntry> characterEntries = new ArrayList<>();
    public static List<EquipmentEntry> equipmentEntries = new ArrayList<>();
    public static short totalSaleEquipments = 0;

    public static void addEquip(EquipmentEntry newEquip) {
        //Tìm nhân vật theo id
        CharacterEntry characterEntry = characterEntries.stream()
                .filter(entry -> entry.id == newEquip.characterId)
                .findFirst()
                .orElse(null);
        if (characterEntry == null) {
            return;
        }
        //Lấy danh sách theo loại trang bị, chưa có thì tạo mới
        List<EquipmentEntry> entryList = characterEntry.equips.computeIfAbsent(newEquip.getEquipType(), k -> new ArrayList<>());
        if (entryList.stream().anyMatch(entry -> entry.getIndex() == newEquip.getIndex())) {//Nếu tồn tại trong danh sách rồi thì bỏ qua
            return;
        }

        if (newEquip.isOnSale()) {
            newEquip.setIndexSale(totalSaleEquipments);
            totalSaleEquipments++;
        } else {
            newEquip.setIndexSale(-1);
        }

        entryList.add(newEquip);
        equipmentEntries.add(newEquip);
    }

    public static EquipmentEntry getEquipEntryById(int nvId, int equipDatId, int equipId) {
        for (EquipmentEntry equipEntry : equipmentEntries) {
            if (equipEntry.characterId == nvId && equipEntry.equipType == equipDatId && equipEntry.index == equipId) {
                return equipEntry;
            }
        }
        return null;
    }

    public static EquipmentEntry getEquipEntryByIndexSale(int indexSale) {
        for (EquipmentEntry equipEntry : equipmentEntries) {
            if (equipEntry.onSale && equipEntry.indexSale == indexSale) {
                return equipEntry;
            }
        }
        return null;
    }

    public static short[] getEquipData(EquipmentData[] trangBi, CharacterData character, byte nvUsed) {
        int index = character.getData().get(5);
        short[] data = new short[5];
        if (index >= 0 && index < trangBi.length) {
            EquipmentData equipmentData = trangBi[index];
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
                } else if (User.nvEquipDefault[nvUsed][i] != null) {
                    data[i] = User.nvEquipDefault[nvUsed][i].index;
                } else {
                    data[i] = -1;
                }
            }
        }
        return data;
    }
}
