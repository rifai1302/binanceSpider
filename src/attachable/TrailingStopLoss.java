package attachable;

import controller.Controller;
import model.SensorArray;

public class TrailingStopLoss extends Attachable implements Runnable {

    private final SensorArray array;
    private final Controller controller;


    public TrailingStopLoss(SensorArray array, Controller controller)    {
        this.array = array;
        this.controller = controller;
    }

    @Override
    public void run()   {
        float highest = 0;
        try {
            Thread.sleep(20000);
        } catch (Exception ignored) {

        }
        while(trade.isOpen())   {
            float close = Float.parseFloat(array.getLastInstantCandlestick().getClose());
            if (close > highest)
                highest = close;
            float boundary = (float) (highest - 100.0);
            if (close < boundary)   {
                try {
                    controller.sellSignal();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(10000);
            } catch (Exception e)   {
                e.printStackTrace();
            }
        }

    }
}
