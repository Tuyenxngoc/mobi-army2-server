package com.teamobi.mobiarmy2.model;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class ArmyMap {
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

    public ArmyMap(byte id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArmyMap entry = (ArmyMap) o;
        return id == entry.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}