package com.teamobi.mobiarmy2.model.entry;

import com.teamobi.mobiarmy2.json.SpecialItemChestJson;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FabricateItemEntry {
    private int id;

    private int xuRequire;
    private int luongRequire;
    private SpecialItemChestJson[] itemRequire;

    private int rewardXu;
    private int rewardLuong;
    private int rewardCup;
    private int rewardExp;
    private SpecialItemChestJson[] rewardItem;

    private String confirmationMessage;
    private String completionMessage;
}
