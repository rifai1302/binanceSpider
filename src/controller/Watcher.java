package controller;

import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import controller.pattern.Pattern;
import controller.pattern.PatternSpotter;
import model.Constants;
import model.RecurrentArray;
import model.SensorArray;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Watcher implements Runnable, Observer    {

    private final SensorArray sensorArray;
    private final Controller controller;
    private volatile boolean stopFlag = false;
    private LocalDateTime startTime;
    private volatile boolean arrayUpdated = false;
    private volatile LocalDateTime pauseTime;
    private Thread flaggerThread;

    public Watcher (SensorArray sensorArray, Controller controller)    {
        this.sensorArray = sensorArray;
        sensorArray.addObserver(this);
        this.controller = controller;
    }

    public void stop()  {
        stopFlag = true;
    }

    public Integer getRunningTime()  {
        return (int) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }

    public void pause()  {
        pauseTime = LocalDateTime.now();
    }

    @Override
    public void run()   {
        stopFlag = false;
        startTime = LocalDateTime.now();
        arrayUpdated = false;
        PatternSpotter spotter = new PatternSpotter(sensorArray);
        Thread spotterThread = new Thread(spotter);
        spotterThread.start();
        while (!stopFlag)   {
            if (pauseTime != null && (ChronoUnit.MINUTES.between(pauseTime, LocalDateTime.now()) < 4))
                continue;
            if (arrayUpdated)    {
                List<Candlestick> candlesticks = sensorArray.getCandlesticks();
              RecurrentArray array = new RecurrentArray(4);
              array.fillWithLast(candlesticks);
                final float close = Float.parseFloat(array.get(3).getClose());
              /* if (sensorArray.getMovingAverage(7) > sensorArray.getMovingAverage(99))
                    if (sensorArray.getMovingAverage(4) > sensorArray.getMovingAverage(7))
                        if (sensorArray.getMomentum(4) > Constants.triggerMomentum)
                            if  (flaggerThread == null || !flaggerThread.isAlive()) {
                            controller.buySignal();
                            ExitFlagger flagger = new ExitFlagger(sensorArray, controller, false);
                            flaggerThread = new Thread(flagger);
                            flaggerThread.start();
                                System.out.println("Short");
                        } */
                    if ((sensorArray.getMAIncrease(CandlestickInterval.FIVE_MINUTES, 4) > 11.5)
                            && (sensorArray.getMAIncrease(CandlestickInterval.ONE_MINUTE, 99) > 3)
                            && (sensorArray.distanceBetweenMA(15, 99) > 20))
                        if (flaggerThread == null || !flaggerThread.isAlive())   {
                            controller.buySignal();
                            ExitFlagger flagger = new ExitFlagger(sensorArray, controller, true);
                            flaggerThread = new Thread(flagger);
                            flaggerThread.start();
                            System.out.println("Long");
                        }

              arrayUpdated = false;
            }
        }
    }

    @Override
    public void observableUpdated() {
        arrayUpdated = true;
    }
}
