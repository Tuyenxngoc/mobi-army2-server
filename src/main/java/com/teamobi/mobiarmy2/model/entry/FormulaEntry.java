package com.teamobi.mobiarmy2.model.entry;

import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import com.teamobi.mobiarmy2.model.entry.user.SpecialItemChestEntry;
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
    private byte materialId;
    private EquipmentEntry requiredEquip;
    private EquipmentEntry resultEquip;
    private String[] details;
    private List<SpecialItemChestEntry> requiredItems = new ArrayList<>();
}
