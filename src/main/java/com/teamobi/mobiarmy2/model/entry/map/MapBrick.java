package com.teamobi.mobiarmy2.model.entry.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author tuyen
 */
@Getter
@Setter
@AllArgsConstructor
public class MapBrick {
    private int id;
    private int[] data;
    private int width;
    private int height;

    public MapBrick(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapBrick mapBrick = (MapBrick) o;
        return id == mapBrick.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}