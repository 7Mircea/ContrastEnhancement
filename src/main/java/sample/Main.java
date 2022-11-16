package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Se ocupă cu procesarea propriu-zisa a imaginii. Sunt folosite urmatoarele controale
 * diferite de cele din Authentication:
 * MenuBar, Menu, MenuItem, ProgressIndicator,ImageView
 */
public class Main extends Application {
    private static String numeFisierSursa;
    private static String numeFisierDestinatie;

    @Override
    public void start(Stage primaryStage) throws Exception{
//        Authentification auth = new Authentification();//creaza un obiect de autentificare
//        boolean authorizedPerson = auth.display();//afiseaza fereastra de autentificare
//        if (!authorizedPerson)//daca persoana nu a fost autorizata opreste aplicatia
//            return;
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));//obtine obiectul parinte pentru GUI
        primaryStage.setTitle("Image Mirroring");//seteaza-i un titlu
        primaryStage.setScene(new Scene(root, 1280, 720));//seteaza fereastra principala si dimensiunile acesteia
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
