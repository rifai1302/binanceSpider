package observable;

import java.util.ArrayList;

public interface Observable {

    ArrayList<Observer> observers = new ArrayList<>();

    void updateObservers();

    void addObserver(Observer observer);

}
