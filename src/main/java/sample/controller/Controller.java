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
import org.jfree.chart.JFreeChart;
import sample.contrastenhancement.Histogram;
import sample.contrastenhancement.HistogramEqualization;
import sample.contrastenhancement.TSIHE;

import java.awt.image.BufferedImage;
import java.io.File;

import static sample.utils.HistogramDraw.createGraphicHistogram;
import static sample.utils.Utils.*;


public class Controller {
    @FXML
    public ImageView image_selected_hist;
    @FXML
    public ImageView histogram_equalization_hist;
    @FXML
    public ImageView tsihe_hist;
    @FXML
    private AnchorPane anchor_pane;

    @FXML
    private ImageView image_selected;

    @FXML
    private ImageView histogram_equalization;
    @FXML
    public ImageView tsihe;

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
        if (image == null) {
            System.out.println("file type not supported");
            return;
        }
        image = changeToGray(image);
        setInitialImage(image);
        displayHistogram(image, image_selected_hist);

        histogram_equalization.setImage(changeBufferedImageToJavaFxImage(image));
        BufferedImage imageHE = createCopyImage(image);
        enhanceContrast(imageHE);
        displayHistogram(imageHE, histogram_equalization_hist);
        BufferedImage imageTSIHE =createCopyImage(image);
        enhanceContrastWithTsihe(imageTSIHE);
        displayHistogram(imageTSIHE,tsihe_hist);
    }

    private void displayHistogram(BufferedImage image, ImageView imageView) {
        JFreeChart chart = createGraphicHistogram(image);
        BufferedImage chartImage =  chart.createBufferedImage(image.getWidth(),image.getHeight());
        imageView.setImage(changeBufferedImageToJavaFxImage(chartImage));
    }

    private void enhanceContrastWithTsihe(BufferedImage image) {
        Histogram histogram = computeHistogram(image);
        TSIHE.he(image.getHeight(),image.getWidth(),histogram);
        Image newImage = createImageFromByteArray(image, histogram.getArr(), image.getHeight(), image.getWidth());
        tsihe.setImage(newImage);
    }

    private void enhanceContrast(@NotNull BufferedImage image) {
        Histogram histogram = computeHistogram(image);
        HistogramEqualization.he(image.getHeight(), image.getWidth(), histogram);

        Image newImage = createImageFromByteArray(image, histogram.getArr(), image.getHeight(), image.getWidth());
        histogram_equalization.setImage(newImage);
    }


    private Histogram computeHistogram(@NotNull BufferedImage image) {
        byte[] arr = getArrayOfPixels(image);

        return new Histogram(arr, image.getHeight(), image.getWidth());
    }


    private void setInitialImage(BufferedImage image) {
        image_selected.setImage(changeBufferedImageToJavaFxImage(image));//seteaza imaginea de afisat
    }


}
