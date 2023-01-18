package sample.controller;

import com.sun.istack.internal.NotNull;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import sample.contrastenhancement.*;
import sample.model.TextFromImage;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static sample.utils.ErrorCalculation.calculateCER;
import static sample.utils.ErrorCalculation.calculateWER;
import static sample.utils.Utils.*;


public class Controller {
    private static final int resolutionWidth = 1910;
    private static final int resolutionHeight = 1070;
    private final AtomicInteger batchNr = new AtomicInteger(0);
    private final Map<String, TextFromImage> textFromImageList = new HashMap<>();
    private final Map<String, TextFromImage> syncTextFromImageList = Collections.synchronizedMap(textFromImageList);
    @FXML
    private Label ocr_result;
    @FXML
    private ImageView fpbhe;
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

    @FXML
    private void chooseDirectory(ActionEvent event) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("e:\\CTI\\AN2\\SEM1\\ACI\\Proiect\\Dataset\\temp\\input\\"));
        Stage stage = (Stage) anchor_pane.getScene().getWindow();//obtinerea ferestrei principale
        File directory = directoryChooser.showDialog(stage);
        if (directory == null) {//daca nu a fost ales nici un fisier
            System.out.println("Directory doesn't exist");//indica acest lucru
            return;//opreste procesul de prelucrare al imaginii
        }
        File[] contentsInput = directory.listFiles();
        String parent = directory.getParent();
        String outputDirectoryPath = parent + "\\output";
        File[] contentsOutput = new File(outputDirectoryPath).listFiles();
        System.out.println(outputDirectoryPath);
        if (contentsInput != null && contentsOutput != null) {
            Arrays.stream(contentsInput).parallel().forEach(file -> {
                int nr = batchNr.incrementAndGet();
                String name = file.getName();
                System.out.println("current file " + file);
                final BufferedImage image = changeToGray(changeFileToBufferedImage(file));
                BufferedImage imageHE = enhanceContrast(image);
                BufferedImage imageTSIHE = enhanceContrastWithTsihe(image);
                BufferedImage imagePLTHE = enhanceContrastWithPlthe(image);
                BufferedImage imageFPBHE = enhanceContrastWithFpbhe(image);
                File fileHE = changeBufferedImageToFile(imageHE, name + nr);
                File fileTSIHE = changeBufferedImageToFile(imageTSIHE, name + nr);
                File filePLTHE = changeBufferedImageToFile(imagePLTHE, name + nr);
                File fileFPBHE = changeBufferedImageToFile(imageFPBHE, name + nr);
                String textHE = getTextFromImage(fileHE);
                String textTSIHE = getTextFromImage(fileTSIHE);
                String textPLTHE = getTextFromImage(filePLTHE);
                String textFPBHE = getTextFromImage(fileFPBHE);
                syncTextFromImageList.put(name, new TextFromImage(textHE, textTSIHE, textPLTHE, textFPBHE));
            });
            Arrays.stream(contentsOutput).parallel().forEach(currentFile -> {
                String name = currentFile.getName();
                String text = getTextFromImage(currentFile);
                TextFromImage txtFromImg = textFromImageList.get(name);

                showCERandWER(name,text,txtFromImg);
            });
        } else {
            System.out.println("empty directory");
        }
    }

    private void showCERandWER(String fileName,String text, TextFromImage txtFromImg) {
        float heCER = calculateCER(text,txtFromImg.getHe());
        float tsiheCER = calculateCER(text,txtFromImg.getTsihe());
        float pltheCER = calculateCER(text,txtFromImg.getPlthe());
        float fpbheCER = calculateCER(text,txtFromImg.getFpbhe());
        float heWER = calculateWER(text,txtFromImg.getHe());
        float tsiheWER = calculateWER(text,txtFromImg.getTsihe());
        float pltheWER = calculateWER(text,txtFromImg.getPlthe());
        float fpbheWER = calculateWER(text,txtFromImg.getFpbhe());
        saveResults("CER_RESULTS.csv",heCER,tsiheCER,pltheCER,fpbheCER);
        saveResults("WER_RESULTS.csv",heWER,tsiheWER,pltheWER,fpbheWER);

//        System.out.printf("%nFor file %s CER is :%n",fileName);
//        System.out.printf("he :%f. ",heCER);
//        System.out.printf("tsihe :%f. ",tsiheCER);
//        System.out.printf("plthe :%f. ",pltheCER);
//        System.out.printf("fpbhe :%f.",fpbheCER);


    }

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
        getTextFromImage(path);

        BufferedImage originalImage = changeFileToBufferedImage(file);
        if (originalImage == null) {
            System.out.println("file type not supported");
            return;
        }
        final BufferedImage image = changeToGray(originalImage);
        setInitialImage(image);
        histogram_equalization.setImage(changeBufferedImageToJavaFxImage(image));


        BufferedImage imageHE = enhanceContrast(image);
        BufferedImage imageTSIHE = enhanceContrastWithTsihe(image);
        BufferedImage imagePLTHE = enhanceContrastWithPlthe(image);
        BufferedImage imageFPBHE = enhanceContrastWithFpbhe(image);

