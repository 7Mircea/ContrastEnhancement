package sample.processing;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public abstract class ImagineAbstract implements Fisier,PrelucreazaImagine {
    protected BufferedImage image;
    protected File fileSursa;
    protected File fileDestinatie;

    @Override
    public BufferedImage getImagine() {
        return image;
    }

    @Override
    public void setImagine() throws IOException {
        //citim imaginea
        image = ImageIO.read(fileSursa);
    }

    @Override
    public void printImagine() throws IOException {
        //scriem imaginea in acelasi folder cu imaginea sursa
        ImageIO.write(image,"bmp",fileDestinatie);
    }

    @Override
    public void setFisierSursa(String string) {
        fileSursa = new File(string);
    }

    @Override
    public void setFisierDestinatie(String string) { fileDestinatie = new File(string);}

    public File getFileDestinatie() {return fileDestinatie;}

    public abstract void afiseaza();
}
