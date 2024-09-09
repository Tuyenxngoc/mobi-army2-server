package com.teamobi.mobiarmy2.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageData {
    public int width;
    public int height;
    public int[] pixelData;

    public ImageData(int width, int height, int[] pixelData) {
        this.width = width;
        this.height = height;
        this.pixelData = pixelData;
    }
}
