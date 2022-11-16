package sample.processing;

public class Timp {
    public static void afiseaza(long[] arr1) {
        System.out.println("Citire informatii identificare a durat "
                + ((arr1[1] - arr1[0]) + (arr1[3] - arr1[4]))
                + " milisecunde.");
        System.out.println("Citire fisier sursa a durat " + (arr1[2] - arr1[1])
                + " milisecunde.");
        System.out.println("Procesare Imagine a durat " + (arr1[3] - arr1[2])
                + " milisecunde.");
        System.out.println("Scriere fisier destinatie a durat " + (arr1[5] - arr1[4])
                + " milisecunde.");
    }
}
