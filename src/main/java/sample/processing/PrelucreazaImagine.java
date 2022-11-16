package sample.processing;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface PrelucreazaImagine {
    BufferedImage getImagine();
    void setImagine() throws IOException;
    void printImagine() throws IOException;
}
