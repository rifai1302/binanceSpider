package controller;

import model.SensorArray;

public class TrailingStop implements Runnable {

    private final SensorArray array;
    private final Trade trade;
    private final Controller controller;


    public TrailingStop (SensorArray array, Controller controller, Trade trade)    {
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
                Thread.sleep(250);
            } catch (Exception e)   {
                e.printStackTrace();
            }
        }

    }
}
