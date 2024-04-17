package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ruongDoTBEntry {
    int index;
    LocalDateTime dayBuy;
    byte slotNull;
    byte vipLevel;
    short[] invAdd;
    short[] percenAdd;
    short[] anAdd;
    int[] slot;
    boolean isUse;
    NVData.EquipmentEntry entry;
    byte cap;
}