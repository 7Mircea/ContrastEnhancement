package sample.processing;

import java.awt.image.BufferedImage;

public abstract class MatriceAbstract implements ExtrageMatrice, ModificaMatrice {
    protected Integer[][] matrice; //matricea de pixeli a imaginii
    protected int width; // latimea imaginii
    protected int height; // inaltimea imaginii
    MatriceAbstract() {
        width = 1;
        height = 1;
        matrice = new Integer[width][height];
    }

    public Integer[][] getMatrice() {
        return matrice;
    }

    @Override
    public void extrageMatrice(Imagine imagine) {
        BufferedImage image =  imagine.getImagine(); // setez imaginea
        assert image != null;
        width = image.getWidth(); //setez latimea imaginii
        height = image.getHeight(); //setez inaltimea imaginii
        matrice = new Integer[height][width]; //aloc matricea
        for (int h = 0; h < height; h++) {
            for (int r = 0; r < width; r++) {
                matrice[h][r] = image.getRGB(r,h); //extrag fiecare pixel si salvez informatia in matrice
            }
        }
    }

    @Override
    public void modificaMatrice() {
        Integer[] rand = new Integer[width];
        for (int h = 0; h < height; h++) {
            //copiem randul din matrice
            for (int r = 0; r < width; r++) {
                rand[r] = matrice[h][r];
            }
            //inversam randul
            for (int r = 0; r < width; r++) {
                matrice[h][r] = rand[width-r-1];
            }
        }
    }

    public abstract Imagine transformaInImagine();
}
