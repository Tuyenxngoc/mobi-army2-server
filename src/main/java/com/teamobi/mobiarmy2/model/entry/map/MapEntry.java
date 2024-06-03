package com.teamobi.mobiarmy2.model.entry.map;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class MapEntry {
    private byte id;
    private String name;
    private String fileName;
    private byte[] data;
    private short bg;
    private short mapAddY;
    private short cl2AddY;
    private short inWaterAddY;
    private short bullEffShower;
    private short[] XPlayer;
    private short[] YPlayer;
}