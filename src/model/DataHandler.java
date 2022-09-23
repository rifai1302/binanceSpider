package model;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import controller.Trade;
import javafx.scene.chart.XYChart;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Date;

public class DataHandler {

    private final BinanceApiRestClient client;
    private final Account account;
    private final SensorArray sensorArray;
    private final Thread arrayThread;

    public DataHandler(BinanceApiRestClient client) {
        this.client = client;
        account = client.getAccount();
        sensorArray = new SensorArray(client, account, 45000);
        arrayThread = new Thread(sensorArray);
        arrayThread.start();
    }

    public SensorArray getSensorArray() {
        return sensorArray;
    }

    public float getLatestPrice()   {
        return(Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose()));
    }

    public float getUSDTBalance()   {
        return (float) ((float)Math.round(Float.parseFloat(account.getAssetBalance("USDT").getFree()) * 100.0) / 100.0 - 0.01);
    }

    public float getBTCBalance() {
        return Float.parseFloat(account.getAssetBalance("BTC").getFree());
    }

    public Date getServerTime()  {
        return new Date(Long.parseLong(String.valueOf(client.getServerTime())));
    }

    public BinanceApiRestClient getClient()    {
        return client;
    }
}
