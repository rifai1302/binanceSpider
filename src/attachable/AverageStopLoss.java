package attachable;

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
                if (sensorArray.getMovingAverage(5) > sensorArray.getMovingAverage(20) + 25)
                    boundary = true;
            } else if (Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose())
                    < sensorArray.getMovingAverage(20)) {
                        controller.sellSignal();
                    }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
