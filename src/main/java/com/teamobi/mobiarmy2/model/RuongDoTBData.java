package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class RuongDoTBData {
    public int index;
    public Date dayBuy;
    public byte vipLevel;
    public byte slotNull;
    public short[] invAdd;
    public short[] percentAdd;
    public short[] anAdd;
    public int[] slot;
    public boolean isUse;
    public NVData.EquipmentEntry entry;
    public byte cap;
}