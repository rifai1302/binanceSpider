package model;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import observable.Observer;
import observable.Observable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class SensorArray implements Runnable, Observable {

    private final LocalDateTime startTime = LocalDateTime.now();
    private final BinanceApiRestClient client;
    private final Account account;
    private volatile List<Candlestick> candlesticks;
    private volatile Candlestick lastUpdate;
    private volatile int interval;
    private final DecimalFormat stableFormat = new DecimalFormat("0.00");

    public SensorArray  (BinanceApiRestClient client, Account account, int interval)   {
        this.client = client;
        this.account = account;
        candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
        this.interval = interval;
    }

    public void setInterval(int interval)   {
        this.interval = interval;
    }

    public List<Candlestick> getCandlesticks()  {
        return candlesticks;
    }

    public float getUSDTBalance()   {
        return (stableFormat(account.getAssetBalance("USDT").getFree()));
    }

    private float stableFormat(String f) {
        return Float.parseFloat(stableFormat.format(Float.valueOf(f)));
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

    public void addObserver (Observer observer)   {
        observers.add(observer);
    }

    @Override
    public void run()   {
        LocalDateTime prevTime = LocalDateTime.now();
        lastUpdate = getLastCandlestick();
        while(true) {
            if (ChronoUnit.MILLIS.between(prevTime, LocalDateTime.now()) >= interval) {
                candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
                prevTime = LocalDateTime.now();
                updateObservers();
                lastUpdate = candlesticks.get(0);
            }
        }
    }

    @Override
    public void updateObservers() {
        for (Observer observer : observers) {
            observer.observableUpdated();
        }
    }
}
