package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.json.EquipmentChestJson;
import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.entry.user.EquipmentChestEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tuyen
 */
public class JsonConverter {

    public static String convertRuongDoItemToJson(List<SpecialItemChestEntry> ruongDoItems) {
        // Convert the list of SpecialItemChestEntry to SpecialItemChestJson
        List<SpecialItemChestJson> specialItemChestJsons = ruongDoItems.stream().map(entry -> {
            SpecialItemChestJson jsonItem = new SpecialItemChestJson();
            jsonItem.setId(entry.getItem().getId());
            jsonItem.setQuantity(entry.getQuantity());
            return jsonItem;
        }).collect(Collectors.toList());

        // Convert the list of SpecialItemChestJson to JSON string
        return GsonUtil.GSON.toJson(specialItemChestJsons);
    }

    public static String convertRuongDoTBToJson(List<EquipmentChestEntry> ruongdoTB) {
        // Convert the list of EquipmentChestEntry to EquipmentChestJson
        List<EquipmentChestJson> equipmentChestJsons = ruongdoTB.stream().map(entry -> {
            EquipmentChestJson jsonItem = new EquipmentChestJson();
            jsonItem.setCharacterId(entry.getEquipEntry().getCharacterId());
            jsonItem.setEquipIndex(entry.getEquipEntry().getEquipIndex());
            jsonItem.setEquipType(entry.getEquipEntry().getEquipType());
            jsonItem.setKey(entry.getKey());
            jsonItem.setInUse((byte) (entry.isInUse() ? 1 : 0));
            jsonItem.setVipLevel(entry.getVipLevel());
            jsonItem.setPurchaseDate(entry.getPurchaseDate());
            jsonItem.setSlots(entry.getSlots());
            jsonItem.setAdditionalPoints(entry.getAddPoints());
            jsonItem.setAdditionalPercent(entry.getAddPercents());

            return jsonItem;
        }).collect(Collectors.toList());

        // Convert the list of EquipmentChestJson to JSON string
        return GsonUtil.GSON.toJson(equipmentChestJsons);
    }

}
