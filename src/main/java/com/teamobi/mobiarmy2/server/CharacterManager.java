package com.teamobi.mobiarmy2.server;

import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.model.Character;
import com.teamobi.mobiarmy2.model.Equipment;
import com.teamobi.mobiarmy2.model.User;
import com.teamobi.mobiarmy2.util.Utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class CharacterManager {
    public static final List<Character> CHARACTERS = new ArrayList<>();
    public static final List<Equipment> EQUIPMENTS = new ArrayList<>();
    public static short totalSaleEquipments = 0;

    public static void addEquip(Equipment newEquip) {
        //Tìm nhân vật theo id
        Character character = CHARACTERS.stream()
                .filter(c -> c.getId() == newEquip.getCharacterId())
                .findFirst()
                .orElse(null);
        if (character == null) {
            return;
        }

        //Lấy danh sách theo loại trang bị, chưa có thì tạo mới
        List<Equipment> entryList = character.getEquips().computeIfAbsent(newEquip.getEquipType(), k -> new ArrayList<>());
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
        EQUIPMENTS.add(newEquip);
    }

    public static Equipment getRandomEquip(Predicate<Equipment> filter) {
        // Áp dụng bộ lọc để lấy danh sách trang bị phù hợp
        List<Equipment> filteredEquipments = EQUIPMENTS.stream()
                .filter(filter)
                .toList();

        if (filteredEquipments.isEmpty()) {
            return null;
        }

        // Lấy ngẫu nhiên một trang bị từ danh sách đã lọc
        return filteredEquipments.get(Utils.nextInt(filteredEquipments.size()));
    }

    public static Equipment getEquipEntry(byte characterId, byte equipType, short equipIndex) {
        if (equipIndex < 0) {
            return null;
        }

        //Find the character entry by ID
        Optional<Character> characterEntryOpt = CHARACTERS.stream()
                .filter(entry -> entry.getId() == characterId)
                .findFirst();
        if (characterEntryOpt.isEmpty()) {
            return null;
        }

        //Get the equipment list for the given type
        List<Equipment> entries = characterEntryOpt.get().getEquips().get(equipType);
        if (entries == null) {
            return null;
        }

        //Find the equipment entry by index
        return entries.stream()
                .filter(equipmentEntry -> equipmentEntry.getEquipIndex() == equipIndex)
                .findFirst()
                .orElse(null);
    }

    public static Equipment getEquipEntryBySaleIndex(int saleIndex) {
        return EQUIPMENTS.stream()
                .filter(equipEntry -> equipEntry.isOnSale() && equipEntry.getSaleIndex() == saleIndex)
                .findFirst()
                .orElse(null);
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
            Equipment equip = getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex());
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
                Equipment equip = getEquipEntry(json.getCharacterId(), json.getEquipType(), json.getEquipIndex());
                if (equip != null && equip.getExpirationDays() - ChronoUnit.DAYS.between(json.getPurchaseDate(), now) > 0) {
                    equipData[i] = json.getEquipIndex();
                    exists = true;
                }
            }

            //Nếu không tìm thấy thì lấy dữ liệu mặc định
            if (!exists) {
                equipData[i] = (User.equipDefault[activeCharacter][i] != null)
                        ? User.equipDefault[activeCharacter][i].getEquipIndex()
                        : -1;
            }
        }

        return equipData;
    }
}
