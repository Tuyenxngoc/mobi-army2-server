package com.teamobi.mobiarmy2.model.entry.user;

import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentChestEntry {
    public int index;
    public Date purchaseDate;
    public byte vipLevel;
    public byte emptySlot;
    public byte[] additionalPoints;
    public byte[] additionalPercent;
    public byte[] slots = {-1, -1, -1};
    public boolean inUse;
    public EquipmentEntry equipmentEntry;
}