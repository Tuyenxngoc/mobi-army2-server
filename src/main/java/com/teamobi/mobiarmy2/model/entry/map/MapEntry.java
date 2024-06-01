package com.teamobi.mobiarmy2.model.entry.map;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class MapEntry {
    public byte id;
    public String name;
    public String fileName;
    public byte[] data;
    public short bg;
    public short mapAddY;
    public short cl2AddY;
    public short inWaterAddY;
    public short bullEffShower;
    public short[] XPlayer;
    public short[] YPlayer;
}