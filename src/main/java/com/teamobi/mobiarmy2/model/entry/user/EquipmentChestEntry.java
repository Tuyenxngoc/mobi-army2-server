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
    private int index;
    private LocalDateTime purchaseDate;
    private byte vipLevel;
    private byte emptySlot;
    private byte[] additionalPoints;
    private byte[] additionalPercent;
    private byte[] slots;
    private boolean inUse;
    private EquipmentEntry equipmentEntry;
}