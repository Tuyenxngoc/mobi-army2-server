package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.Equipment;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EquipmentManager {
    public static Equipment[][] equipDefault;
    public static final Map<Short, Equipment> EQUIPMENTS = new HashMap<>();
    public static final List<Short> SALE_INDEX_TO_ID = new ArrayList<>();
    public static final Map<Byte, Map<Byte, List<Short>>> EQUIPMENTS_BY_CHARACTER_AND_TYPE = new HashMap<>();

    public static void addEquipment(Equipment equipment) {
        // Thêm vào danh sách trang bị đang bán
        if (equipment.isOnSale()) {
            SALE_INDEX_TO_ID.add(equipment.getEquipmentId());
            equipment.setSaleIndex((short) (SALE_INDEX_TO_ID.size() - 1));
        } else {
            equipment.setSaleIndex(-1);
        }

        // Thêm vào danh sách nhóm trang bị theo nhân vật và loại
        Map<Byte, List<Short>> equipmentsByType = EQUIPMENTS_BY_CHARACTER_AND_TYPE.computeIfAbsent(equipment.getCharacterId(), k -> new HashMap<>());
        List<Short> equipmentIds = equipmentsByType.computeIfAbsent(equipment.getEquipType(), k -> new ArrayList<>());
        equipmentIds.add(equipment.getEquipmentId());

        // Thêm vào danh sách
        EQUIPMENTS.put(equipment.getEquipmentId(), equipment);
    }

    public static Equipment getEquipment(short equipmentId) {
        return EQUIPMENTS.get(equipmentId);
    }

    public static Equipment getEquipmentBySaleIndex(short saleIndex) {
        if (saleIndex < 0 || saleIndex >= SALE_INDEX_TO_ID.size()) {
            return null;
        }
        Short equipmentId = SALE_INDEX_TO_ID.get(saleIndex);
        return equipmentId != null ? EQUIPMENTS.get(equipmentId) : null;
    }

    public static Equipment getRandomEquipment(Predicate<Equipment> filter) {
        return null;
    }

    public static Equipment getEquipment(byte characterId, byte equipType, short equipIndex) {
        return null;
    }

    public static Map<Byte, List<Short>> getEquipmentByCharacterId(byte characterId) {
        return EQUIPMENTS_BY_CHARACTER_AND_TYPE.get(characterId);
    }

    public static short[] getEquipmentIndexes(EquipmentChestJson[] equipmentChestJsons, int[] data, byte activeCharacter) {
        short[] equipData = new short[5];
        LocalDateTime now = LocalDateTime.now();

        Map<Integer, EquipmentChestJson> equipmentMap = Arrays.stream(equipmentChestJsons)
                .filter(json -> json != null && json.getCharacterId() == activeCharacter)
                .collect(Collectors.toMap(EquipmentChestJson::getKey, json -> json));

        //Tìm cải trang
        int disguiseKey = data[5];
        if (disguiseKey != -1 && equipmentMap.containsKey(disguiseKey)) {
            EquipmentChestJson json = equipmentMap.get(disguiseKey);
            Equipment equip = getEquipment(json.getCharacterId(), json.getEquipType(), json.getEquipIndex());
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
                Equipment equip = getEquipment(json.getCharacterId(), json.getEquipType(), json.getEquipIndex());
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
        equipDefault = null;
        EQUIPMENTS.clear();
        SALE_INDEX_TO_ID.clear();
        EQUIPMENTS_BY_CHARACTER_AND_TYPE.clear();
    }
}