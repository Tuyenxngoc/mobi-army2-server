package com.teamobi.mobiarmy2.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomConfig {
    public final String nameVi;
    public final String nameEn;
    public final int quantity;
    public final int minXu;
    public final int maxXu;
    public final byte minMap;
    public final byte maxMap;
}