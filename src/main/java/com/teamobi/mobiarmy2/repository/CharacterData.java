package com.teamobi.mobiarmy2.repository;

import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.model.equip.CharacterEntry;
import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author tuyen
 */
public class CharacterData {
    public static final List<CharacterEntry> CHARACTER_ENTRIES = new ArrayList<>();
    public static final List<EquipmentEntry> EQUIPMENT_ENTRIES = new ArrayList<>();
    public static short totalSaleEquipments = 0;

    public static void addEquip(EquipmentEntry newEquip) {
        //Tìm nhân vật theo id
        CharacterEntry characterEntry = CHARACTER_ENTRIES.stream()
                .filter(entry -> entry.getId() == newEquip.getCharacterId())
                .findFirst()
                .orElse(null);
        if (characterEntry == null) {
            return;
        }
        //Lấy danh sách theo loại trang bị, chưa có thì tạo mới
        List<EquipmentEntry> entryList = characterEntry.getEquips().computeIfAbsent(newEquip.getEquipType(), k -> new ArrayList<>());
        if (entryList.stream().anyMatch(entry -> entry.getEquipIndex() == newEquip.getEquipIndex())) {//Nếu tồn tại trong danh sách rồi thì bỏ qua
            return;
        }

        if (newEquip.isOnSale()) {
            newEquip.setSaleIndex(totalSaleEquipments);
            totalSaleEquipments++;
        } else {
            newEquip.setSaleIndex(-1);
        }

        entryList.add(newEquip);
        EQUIPMENT_ENTRIES.add(newEquip);
    }

    public static EquipmentEntry getEquipEntry(byte characterId, byte equipType, short equipIndex) {
        if (equipIndex < 0) {
            return null;
        }

        // Find the character entry by ID
        Optional<CharacterEntry> characterEntryOpt = CHARACTER_ENTRIES.stream()
                .filter(entry -> entry.getId() == characterId)
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

    public static EquipmentEntry getEquipEntryBySaleIndex(int saleIndex) {
        return EQUIPMENT_ENTRIES.stream()
                .filter(equipEntry -> equipEntry.isOnSale() && equipEntry.getSaleIndex() == saleIndex)
                .findFirst()
                .orElse(null);
    }

    //Todo optimize this method
    public static short[] getEquipData(EquipmentChestJson[] equipmentChestJsons, int[] data, byte activeCharacter) {
        short[] equipData = new short[5];

        //Tìm cải trang
        boolean found = false;
        int disguiseKey = data[5];
        if (disguiseKey >= 0) {
            for (EquipmentChestJson json : equipmentChestJsons) {
                if (json != null && json.getKey() == disguiseKey) {
                    EquipmentEntry equip = getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex());
                    if (equip != null) {

                        //Kiểm tra cải trang còn hạn sử dụng hay không
                        if (equip.getExpirationDays() - ChronoUnit.DAYS.between(json.getPurchaseDate(), LocalDateTime.now()) > 0) {
                            System.arraycopy(equip.getDisguiseEquippedIndexes(), 0, equipData, 0, equipData.length);
                            found = true;
                            break;
                        }
                    }
                }
            }
        }

        //Tìm trang bị
        if (!found) {
            for (int i = 0; i < equipData.length; i++) {

                //Kiểm tra có trang bị trong rương hay không
                boolean exists = false;
                int equipKey = data[i];
                if (equipKey >= 0) {
                    for (EquipmentChestJson json : equipmentChestJsons) {
                        if (json != null && json.getKey() == equipKey) {
                            EquipmentEntry equip = getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex());
                            if (equip != null) {

                                //Kiểm tra trang bị còn hạn sử dụng hay không
                                if (equip.getExpirationDays() - ChronoUnit.DAYS.between(json.getPurchaseDate(), LocalDateTime.now()) > 0) {
                                    equipData[i] = json.getEquipIndex();
                                    exists = true;
                                    break;
                                }
                            }
                        }
                    }
                }

                //Nếu không tìm thấy thì lấy dữ liệu mặc định
                if (!exists) {
                    if (User.equipDefault[activeCharacter][i] != null) {
                        equipData[i] = User.equipDefault[activeCharacter][i].getEquipIndex();
                    } else {
                        equipData[i] = -1;
                    }
                }
            }
        }

        return equipData;
    }
}
