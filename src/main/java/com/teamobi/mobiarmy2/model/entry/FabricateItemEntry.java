package com.teamobi.mobiarmy2.model.entry;

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
    private String itemRequire;

    private int rewardXu;
    private int rewardLuong;
    private int rewardCup;
    private int rewardExp;
    private String rewardItem;

    private String confirmationMessage;
    private String completionMessage;
}
