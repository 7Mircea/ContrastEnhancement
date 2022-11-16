package sample;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.*;
import java.util.HashSet;

public class Authentification {
    private static boolean answer = false;
    private final static Stage windowAuth = new Stage();

    /**
     * display a new window(stage) and check the personal info submitted by the user
     * or create an account. Uses(12) Label, TextField, PasswordField, Tab,Button,ChoiceBox,
     * CheckBox,HyperLink,WebView,RadioButtons,ProgressBarr,ToggleGroup
     */
    public boolean display() {

        windowAuth.initModality(Modality.APPLICATION_MODAL);//face ca aceasta fereastra sa fie singura cu care se poate
        //interactiona
        windowAuth.setTitle("Autentificare"); //setam titlul ferestrei

        //signInTab content
        GridPane signInGrid = new GridPane();//setam containerul din interiorul primului tab
        signInGrid.setAlignment(Pos.CENTER);//setam alinierea acestuia
        signInGrid.setHgap(10);//setam marginile pe verticala
        signInGrid.setVgap(10);//setam marginile pe orizontala
        signInGrid.setPadding(new Insets(25, 25, 25, 25));//setam umplerea


        Text signInTitle = new Text("Sign-In");//cream titlul containerului signInGrid
        signInTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));//setam fontul lui signInGrid
        signInGrid.add(signInTitle, 0, 0, 2, 1);//setam titlul containerului signInGrid

        Label userNameLabel = new Label("User Name:");//cream eticheta pentru introducerea numelui de utilizator
        signInGrid.add(userNameLabel, 0, 1);//setam eticheta pentru introducerea numelui de utilizator

        TextField userTextField = new TextField();//cream campul pentru introducerea numelui
        signInGrid.add(userTextField, 1, 1);//il adaugam la container

        Label pw = new Label("Password:");//cream eticheta pentru parola
        signInGrid.add(pw, 0, 2);//o adaugam la container

        PasswordField pwBoxSignIn = new PasswordField();//cream campul de introdus parola
        signInGrid.add(pwBoxSignIn, 1, 2);//il adaugam la container

        Label labelRezultat = new Label();//cream un label pentru rezultat

        Button buttonSignIn = new Button("Sign In");//cream un button pentru autentificare
        Gson gson = new Gson();//cream un obiect gson pentru parsarea fisierului cu utilizatorii
        //deja inregritrati
        buttonSignIn.setOnAction(e -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(new File("utilizatori.json"))));//cream un obiect pentru citirea
                //fisierului cu utilizatori deja inregistrati
                String userName = userTextField.getText();//cream un string in care salvam numele introdus
                String password = pwBoxSignIn.getText();//cream un string in care salvam parola introdusa
                HashSet<Persoana> listaPersoane = gson.fromJson(reader, new TypeToken<HashSet<Persoana>>() {
                }.getType());////cream un HashSet in cu persoanele introduse
                if (listaPersoane == null || listaPersoane.isEmpty()) { //daca lista de persoana este nula sau goala
                    labelRezultat.setText("Nu exista persoane in baza de date");//afiseaza acest lucru
                    return;//si opreste procesul de autentificare
                }
                Persoana persoana = new Persoana(userName, password);//cream o persoana noua
                if (listaPersoane.contains(persoana)) {//daca lista de persoana o contine
                    answer = true;//raspunsul functiei este true
                    reader.close();//inchidem obiectul de citire
                    windowAuth.close();//inchidem aceasta fereastra
                } else {
                    labelRezultat.setText("Introduceti din nou datele de autentificare.");//cerem introducerea datelor
                }
            } catch (IOException e1) {
                e1.printStackTrace();//afiseaza eroarea si functiiele implicate
            }
        });
        signInGrid.add(buttonSignIn, 1, 3);//adauga butonul de autentificare la container

        signInGrid.add(labelRezultat, 0, 4, 2, 1);//adauga eticheta cu rezultatul
        // la container

        //signUp content
        GridPane signUpGrid = new GridPane();//creaza un nou container pentru tabul 2
        signUpGrid.setAlignment(Pos.CENTER);//aliniaza la centru
        signUpGrid.setHgap(10);//seteaza maginea pe orizontala
        signUpGrid.setVgap(10);//seteaza maginea pe verticala
        signUpGrid.setPadding(new Insets(25, 25, 25, 25));//seteaza umplerea


        Text signUpTitle = new Text("Sign-Up");//seteaza titlul pentru container
        signUpTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));//seteaza fontul titlului
        signUpGrid.add(signUpTitle, 0, 0, 2, 1);//se adauga titlul la container

        Label userNameSignUp = new Label("User Name:");//se creeaza eticheta pentru utilizator
        signUpGrid.add(userNameSignUp, 0, 1);//se adauga eticheta pentru utilizator
        TextField userTextFieldSignUp = new TextField();//se creeaza textul pentur eticheta
        signUpGrid.add(userTextFieldSignUp, 1, 1);//se adauga textul pentru eticheta

        Label labelCountry = new Label("Country :");//se creeaza eticheta pentru alegerea tarii
        signUpGrid.add(labelCountry, 0, 2);//se adauga eticheta
        ChoiceBox<String> countryChoiceBox = new ChoiceBox<>();// se creaza cutia cu alegeri
        countryChoiceBox.getItems().addAll("USA", "RO", "FR");//se introduc optiunile
        signUpGrid.add(countryChoiceBox, 1, 2);//se adauga cutia cu alegeri la container

        final ToggleGroup group = new ToggleGroup();//se creeaza grupul cu o singură variantă ce poate fi aleasa


        Label genderLabel = new Label("Gender");//crearea etichetei pentru gen
        RadioButton rb1 = new RadioButton("Male");//prima varianta
        rb1.setToggleGroup(group);//setarea grupului pentru prima varianta
        RadioButton rb2 = new RadioButton("Female");//a doua varianta
        rb2.setToggleGroup(group);//setarea grupului pentru a doua varianta
        RadioButton rb3 = new RadioButton("Prefer not to say");//a treia varianta
        rb3.setToggleGroup(group);//setarea grupului pentru a treia varianta
        rb1.setSelected(true);//setarea variantei implicite
        signUpGrid.add(genderLabel, 0, 3);//setarea etichetei
        signUpGrid.add(rb1, 1, 3);//setarea primei variante
        signUpGrid.add(rb2, 1, 4);//setarea celei de a doua varianta
        signUpGrid.add(rb3, 1, 5);//setarea celei de a treia varianta

        Label pwSignUp = new Label("Password:");//setarea etichetei pentru parola
        signUpGrid.add(pwSignUp, 0, 6);//adaugarea etichetei la container
        PasswordField pwBoxSignUp = new PasswordField();//camp pentru introducerea parolei
        signUpGrid.add(pwBoxSignUp, 1, 6);//adaugarea campului la container

        CheckBox acceptTCCheckBox = new CheckBox("I agree with ");//crearea CheckBox pentru verificarea acceptarii T&C
        acceptTCCheckBox.setIndeterminate(false);//setarea starii implicite a CheckBox
        signUpGrid.add(acceptTCCheckBox, 0, 7);//adaugarea CheckBox la container
        Hyperlink hyperlink = new Hyperlink("T&C");//T&C vine de la terms and condition

        signUpGrid.add(hyperlink, 1, 7);//adaugarea hyperlinkului la container
        Label labelRezultatSignUp = new Label();//crearea unei etichete pentru aficarea rezultatului
        //aceasta va spune daca datele au fost introduse cu succes sau daca au fost erori la introducere

        final ProgressBar progressBarSignUp = new ProgressBar(0);//crearea unui progress bar
        final GetProgressService service = new GetProgressService();//creerea unui serviciu pentru citirea
        // utilizatorilor introdusi
        progressBarSignUp.progressProperty().bind(service.progressProperty());//legarea proprietatii de progress a serviciului
        //de cea a progress barului. Astfel se poate observa progresul realizat de serviciu in salvarea datelor
        //persoanei introduse
        BooleanProperty onService = new SimpleBooleanProperty(false);//crearea unei proprietii boolene simple
        onService.bind(service.runningProperty());//legarea acesteia la progresul serviciului
        onService.addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue == false && oldValue == true) {
                    windowAuth.close();//cand serviciul a terminat de salvat datele persoanei fereastra se inchide
                }
            }
        });

        Button buttonSignUp = new Button("Sign Up");//crearea butonului pentru introducerea datelor
        buttonSignUp.setOnAction(e -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("utilizatori.json"))));
                //crearea unui obiect pentru a citi datele deja introduse
                String userName = userTextFieldSignUp.getText();//string cu numele persoanei introduse
                String country = (String) countryChoiceBox.getValue();//string cu tara persoanei introduse
                String password = pwBoxSignUp.getText();//string cu parola persoanei introduse
                boolean acceptTermsAndConditions = acceptTCCheckBox.isSelected();//boolean ce indica acceptarea T&C

                HashSet<Persoana> listaPersoane = gson.fromJson(reader, new TypeToken<HashSet<Persoana>>() {
                    //lista persoane deja introduse
                }.getType());
                if (listaPersoane == null) {//daca lista nu a putut fi extrasa
                    labelRezultatSignUp.setText("Nu se pot adauga persoane");//nu putem salva o noua persoana in ea
                    return;//oprim procesul de salvare a numelui
                }
                if (!acceptTermsAndConditions) {//daca termenii si condiitile nu au fost acceptate
                    labelRezultatSignUp.setText("Dacă nu accepți termenii și condițiile nu poți folosi aplicația.");
                    //informama utilizatorul ca fara aceptarea acestora nu putem trece mai departe
                    return;//oprim procesul de prelucrarea a datelor
                }
                Persoana persoana = new Persoana(userName, country, password);//cream o noua persoana
                if (!listaPersoane.contains(persoana)) {//daca lista nu contine deja aceasta persoana
                    answer = true;//raspunsul este true
                    listaPersoane.add(persoana);//adaugam persoana
                    String fisierPersoane = gson.toJson(listaPersoane);//transformam lista in string json
                    FileWriter writer = new FileWriter("utilizatori.json");//salvam stringul
                    writer.write(fisierPersoane);//scriem stringul  in fisier
                    writer.flush();//eliberam bufferul obiectului de scriere
                    writer.close();//inchidem obiectul de scriere
                    service.start();//pornim serviciul ce simuleaza procesul de scriere
                } else {
                    labelRezultatSignUp.setText("Exista deja o persoana cu aceleasi date de identificare");
                    //indicam ca exita deja o persoana cu aceleasi date de identificare
                }
            } catch (IOException e1) {
                e1.printStackTrace();//afisam exceptia
            }
        });
        signUpGrid.add(buttonSignUp, 1, 8);//adaugam buttonul de inregistrare
        signUpGrid.add(labelRezultatSignUp, 0, 9, 2, 1);//adaugam butonul cu rezultatul
        final WebView browser = new WebView();//cream un WebView
        final WebEngine webEngine = browser.getEngine();//obtinum motorul acestuia
        hyperlink.setOnAction(e -> {
            webEngine.load("https://en.wikipedia.org/wiki/Terms_of_service");//pornim pagina cu datele T&C
            //am afisat in scop didactic o pagina de pe wikipedia
        });

        signUpGrid.add(progressBarSignUp, 0, 10, 2, 1);//adaugare bara de progress
        signUpGrid.add(browser, 0, 11, 2, 4);//adaugare WebView

        //---------------------------------------
        TabPane tabPane = new TabPane();//crearea containerului de taburi
        Tab signInTab = new Tab();//crearea tabului de autentificare
        signInTab.setText("Sign In");//adaugarea titlului pentru primul tab
        signInTab.setContent(signInGrid);//adaugarea continului pentru primul tab
        tabPane.getTabs().add(signInTab);//adaugarea tabului in containerul parinte


        Tab signUpTab = new Tab();//crearea unui tab
        signUpTab.setText("Sign Up");//adaugarea titlui pentru tabul al doilea
        signUpTab.setContent(signUpGrid);//salvarea continutului pentru al doilea tab
        tabPane.getTabs().add(signUpTab);//adaugarea tabului in containerul parinte

        Scene scene = new Scene(tabPane, 1280, 720);//crearea scenei
        windowAuth.setScene(scene);//adaugarea scenei la fereastra

        windowAuth.showAndWait();
        return answer;
    }

    private void shutdownWindow() {
        windowAuth.close();
    }


}
