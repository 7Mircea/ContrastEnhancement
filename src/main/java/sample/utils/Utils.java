package sample.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;

import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

public class Utils {

    public static Image createImageFromByteArray(BufferedImage bufferedImage, byte[] arr, int height, int width) {
        setArrayOfPixels(bufferedImage, arr);
        return changeBufferedImageToJavaFxImage(bufferedImage);
    }


    /**
     * creates a hard copy of original
     * @param original image to be copied
     * @return hard copy of original
     */
    public static BufferedImage createCopyImage(BufferedImage original) {
        BufferedImage newImage = new BufferedImage(original.getWidth(), original.getHeight(), TYPE_BYTE_GRAY);
        setArrayOfPixels(newImage,((DataBufferByte)original.getData().getDataBuffer()).getData());
        return newImage;
    }

    public static Image changeBufferedImageToJavaFxImage(BufferedImage image) {
        if (image == null) return null;
        return SwingFXUtils.toFXImage(image, null);
    }

    public static BufferedImage changeFileToBufferedImage(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static BufferedImage changeToGray(BufferedImage image) {

        // create a grayscale image the same size
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(),
                TYPE_BYTE_GRAY);

        // convert the original colored image to grayscale
        ColorConvertOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                gray.getColorModel().getColorSpace(), null);
        op.filter(image, gray);
        image = null;
        return gray;

    }

    /**
     * @param image will have the contents changed to those of arr
     * @param arr   new content for image
     */
    public static void setArrayOfPixels(BufferedImage image, byte[] arr) {
        SampleModel sampleModel = image.getSampleModel();
        DataBufferByte bufferByte = new DataBufferByte(arr, arr.length);
        Raster r = Raster.createRaster(sampleModel, bufferByte, new Point());
        image.setData(r);
    }

    public static byte[] getArrayOfPixels(BufferedImage image) {
        return ((DataBufferByte) image.getData().getDataBuffer()).getData();
    }

}
