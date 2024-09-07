package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FabricateItemEntry {
    private int id;

    private int xuRequire;
    private int luongRequire;
    private Set<SpecialItemChestEntry> itemRequire = new HashSet<>();

    private int rewardXu;
    private int rewardLuong;
    private int rewardCup;
    private int rewardExp;
    private List<SpecialItemChestEntry> rewardItem = new ArrayList<>();

    private String confirmationMessage;
    private String completionMessage;
}
