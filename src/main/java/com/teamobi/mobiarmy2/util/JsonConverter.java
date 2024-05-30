package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import com.teamobi.mobiarmy2.model.SpecialItemChestEntry;

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

}
