package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.Equipment;
import com.teamobi.mobiarmy2.model.EquipmentChestJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class EquipmentManager {
    public static Equipment[][] equipDefault;
    public static final Map<Short, Equipment> EQUIPMENTS = new HashMap<>();

    private static final List<Short> SALE_INDEX_TO_ID = new ArrayList<>();
    private static final Map<Byte, Map<Byte, List<Short>>> EQUIPMENTS_BY_CHARACTER_AND_TYPE = new HashMap<>();

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
        Short equipmentId = SALE_INDEX_TO_ID.get(saleIndex);
        return equipmentId != null ? EQUIPMENTS.get(equipmentId) : null;
    }

    public static Equipment getRandomEquip(Predicate<Equipment> filter) {
        return null;
    }

    public static short[] getEquipmentData(EquipmentChestJson[] equipmentChestJsons, int[] data, byte activeCharacter) {
        return new short[5];
    }

    public static void clear() {
        EQUIPMENTS.clear();
        SALE_INDEX_TO_ID.clear();
        EQUIPMENTS_BY_CHARACTER_AND_TYPE.clear();
        equipDefault = null;
    }

    public static int getTotalSaleEquipments() {
        return SALE_INDEX_TO_ID.size();
    }

    public static Map<Byte, List<Short>> getEquipmentByCharacterId(byte characterId) {
        return EQUIPMENTS_BY_CHARACTER_AND_TYPE.get(characterId);
    }
}
