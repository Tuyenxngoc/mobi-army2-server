package com.teamobi.mobiarmy2.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Formula {
    private byte level;
    private byte levelRequired;
    private byte equipType;
    private byte characterId;
    private byte[] addPointsMax;
    private byte[] addPointsMin;
    private byte[] addPercentsMax;
    private byte[] addPercentsMin;
    private SpecialItem material;
    private Equipment requiredEquip;
    private Equipment resultEquip;
    private String[] details;
    private List<SpecialItemChest> requiredItems = new ArrayList<>();
}
