package model;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import javafx.scene.chart.XYChart;
import observable.Observer;
import observable.Observable;
import view.Interfacer;

import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class SensorArray implements Runnable, Observable {

    private final BinanceApiRestClient client;
    private volatile List<Candlestick> candlesticks;
    private volatile Candlestick lastUpdate;
    private volatile int interval;
    private volatile ArrayList<Float> balanceHistory = new ArrayList<>();
    private volatile XYChart.Series chartData;
    private final String crypto;
    private final String stable;
    private final int startDelay;

    public SensorArray  (BinanceApiRestClient client, int interval, String crypto, String stable, int startDelay)   {
        this.client = client;
        candlesticks = client.getCandlestickBars(crypto + stable, CandlestickInterval.ONE_MINUTE);
        this.interval = interval;
        this.crypto = crypto;
        this.stable = stable;
        balanceHistory.add(Float.parseFloat(client.getAccount().getAssetBalance(stable).getFree()));
        this.startDelay = startDelay;
    }

    public XYChart.Series getData()   {
        XYChart.Series returner = chartData;
        chartData = null;
        return returner;
    }

    public float getLastProfit()    {
        try {
            return (float) (Math.round
                                ((balanceHistory.get(balanceHistory.size() - 1) -
                                        balanceHistory.get(balanceHistory.size() - 2)) * 100.0) / 100.0);
        } catch (IndexOutOfBoundsException e)   {
            return 0;
        }
    }

    public float getAverageProfit() {
        float avg = 0;
        for (int i = 1; i < balanceHistory.size(); i++) {
            avg += balanceHistory.get(i) - balanceHistory.get(i - 1);
        }
        avg = avg / balanceHistory.size();
        if (Float.isNaN(avg))
            return 0;
        return (float) ((float) Math.round(avg * 100.0) / 100.0);
    }

    public float getTotalProfit()   {
        float total = 0;
        for (int i = 1; i < balanceHistory.size(); i++) {
            total += balanceHistory.get(i) - balanceHistory.get(i - 1);
        }
        return (float) ((float) Math.round(total * 100.0) / 100.0);
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

    public float getStableBalance()   {
        try {
            return (float) ((float) Math.round(balanceHistory.get(balanceHistory.size() - 1) * 100.0) / 100.0 - 0.01);
        } catch (Exception ignored) {
            return 0;
        }
    }

    public String getCryptoBalance() {
        return client.getAccount()
                .getAssetBalance(crypto).getFree();
    }

    public Candlestick getLastCandlestick() {
        return candlesticks.get(candlesticks.size() - 1);
    }

    public BinanceApiRestClient getClient() {
        return client;
    }

    public float getLatestPrice()   {
        return(Float.parseFloat(getLastInstantCandlestick().getClose()));
    }

    public Candlestick getLastInstantCandlestick()  {
        candlesticks = client.getCandlestickBars(crypto + stable, CandlestickInterval.ONE_MINUTE);
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

    public String getStableCoin()   {
        return stable;
    }

    public String getCryptoCoin()   {
        return crypto;
    }

    @Override
    public void addObserver (Observer observer)   {
        observers.add(observer);
    }

    @Override
    public void run()   {
        try {
            Thread.sleep(startDelay);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalDateTime prevTime = LocalDateTime.now();
        int index = 0;
        while(!Thread.currentThread().isInterrupted()) {
            if (ChronoUnit.MILLIS.between(prevTime, LocalDateTime.now()) >= interval) {
                try {
                    candlesticks = client.getCandlestickBars(crypto + stable, CandlestickInterval.ONE_MINUTE);
                    prevTime = LocalDateTime.now();
                    updateObservers();
                    lastUpdate = candlesticks.get(candlesticks.size() - 1);
                    chartData = new XYChart.Series();
                    Candlestick last = candlesticks.get(candlesticks.size() - 2);
                    chartData.getData().add(new XYChart.Data(index, getMovingAverage(3)));
                    index++;
                    float balance = (Float.parseFloat(client.getAccount().getAssetBalance(stable).getFree()));
                    if ((balance > 10) && (balanceHistory.get(balanceHistory.size() - 1) != balance)) {
                        balanceHistory.add(balance);
                    }
                } catch (Exception s)  {
                    Interfacer.consolePrint("Sensor array failure; no internet connection.");
                    Interfacer.consolePrint("Exception caught.");
                    s.printStackTrace();
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
