package controller;

import com.binance.api.client.domain.market.Candlestick;
import model.SensorArray;
import observable.Observer;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class Watcher implements Runnable, Observer {

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
        while (!stopFlag)   {
            if (pauseTime != null && (ChronoUnit.MINUTES.between(pauseTime, LocalDateTime.now()) < 4))
                continue;
            if (arrayUpdated)    {
            }
        }
    }

    @Override
    public void observableUpdated() {
        arrayUpdated = true;
    }
}
