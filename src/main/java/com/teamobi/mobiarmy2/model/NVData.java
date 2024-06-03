package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.json.CharacterJson;
import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.entry.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author tuyen
 */
public class NVData {
    public static final List<CharacterEntry> CHARACTER_ENTRIES = new ArrayList<>();
    public static final List<EquipmentEntry> EQUIPMENT_ENTRIES = new ArrayList<>();
    public static short totalSaleEquipments = 0;

    public static void addEquip(EquipmentEntry newEquip) {
        //Tìm nhân vật theo id
        CharacterEntry characterEntry = CHARACTER_ENTRIES.stream()
                .filter(entry -> entry.id == newEquip.getCharacterId())
                .findFirst()
                .orElse(null);
        if (characterEntry == null) {
            return;
        }
        //Lấy danh sách theo loại trang bị, chưa có thì tạo mới
        List<EquipmentEntry> entryList = characterEntry.equips.computeIfAbsent(newEquip.getEquipType(), k -> new ArrayList<>());
        if (entryList.stream().anyMatch(entry -> entry.getEquipIndex() == newEquip.getEquipIndex())) {//Nếu tồn tại trong danh sách rồi thì bỏ qua
            return;
        }

        if (newEquip.isOnSale()) {
            newEquip.setIndexSale(totalSaleEquipments);
            totalSaleEquipments++;
        } else {
            newEquip.setIndexSale(-1);
        }

        entryList.add(newEquip);
        EQUIPMENT_ENTRIES.add(newEquip);
    }

    public static EquipmentEntry getEquipEntry(byte characterId, byte equipType, short equipIndex) {
        // Find the character entry by ID
        Optional<CharacterEntry> characterEntryOpt = CHARACTER_ENTRIES.stream()
                .filter(entry -> entry.id == characterId)
                .findFirst();

        if (characterEntryOpt.isEmpty()) {
            return null;
        }

        // Get the equipment list for the given type
        List<EquipmentEntry> entries = characterEntryOpt.get().getEquips().get(equipType);

        if (entries == null) {
            return null;
        }

        // Find the equipment entry by index
        return entries.stream()
                .filter(equipmentEntry -> equipmentEntry.getEquipIndex() == equipIndex)
                .findFirst()
                .orElse(null);
    }

    public static EquipmentEntry getEquipEntryByIndexSale(int indexSale) {
        return EQUIPMENT_ENTRIES.stream()
                .filter(equipEntry -> equipEntry.isOnSale() && equipEntry.getIndexSale() == indexSale)
                .findFirst()
                .orElse(null);
    }

    public static short[] getEquipData(EquipmentChestJson[] trangBi, CharacterJson character, byte nvUsed) {
        short[] data = new short[5];

        int index = character.getData()[5];
        if (index >= 0 && index < trangBi.length) {
            EquipmentChestJson equipmentChestJson = trangBi[index];
            EquipmentEntry entry = NVData.getEquipEntry(equipmentChestJson.getCharacterId(), equipmentChestJson.getEquipType(), equipmentChestJson.getEquipIndex());
            if (entry != null && entry.getDisguiseEquippedIndexes() != null) {
                data[0] = entry.getDisguiseEquippedIndexes()[0];
                data[1] = entry.getDisguiseEquippedIndexes()[1];
                data[2] = entry.getDisguiseEquippedIndexes()[2];
                data[3] = entry.getDisguiseEquippedIndexes()[3];
                data[4] = entry.getDisguiseEquippedIndexes()[4];
            }
        } else {
            for (byte i = 0; i < 5; i++) {
                index = character.getData()[i];
                if (index >= 0 && index < trangBi.length) {
                    data[i] = trangBi[index].getEquipIndex();
                } else if (User.nvEquipDefault[nvUsed][i] != null) {
                    data[i] = User.nvEquipDefault[nvUsed][i].getEquipIndex();
                } else {
                    data[i] = -1;
                }
            }
        }
        return data;
    }
}
