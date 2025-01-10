package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.Equipment;
import com.teamobi.mobiarmy2.model.EquipmentChestJson;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EquipmentManager {
    public static Equipment[][] equipDefault;
    public static short totalSaleEquipments = 0;
    public static final Map<Short, Equipment> EQUIPMENTS = new HashMap<>();

    public static void addEquip(Equipment equipment) {
        if (equipment.isOnSale()) {
            equipment.setSaleIndex(totalSaleEquipments);
            totalSaleEquipments++;
        } else {
            equipment.setSaleIndex(-1);
        }
        EQUIPMENTS.put(equipment.getEquipmentId(), equipment);
    }

    public static Equipment getEquipment(short equipmentId) {
        return EQUIPMENTS.get(equipmentId);
    }

    public static Equipment getRandomEquip(Predicate<Equipment> filter) {
        return null;
    }

    public static Equipment getEquipEntryBySaleIndex(int saleIndex) {
        return null;
    }

    public static short[] getEquipData(EquipmentChestJson[] equipmentChestJsons, int[] data, byte activeCharacter) {
        short[] equipData = new short[5];
        LocalDateTime now = LocalDateTime.now();

        Map<Integer, EquipmentChestJson> equipmentMap = Arrays.stream(equipmentChestJsons)
                .filter(json -> json != null && json.getCharacterId() == activeCharacter)
                .collect(Collectors.toMap(EquipmentChestJson::getKey, json -> json));

        //Tìm cải trang
        int disguiseKey = data[5];
        if (disguiseKey != -1 && equipmentMap.containsKey(disguiseKey)) {
            EquipmentChestJson json = equipmentMap.get(disguiseKey);
            Equipment equip = getEquipment((short) 0);
            if (equip != null && equip.getExpirationDays() - ChronoUnit.DAYS.between(json.getPurchaseDate(), now) > 0) {
                return equip.getDisguiseEquippedIndexes();
            }
        }

        //Tìm trang bị
        for (int i = 0; i < equipData.length; i++) {
            int equipKey = data[i];
            boolean exists = false;

            if (equipKey != -1 && equipmentMap.containsKey(equipKey)) {
                EquipmentChestJson json = equipmentMap.get(equipKey);
                Equipment equip = getEquipment((short) 0);
                if (equip != null && equip.getExpirationDays() - ChronoUnit.DAYS.between(json.getPurchaseDate(), now) > 0) {
                    equipData[i] = json.getEquipIndex();
                    exists = true;
                }
            }

            //Nếu không tìm thấy thì lấy dữ liệu mặc định
            if (!exists) {
                equipData[i] = (EquipmentManager.equipDefault[activeCharacter][i] != null)
                        ? EquipmentManager.equipDefault[activeCharacter][i].getEquipIndex()
                        : -1;
            }
        }

        return equipData;
    }

    public static void clear() {
        EQUIPMENTS.clear();
        equipDefault = null;
    }
}
