package com.teamobi.mobiarmy2.model.map;

import com.teamobi.mobiarmy2.model.ImageData;
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
    private ImageData image;

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

    public ImageData getNewImage() {
        if (image != null) {
            int[] newPixelData = new int[image.getPixelData().length];
            System.arraycopy(image.getPixelData(), 0, newPixelData, 0, image.getPixelData().length);
            return new ImageData(image.getWidth(), image.getHeight(), newPixelData);
        }
        return null;
    }
}