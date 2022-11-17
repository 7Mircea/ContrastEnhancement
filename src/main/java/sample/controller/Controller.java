package sample.controller;

import com.sun.istack.internal.NotNull;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sample.contrastenhancement.Histogram;
import sample.contrastenhancement.HistogramEqualization;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import static sample.utils.Utils.*;


public class Controller {
    @FXML
    private AnchorPane anchor_pane;

    @FXML
    private ImageView image_selected;

    @FXML
    private ImageView image_changed;


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


        BufferedImage image = changeFileToBufferedImage(file);
        image = changeToGray(image);
        setInitialImage(image);
        image_changed.setImage(changeBufferedImageToJavaFxImage(image));
        enhanceContrast(image);

    }

    private void enhanceContrast(@NotNull BufferedImage image) {
        Histogram histogram = computeHistogram(image);
        HistogramEqualization.he(image.getHeight(),image.getWidth(),histogram);


        Image newImage = createImageFromByteArray(image,histogram.getArr(),image.getHeight(),image.getWidth());
        image_changed.setImage(newImage);
    }



    private Histogram computeHistogram(@NotNull BufferedImage image) {
        byte[] arr = getArrayOfPixels(image);

        return new Histogram(arr,image.getHeight(),image.getWidth());
    }



    private void setInitialImage(BufferedImage image) {
        image_selected.setImage(changeBufferedImageToJavaFxImage(image));//seteaza imaginea de afisat
    }


}
