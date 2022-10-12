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
            try {
            if (!boundary)  {
                if (sensorArray.getMovingAverage(4) > (sensorArray.getMovingAverage(20) + 15))
                    boundary = true;
            } else if (Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose())
                    < sensorArray.getMovingAverage(20)) {
                        System.out.println("Average stop sell signal");
                        controller.sellSignal();
                    }
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
    }
}
