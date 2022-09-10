package controller.strategist;

import controller.Controller;
import controller.Trade;
import model.SensorArray;

public class TrailingStopLoss implements Runnable {

    private final SensorArray array;
    private final Trade trade;
    private final Controller controller;


    public TrailingStopLoss(SensorArray array, Controller controller, Trade trade)    {
        this.array = array;
        this.controller = controller;
        this.trade = trade;
    }

    @Override
    public void run()   {
        float highest = 0;
        while(trade.isOpen())   {
            float close = Float.parseFloat(array.getLastInstantCandlestick().getClose());
            if (close > highest)
                highest = close;
            float boundary = (float) (highest - (0.15 * highest) / 100);
            if (Float.parseFloat(array.getLastInstantCandlestick().getClose()) < boundary)   {
                try {
                    trade.close();
                    controller.tradeClosed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(1000);
            } catch (Exception e)   {
                e.printStackTrace();
            }
        }

    }
}
