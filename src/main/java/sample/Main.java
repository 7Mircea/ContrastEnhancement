package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Se ocupă cu procesarea propriu-zisa a imaginii. Sunt folosite urmatoarele controale
 * diferite de cele din Authentication:
 * MenuBar, Menu, MenuItem, ProgressIndicator,ImageView
 */
public class Main extends Application {
    private static String numeFisierSursa;
    private static String numeFisierDestinatie;

    private static final int resolutionWidth = 1910;
    private static final int resolutionHeight = 1070;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getClassLoader().getResource("sample.fxml")));//obtine obiectul parinte pentru GUI
        primaryStage.setTitle("Image Contrast Enhancement");//seteaza-i un titlu
        primaryStage.setScene(new Scene(root, resolutionWidth, resolutionHeight));//seteaza fereastra principala si dimensiunile acesteia
        //in pixeli
        primaryStage.show();//afiseaza fereastra
    }


    public static void main(String[] args) {
        if (args.length == 2) {//daca au fost introduse numele fisierului sursa si destinatie de la tastatura
            numeFisierSursa = args[0];//salveaza numele fisierului sursa
            numeFisierDestinatie = args[1];//salveaza numele fisierului destinatie
        } else {
            numeFisierSursa = "Taj.bmp";//altfel alege numele de
            numeFisierDestinatie = "Taj2.bmp";//fisierele implicite
        }
        launch(args);//lanseaza
    }
}
