package sample.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.contrastenhancement.Histogram;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class Controller {
    @FXML
    private AnchorPane anchor_pane;

    @FXML
    private ImageView image_selected;

    @FXML
    private ImageView image_changed;

    @FXML
    private ProgressIndicator indicator;

    @FXML
    private MenuBar menu_bar;

    @FXML
    private Menu file;

    @FXML
    private MenuItem menu_item;


    @FXML
    private void chooseFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();//crearea ferestrei de ales fisiere
        Stage stage = (Stage) anchor_pane.getScene().getWindow();//obtinerea ferestrei principale
        File file = fileChooser.showOpenDialog(stage);//afisarea ferestrei de ales fisiere
        if (file == null) {//daca nu a fost ales nici un fisier
            System.out.println("File is null");//indica acest lucru
            return;//opreste procesul de prelucrare al imaginii
        }
        String path = file.getAbsolutePath();//obtine calea absoluta catre acel fisier

        setInitialImage(file);
        BufferedImage image = changeFileToBufferedImage(file);
        image = changeToGray(image);
        image_changed.setImage(changeBufferedImageToJavaFxImage(image));
        computeHistogram(image);
    }

    private Image changeBufferedImageToJavaFxImage(BufferedImage image) {
        return SwingFXUtils.toFXImage(image, null);
    }

    private void computeHistogram(BufferedImage image) {
        byte[] arr = getArrayOfPixels(image);

        Histogram histogram = new Histogram(arr,image.getHeight(),image.getWidth());
    }

    private byte[] getArrayOfPixels(BufferedImage image) {
        return ((DataBufferByte)image.getData().getDataBuffer()).getData();
    }

    private BufferedImage changeToGray(BufferedImage image) {

        // create a grayscale image the same size
        BufferedImage gray = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        // convert the original colored image to grayscale
        ColorConvertOp op = new ColorConvertOp(
                image.getColorModel().getColorSpace(),
                gray.getColorModel().getColorSpace(), null);
        op.filter(image, gray);
        image = null;
        return gray;

    }

    public BufferedImage changeFileToBufferedImage(File file) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private void setInitialImage(File file) {
        Image selected = null;//declara un obiect Image pentru a-l afisa
        try {
            selected = new Image(new FileInputStream(file));//salveaza un obiect Image pentru a-l afisa
        } catch (FileNotFoundException e) {
            e.printStackTrace();//afiseaza eroare
            return;//opreste functia
        }
        image_selected.setImage(selected);//seteaza imaginea de afisat
    }
}
