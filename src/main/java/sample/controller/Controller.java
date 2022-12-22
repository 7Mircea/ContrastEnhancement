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
//        saveCERResult(heCER,tsiheCER,pltheCER,fpbheCER);

        System.out.printf("%nFor file %s CER is :%n",fileName);
        System.out.printf("he :%f. ",heCER);
        System.out.printf("tsihe :%f. ",tsiheCER);
        System.out.printf("plthe :%f. ",pltheCER);
        System.out.printf("fpbhe :%f.",fpbheCER);

    }

    private void saveCERResult(float heCER, float tsiheCER, float pltheCER, float fpbheCER) {
        try {
            File txtFile = new File("CER_RESULT.csv");
            if (!txtFile.exists()) {
                txtFile.createNewFile();
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(txtFile));
            String result = String.format("%f,%f,%f,%f %n",heCER,tsiheCER,pltheCER,fpbheCER);
            writer.append(result);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        createNewWindows(image, imageHE, imageTSIHE, imagePLTHE, imageFPBHE);
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

    private void createNewWindows(BufferedImage imageGray, BufferedImage imageHE, BufferedImage imageTSIHE, BufferedImage imagePLTHE, BufferedImage imageFPBHE) {
        FXMLLoader grayLoader = new FXMLLoader(getClass().getClassLoader().getResource("gray.fxml"));
        FXMLLoader heLoader = new FXMLLoader(getClass().getClassLoader().getResource("he.fxml"));
        FXMLLoader tsiheLoader = new FXMLLoader(getClass().getClassLoader().getResource("tsihe.fxml"));
        FXMLLoader pltheLoader = new FXMLLoader(getClass().getClassLoader().getResource("plthe.fxml"));
        FXMLLoader fpbheLoader = new FXMLLoader(getClass().getClassLoader().getResource("fpbhe.fxml"));

        Parent rootGray;
        Parent rootHE;
        Parent rootTSIHE;
        Parent rootPLTHE;
        Parent rootFPBHE;
        try {
            rootGray = grayLoader.load();
            rootHE = heLoader.load();//obtine obiectul parinte pentru GUI
            rootTSIHE = tsiheLoader.load();//obtine obiectul parinte pentru GUI
            rootPLTHE = pltheLoader.load();//obtine obiectul parinte pentru GUI
            rootFPBHE = fpbheLoader.load();//obtine obiectul parinte pentru GUI
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
        FPBHEController fpbhe = fpbheLoader.getController();
        fpbhe.setImage(imageFPBHE);
        fpbhe.setHistogram(imageFPBHE);

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
        Stage stageFPBHE = new Stage();
        stageFPBHE.setTitle("Feature-preserving bi-histogram equalization");
        stageFPBHE.setScene(new Scene(rootFPBHE, resolutionWidth, resolutionHeight));
        stageFPBHE.show();
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
