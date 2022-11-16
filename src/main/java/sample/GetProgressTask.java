package sample;

import javafx.concurrent.Task;

import java.util.Observable;

/**
 * Acest obiect simuleaza salvarea utilizatorilor intr-o baza de date
 */
public class GetProgressTask extends Task<Void> {
    @Override
    protected Void call() throws Exception {
        for (int i = 0; i < 11; ++i) {//de 11 ori
            if (isCancelled()) {//daca firul de executie a fost oprit
                updateMessage("Task canceled");//updateaza mesajul asociat firului
                break;//operste
            }
            try {
                updateProgress(i * 0.1, 1.);//updateaza proprietatea ce indica progresul fislui de exec
                Thread.sleep(100);//adoarme firul de executie
            } catch (InterruptedException e) {
                if (isCancelled()) {//daca firul a fost oprit
                    System.out.println("Task canceled");//afiseaza acest lucru
                }
                System.out.println("Thread  interrupted.");//afiseaza ca a avut loc o intrerupere
            }
        }

        return null;
    }
}
