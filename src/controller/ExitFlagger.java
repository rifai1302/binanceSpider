package controller;

import com.binance.api.client.domain.market.CandlestickInterval;
import model.SensorArray;

public class ExitFlagger implements Runnable {

    private final SensorArray sensorArray;
    private final Controller controller;
    private final boolean longExit;

    public ExitFlagger(SensorArray sensorArray, Controller controller, boolean longExit) {
        this.sensorArray = sensorArray;
        this.controller = controller;
        this.longExit = longExit;
    }

    @Override
    public void run() {
        boolean flagged = false;
        while (!flagged) {
                    if ((sensorArray.getMAIncrease(CandlestickInterval.FIVE_MINUTES, 4) < 3)
                            || (sensorArray.getMovingAverage(4) < sensorArray.getMovingAverage(7))
                            || (Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose()) < sensorArray.getMovingAverage(6))) {
                        controller.sellSignal();
                        flagged = true;
                    }
        }
    }
}
