package com.teamobi.mobiarmy2.model.user;

import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
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
    public byte[] invAdd;
    public byte[] percentAdd;
    public int[] slots = {-1, -1, -1};
    public boolean isUse;
    public EquipmentEntry equipmentEntry;
}