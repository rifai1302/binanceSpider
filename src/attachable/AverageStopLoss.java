package attachable;

import controller.Trade;
import controller.Controller;
import model.SensorArray;

public class AverageStopLoss extends Attachable implements Runnable {

    private final SensorArray sensorArray;
    private final Controller controller;
    private boolean boundary = false;

    public AverageStopLoss(SensorArray sensorArray, Controller controller) {
        this.sensorArray = sensorArray;
        this.controller = controller;
    }

    @Override
    public void run() {
        while (trade.isOpen()) {
            if (!boundary)  {
                if (sensorArray.getMovingAverage(5) > sensorArray.getMovingAverage(20) + 50)
                    boundary = true;
            } else if (sensorArray.getMovingAverage(5) < sensorArray.getMovingAverage(20)) {
                        controller.sellSignal();
                    }
        }
    }
}
