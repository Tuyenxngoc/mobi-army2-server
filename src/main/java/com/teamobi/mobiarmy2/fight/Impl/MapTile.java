package com.teamobi.mobiarmy2.fight.Impl;

import lombok.Getter;
import lombok.Setter;

/**
 * @author tuyen
 */
@Getter
@Setter
public class MapTile {

    private int brickId;
    private short x;
    private short y;
    private int[] data;
    private short width;
    private short height;
    private boolean collision;

    public MapTile(int brickId, short x, short y, int[] data, short width, short height, boolean collision) {
        this.brickId = brickId;
        this.x = x;
        this.y = y;
        this.data = data;
        this.width = width;
        this.height = height;
        this.collision = collision;
    }

}
