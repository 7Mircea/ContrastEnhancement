package sample.controller;

import com.sun.istack.internal.NotNull;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import sample.contrastenhancement.PLTHE;
import sample.contrastenhancement.TSIHE;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static sample.utils.Utils.*;


public class Controller {
    @FXML
    private ImageView plthe;
    @FXML
    private AnchorPane anchor_pane;

    @FXML
    private ImageView image_selected;

    @FXML
    private ImageView histogram_equalization;
    @FXML
    private ImageView tsihe;

    private static final int resolutionWidth = 1910;
    private static final int resolutionHeight = 1070;


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


        BufferedImage originalImage = changeFileToBufferedImage(file);
        if (originalImage == null) {
            System.out.println("file type not supported");
            return;
        }
        final BufferedImage image = changeToGray(originalImage);
        setInitialImage(image);

        histogram_equalization.setImage(changeBufferedImageToJavaFxImage(image));
        BufferedImage imageHE = createCopyImage(image);
        enhanceContrast(imageHE);

        BufferedImage imageTSIHE = createCopyImage(image);
        enhanceContrastWithTsihe(imageTSIHE);

        BufferedImage imagePLTHE = createCopyImage(image);
        enhanceContrastWithPlthe(imagePLTHE);

        createNewWindows(image, imageHE, imageTSIHE, imagePLTHE);
    }

    private void createNewWindows(BufferedImage imageGray, BufferedImage imageHE, BufferedImage imageTSIHE, BufferedImage imagePLTHE) {
        FXMLLoader grayLoader = new FXMLLoader(getClass().getClassLoader().getResource("gray.fxml"));
        FXMLLoader heLoader = new FXMLLoader(getClass().getClassLoader().getResource("he.fxml"));
        FXMLLoader tsiheLoader = new FXMLLoader(getClass().getClassLoader().getResource("tsihe.fxml"));
        FXMLLoader pltheLoader = new FXMLLoader(getClass().getClassLoader().getResource("plthe.fxml"));

        Parent rootGray;
        Parent rootHE;
        Parent rootTSIHE;
        Parent rootPLTHE;
        try {
            rootGray = grayLoader.load();
            rootHE = heLoader.load();//obtine obiectul parinte pentru GUI
            rootTSIHE = tsiheLoader.load();//obtine obiectul parinte pentru GUI
            rootPLTHE = pltheLoader.load();//obtine obiectul parinte pentru GUI
        } catch (IOException e) {
            System.out.println("error ar reading fxml ui files");
            e.printStackTrace();
            return;
        }

        GrayController grayController = grayLoader.getController();
        grayController.setImage(imageGray);
        grayController.setHistogram(imageGray);

        HEController heController = heLoader.getController();
        heController.setImage(imageHE);
        heController.setHistogram(imageHE);
        TSIHEController tsihe = tsiheLoader.getController();
        tsihe.setImage(imageTSIHE);
        tsihe.setHistogram(imageTSIHE);
        PLTHEController plthe = pltheLoader.getController();
        plthe.setImage(imagePLTHE);
        plthe.setHistogram(imagePLTHE);

        Stage stageGray = new Stage();
        stageGray.setTitle("Gray image");
        stageGray.setScene(new Scene(rootGray, resolutionWidth, resolutionHeight));
        stageGray.show();
        Stage stageHE = new Stage();
        stageHE.setTitle("Histogram Equalization");
        stageHE.setScene(new Scene(rootHE, resolutionWidth, resolutionHeight));
        stageHE.show();
        Stage stageTSIHE = new Stage();
        stageTSIHE.setTitle("Tripartite Sub-Image Histogram Equalization");
        stageTSIHE.setScene(new Scene(rootTSIHE, resolutionWidth, resolutionHeight));
        stageTSIHE.show();
        Stage stagePLTHE = new Stage();
        stagePLTHE.setTitle("Plateau limit-based tri-histogram equalisation");
        stagePLTHE.setScene(new Scene(rootPLTHE, resolutionWidth, resolutionHeight));
        stagePLTHE.show();
    }

    private void enhanceContrastWithPlthe(BufferedImage image) {
        Histogram histogram = computeHistogram(image);
        PLTHE.he(image.getHeight(), image.getWidth(), histogram);
        Image newImage = createImageFromByteArray(image, histogram.getArr(), image.getHeight(), image.getWidth());
        plthe.setImage(newImage);
    }


    private void enhanceContrastWithTsihe(BufferedImage image) {
        Histogram histogram = computeHistogram(image);
        TSIHE.he(image.getHeight(), image.getWidth(), histogram);
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
