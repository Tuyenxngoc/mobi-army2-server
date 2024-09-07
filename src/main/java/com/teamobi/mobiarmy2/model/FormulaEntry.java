package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.item.SpecialItemEntry;
import com.teamobi.mobiarmy2.model.user.SpecialItemChestEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tuyen
 */
@Getter
@Setter
public class FormulaEntry {
    private byte level;
    private byte levelRequired;
    private byte equipType;
    private byte characterId;
    private byte[] addPointsMax;
    private byte[] addPointsMin;
    private byte[] addPercentsMax;
    private byte[] addPercentsMin;
    private SpecialItemEntry material;
    private EquipmentEntry requiredEquip;
    private EquipmentEntry resultEquip;
    private String[] details;
    private List<SpecialItemChestEntry> requiredItems = new ArrayList<>();
}
