package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.Equipment;

import java.util.HashMap;
import java.util.Map;

public class EquipmentManager {
    public static Equipment[][] equipDefault;

    public static final Map<Short, Equipment> EQUIPMENTS = new HashMap<>();

    public static void addEquip(Equipment equipment) {
        EQUIPMENTS.put(equipment.getEquipmentId(), equipment);
    }
}
