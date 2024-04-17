package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RuongDoTBData {
    int index;
    LocalDateTime dayBuy;
    byte slotNull;
    short[] invAdd;
    short[] percentAdd;
    short[] anAdd;
    int[] slot;
    boolean isUse;
    NVData.EquipmentEntry entry;
    byte cap;
}