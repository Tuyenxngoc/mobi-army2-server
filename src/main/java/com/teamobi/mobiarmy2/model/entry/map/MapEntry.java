package com.teamobi.mobiarmy2.model.entry.map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

/**
 * @author tuyen
 */
@Getter
@Setter
@NoArgsConstructor
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

    public MapEntry(byte id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapEntry entry = (MapEntry) o;
        return id == entry.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}