package com.teamobi.mobiarmy2.model.entry.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
@AllArgsConstructor
public class MapBrick {
    private int id;
    private int[] data;
    private int Width;
    private int Height;
}