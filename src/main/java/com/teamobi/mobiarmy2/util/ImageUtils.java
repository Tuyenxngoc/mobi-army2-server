package com.teamobi.mobiarmy2.util;

import com.teamobi.mobiarmy2.fight.ImageData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    private ImageUtils() {
    }

    /**
     * Load image data from a file.
     *
     * @param filePath the path to the image file
     * @return the loaded ImageData
     * @throws IOException if there is an error reading the file
     */
    public static ImageData loadImageData(String filePath) throws IOException {
        BufferedImage img = ImageIO.read(new File(filePath));
        int width = img.getWidth();
        int height = img.getHeight();

        int[] pixelData = new int[width * height];
        img.getRGB(0, 0, width, height, pixelData, 0, width);

        return new ImageData(width, height, pixelData);
    }
}
