package sample.processing;

import java.awt.image.BufferedImage;

public class MatriceImagine extends MatriceAbstract {
    @Override
    public Imagine transformaInImagine() {
        BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < height; i++ ) {
            for (int j = 0; j < width; j++) {
                image.setRGB(j, i, matrice[i][j]);
            }
        }
        Imagine imagine = new Imagine(image);
        return imagine;
    }
}
