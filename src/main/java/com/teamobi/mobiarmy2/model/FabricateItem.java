package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@AllArgsConstructor
public class FabricateItem {
    private int id;
    private int xuRequire;
    private int luongRequire;
    private Set<SpecialItemChest> itemRequire = new HashSet<>();
    private int rewardXu;
    private int rewardLuong;
    private int rewardCup;
    private int rewardExp;
    private List<SpecialItemChest> rewardItem = new ArrayList<>();
    private String confirmationMessage;
    private String completionMessage;
}
