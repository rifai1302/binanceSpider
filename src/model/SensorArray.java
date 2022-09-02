package model;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import controller.Observer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SensorArray implements Runnable, Observable {

    private final BinanceApiRestClient client;
    private final Account account;
    private volatile List<Candlestick> candlesticks;
    private volatile Candlestick lastUpdate;
    private ArrayList<Observer> observerArray = new ArrayList<>();

    public SensorArray  (BinanceApiRestClient client, Account account)   {
        this.client = client;
        this.account = account;
        candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
    }

    public List<Candlestick> getCandlesticks()  {
        return candlesticks;
    }

    public Candlestick getLastCandlestick() {
        return candlesticks.get(candlesticks.size() - 1);
    }

    public Candlestick getLastInstantCandlestick()  {
        candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
        return candlesticks.get(candlesticks.size() - 1);
    }

    public float getMovingAverage(int units)    {
        float ma = 0;
        for (int i = candlesticks.size() - 1; i > candlesticks.size() - 1 - units; i--) {
            ma += Float.parseFloat(candlesticks.get(i).getClose());
        }
        ma = ma / units;
        return ma;
    }

    public float distanceBetweenMA(int MA1, int MA2) {
        final float close = Float.parseFloat(getLastInstantCandlestick().getClose());
        if (getMovingAverage(MA1) > getMovingAverage(MA2))
            return(getMovingAverage(MA1) - getMovingAverage(MA2));
        return(getMovingAverage(MA2) - getMovingAverage(MA1));
    }

    public float getMAIncrease(CandlestickInterval interval, int iter)    {
        List<Candlestick> candlesticks = client.getCandlestickBars(Constants.getCurrency(), interval);
        float avg = 0;
        for (int i = candlesticks.size() - 2; i > candlesticks.size() - iter; i--) {
            avg += Float.parseFloat(candlesticks.get(i + 1).getClose()) - Float.parseFloat(candlesticks.get(i).getClose());
        }
        avg = avg / iter - 1;
        return avg;
    }

    public void addObserver (Observer observer)   {
        observerArray.add(observer);
    }

    @Override
    public void run()   {
        LocalDateTime prevTime = LocalDateTime.now();
        lastUpdate = getLastCandlestick();
        while(true) {
            if (ChronoUnit.SECONDS.between(prevTime, LocalDateTime.now()) >= 10) {
                candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
                prevTime = LocalDateTime.now();
                updateObservers();
                lastUpdate = candlesticks.get(0);
            }
        }
    }

    @Override
    public void updateObservers() {
        for (Observer observer : observerArray) {
            observer.observableUpdated();
        }
    }
}
