package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.model.Equipment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentManager {
    public static Equipment[][] equipDefault;
    public static final Map<Short, Equipment> EQUIPMENTS = new HashMap<>();

    private static final List<Short> SALE_INDEX_TO_ID = new ArrayList<>();
    private static final Map<Byte, Map<Byte, List<Short>>> EQUIPMENTS_BY_CHARACTER_AND_TYPE = new HashMap<>();
}