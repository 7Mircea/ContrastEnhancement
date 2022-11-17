package sample.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

import static java.awt.image.BufferedImage.TYPE_BYTE_GRAY;

public class Utils {
    public static BufferedImage createBufferedImageFromByteArr(byte[] arr, int height, int width) {
        BufferedImage image = new BufferedImage(width, height, TYPE_BYTE_GRAY);
        try {
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            out.write(arr);
//
//            ImageIO.write(image, "png", out);
//            File outputfile = new File("image.jpg");
//            ImageIO.write(bufferedImage, "jpg", outputfile);
            ByteArrayInputStream in = new ByteArrayInputStream(arr);
            int availableBytes = in.available();
            System.out.println("available bytes " + availableBytes);
            image = ImageIO.read(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    public static Image createImageFromByteArray(BufferedImage bufferedImage,byte[] arr, int height, int width) {
        setArrayOfPixels(bufferedImage,arr);
        return changeBufferedImageToJavaFxImage(bufferedImage);
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
     *
     * @param image will have the contents changed to those of arr
     * @param arr new content for image
     */
    public static void setArrayOfPixels(BufferedImage image,byte[] arr) {
        SampleModel sampleModel= image.getSampleModel();
        DataBufferByte bufferByte = new DataBufferByte(arr,arr.length);
        Raster r =Raster.createRaster(sampleModel,bufferByte,new Point());
        image.setData(r);
    }

    public static byte[] getArrayOfPixels(BufferedImage image) {
        return ((DataBufferByte)image.getData().getDataBuffer()).getData();
    }

}
