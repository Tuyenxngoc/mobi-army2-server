package com.teamobi.mobiarmy2.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageData {
    private int width;
    private int height;
    private int[] pixelData;
}