//        createNewWindows(image, imageHE, imageTSIHE, imagePLTHE, imageFPBHE);
        createNewWindow(image, "Gray image");
        createNewWindow(imageHE, "HE image");
        createNewWindow(imageTSIHE, "TSIHE image");
        createNewWindow(imagePLTHE, "PLTHE image");
        createNewWindow(imageFPBHE, "FPBHE image");
    }

    private void getTextFromImage(String path) {
        File image = new File(path);
        Tesseract tesseract = getTesseract();
        try {
            String result = tesseract.doOCR(image);
            ocr_result.setText(result);
            System.out.println(result);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
    }

    private String getTextFromImage(File image) {
        Tesseract tesseract = getTesseract();
        String result = null;
        try {
            result = tesseract.doOCR(image);
        } catch (TesseractException e) {
            e.printStackTrace();
        }
        return result;
    }

    private Tesseract getTesseract() {
        Tesseract tesseract = new Tesseract();

        tesseract.setDatapath("src\\main\\resources\\tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);
        tesseract.setOcrEngineMode(1);
        return tesseract;
    }

    private BufferedImage enhanceContrastWithFpbhe(BufferedImage image) {
        BufferedImage imageFPBHE = createCopyImage(image);
        Histogram histogram = computeHistogram(imageFPBHE);
        FPBHE.he(histogram);
        Image newImage = createImageFromByteArray(imageFPBHE, histogram.getArr(), imageFPBHE.getHeight(), imageFPBHE.getWidth());
        fpbhe.setImage(newImage);
        return imageFPBHE;
    }

    private void createNewWindow(BufferedImage image, String title) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getClassLoader().getResource("detail.fxml"));
        Parent root;
        try {
            root = fxmlLoader.load();
        } catch (IOException e) {
            System.out.println("error ar reading fxml ui files");
            e.printStackTrace();
            return;
        }
        BaseController grayController = fxmlLoader.getController();
        grayController.setImage(image);
        grayController.setHistogram(image);
        Stage stageGray = new Stage();
        stageGray.setTitle(title);
        stageGray.setScene(new Scene(root, resolutionWidth, resolutionHeight));
        stageGray.show();
    }

    private BufferedImage enhanceContrastWithPlthe(@NotNull final BufferedImage image) {
        BufferedImage imagePLTHE = createCopyImage(image);
        Histogram histogram = computeHistogram(imagePLTHE);
        PLTHE.he(imagePLTHE.getHeight(), imagePLTHE.getWidth(), histogram);
        Image newImage = createImageFromByteArray(imagePLTHE, histogram.getArr(), imagePLTHE.getHeight(), imagePLTHE.getWidth());
        plthe.setImage(newImage);
        return imagePLTHE;
    }


    private BufferedImage enhanceContrastWithTsihe(@NotNull final BufferedImage image) {
        BufferedImage imageTSIHE = createCopyImage(image);
        Histogram histogram = computeHistogram(imageTSIHE);
        TSIHE.he(imageTSIHE.getHeight(), imageTSIHE.getWidth(), histogram);
        Image newImage = createImageFromByteArray(imageTSIHE, histogram.getArr(), imageTSIHE.getHeight(), image.getWidth());
        tsihe.setImage(newImage);
        return imageTSIHE;
    }

    private BufferedImage enhanceContrast(@NotNull final BufferedImage image) {
        BufferedImage imageHE = createCopyImage(image);
        Histogram histogram = computeHistogram(imageHE);
        HistogramEqualization.he(imageHE.getHeight(), imageHE.getWidth(), histogram);

        Image newImage = createImageFromByteArray(imageHE, histogram.getArr(), imageHE.getHeight(), imageHE.getWidth());
        histogram_equalization.setImage(newImage);
        return imageHE;
    }


    private Histogram computeHistogram(@NotNull BufferedImage image) {
        byte[] arr = getArrayOfPixels(image);

        return new Histogram(arr, image.getHeight(), image.getWidth());
    }


    private void setInitialImage(BufferedImage image) {
        image_selected.setImage(changeBufferedImageToJavaFxImage(image));//seteaza imaginea de afisat
    }


}
