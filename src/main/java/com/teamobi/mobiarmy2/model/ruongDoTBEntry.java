package com.teamobi.mobiarmy2.model;

import com.teamobi.mobiarmy2.model.equip.EquipmentEntry;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author tuyen
 */
@Getter
@Setter
public class ruongDoTBEntry {
    public int index;
    public Date dayBuy;
    public byte vipLevel;
    public byte slotNull;
    public short[] invAdd;
    public short[] percentAdd;
    public short[] anAdd;
    public int[] slot;
    public boolean isUse;
    public EquipmentEntry entry;
    public byte cap;
}