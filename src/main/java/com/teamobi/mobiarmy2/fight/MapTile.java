package com.teamobi.mobiarmy2.fight;

import com.teamobi.mobiarmy2.util.Utils;
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

    public boolean isCollision(short x, short y) {
        return (Utils.inRegion(x, y, this.x, this.y, this.width, this.height)
                && Utils.isNotAlpha(getARGB(x - this.x, y - this.x)));
    }

    public final int getARGB(int x, int y) {
        return data[y * width + x];
    }

}
