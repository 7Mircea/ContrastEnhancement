package sample;

import com.google.gson.annotations.SerializedName;

public class Persoana {
    @SerializedName("nume")
    private String name;
    @SerializedName("tara")
    private String country;
    @SerializedName("parola")
    private String password;

    public Persoana() {
    }

    public Persoana(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public Persoana(String name, String country, String password) {
        this.name = name;
        this.country = country;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;//daca obiectul cu care se compara este null declara diferit
        if (this.getName().equals(((Persoana) obj).getName()) &&
                this.getPassword().equals(((Persoana) obj).getPassword()))
            return true;//compara doar numele si parola celor doua persoane
        return false;//si returneaza un raspuns corespunzator situatiei
    }

    @Override
    public int hashCode() {
        if (name == null || password == null)//daca numele sau parola sunt nule returneaza 0
            return 0;
        return name.hashCode() + password.hashCode();//returneaza doar hash pentru nume si parola
        // acestea fiind singurele care trebuie sa fie unice
    }

    @Override
    public String toString() {
        return "Persoana{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
