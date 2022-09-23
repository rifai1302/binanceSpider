package model;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import javafx.scene.chart.XYChart;
import observable.Observer;
import observable.Observable;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SensorArray implements Runnable, Observable {

    private final BinanceApiRestClient client;
    private final Account account;
    private volatile List<Candlestick> candlesticks;
    private volatile Candlestick lastUpdate;
    private volatile int interval;
    private volatile ArrayList<Float> balanceHistory = new ArrayList<>();
    private volatile XYChart.Series chartData;

    public SensorArray  (BinanceApiRestClient client, Account account, int interval)   {
        this.client = client;
        this.account = account;
        candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
        this.interval = interval;
    }

    public XYChart.Series getData()   {
        XYChart.Series returner = chartData;
        chartData = null;
        return returner;
    }

    public float getLastProfit()    {
        try {
            return balanceHistory.get(balanceHistory.size() - 1) - balanceHistory.get(balanceHistory.size() - 2);
        } catch (IndexOutOfBoundsException e)   {
            return 0;
        }
    }

    public float getAverageProfit() {
        float avg = 0;
        for (int i = 1; i < balanceHistory.size(); i++) {
            avg += balanceHistory.get(i) - balanceHistory.get(0);
        }
        avg = avg / balanceHistory.size();
        if (Float.isNaN(avg))
            return 0;
        return avg;
    }

    public float getTotalProfit()   {
        float total = 0;
        for (int i = 1; i < balanceHistory.size(); i++) {
            total += balanceHistory.get(i) - balanceHistory.get(0);
        }
        return total;
    }

    public int getSuccessfulTrades()    {
        int success = 0;
        for (int i = 1; i < balanceHistory.size(); i++) {
            if (balanceHistory.get(i) > balanceHistory.get(i - 1))
                success++;
        }
        return success;
    }

    public void setInterval(int interval)   {
        this.interval = interval;
    }

    public List<Candlestick> getCandlesticks()  {
        return candlesticks;
    }

    public float getUSDTBalance()   {
        return (float) ((float)Math.round(Float.parseFloat(account.getAssetBalance("USDT").getFree()) * 100.0) / 100.0);
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

    @Override
    public void addObserver (Observer observer)   {
        observers.add(observer);
    }

    @Override
    public void run()   {
        LocalDateTime prevTime = LocalDateTime.now();
        int index = 0;
        balanceHistory.add(getUSDTBalance());
        while(!Thread.currentThread().isInterrupted()) {
            if (ChronoUnit.MILLIS.between(prevTime, LocalDateTime.now()) >= interval) {
                candlesticks = client.getCandlestickBars(Constants.getCurrency(), CandlestickInterval.ONE_MINUTE);
                prevTime = LocalDateTime.now();
                updateObservers();
                lastUpdate = candlesticks.get(candlesticks.size() - 1);
                chartData = new XYChart.Series();
                Candlestick last = candlesticks.get(candlesticks.size() - 2);
                chartData.getData().add(new XYChart.Data(index, getMovingAverage(3)));
                index++;
                if ((getUSDTBalance() > 1) && (balanceHistory.get(balanceHistory.size() - 1) != getUSDTBalance()))   {
                    balanceHistory.add(getUSDTBalance());
                }
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
