package sample;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.Observable;

public class GetProgressService extends Service<Void> {
    @Override
    protected GetProgressTask createTask() {
        return new GetProgressTask();
    }//intorc obiectul de tip task
}
