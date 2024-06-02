package com.teamobi.mobiarmy2.model.entry.user;

import com.teamobi.mobiarmy2.model.entry.equip.EquipmentEntry;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author tuyen
 */
@Getter
@Setter
public class EquipmentChestEntry {
    public int index;
    public LocalDateTime purchaseDate;
    public byte vipLevel;
    public byte emptySlot;
    public byte[] additionalPoints;
    public byte[] additionalPercent;
    public byte[] slots;
    public boolean inUse;
    public EquipmentEntry equipmentEntry;
}