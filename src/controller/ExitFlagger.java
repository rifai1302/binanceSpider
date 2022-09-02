package controller;

import com.binance.api.client.domain.market.CandlestickInterval;
import controller.pattern.PatternSpotter;
import model.SensorArray;

public class ExitFlagger implements Runnable {

    private final SensorArray sensorArray;
    private final Controller controller;
    private final boolean longExit;
    private PatternSpotter spotter;

    public ExitFlagger(SensorArray sensorArray, Controller controller, boolean longExit) {
        this.sensorArray = sensorArray;
        this.controller = controller;
        this.longExit = longExit;
    }

    @Override
    public void run() {
        boolean flagged = false;
        if (!longExit) {
            spotter = new PatternSpotter(sensorArray);
            Thread thread = new Thread(spotter);
            thread.start();
        }
        while (!flagged) {
            if (longExit) {
                    if ((sensorArray.getMAIncrease(CandlestickInterval.FIVE_MINUTES, 4) < 3)
                            || (sensorArray.getMovingAverage(4) < sensorArray.getMovingAverage(7))
                            || (Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose()) < sensorArray.getMovingAverage(6))) {
                        controller.sellSignal();
                        flagged = true;
                    }
            } else {
                float close = Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose());
                if (sensorArray.getMovingAverage(4) < sensorArray.getMovingAverage(7)) {
                    controller.sellSignal();
                    flagged = true;
                }
                if (spotter.patternFound()) {
                    String[] pattern = spotter.getFoundPatterns();
                    for (String value : pattern) {
                        if (value.contains("InvertedHammer")) {
                            controller.sellSignal();
                            flagged = true;
                        }
                    }
                }
            }

        }
    }
}
