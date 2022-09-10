package controller.strategist;

import controller.Trade;
import controller.Controller;
import model.SensorArray;

public class AverageStopLoss implements Runnable {

    private final SensorArray sensorArray;
    private final Controller controller;
    private final Trade trade;
    private boolean boundary = false;

    public AverageStopLoss(SensorArray sensorArray, Controller controller, Trade trade) {
        this.sensorArray = sensorArray;
        this.controller = controller;
        this.trade = trade;
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
