package model;

import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.account.Account;
import controller.Trade;

import java.io.*;
import java.text.DecimalFormat;
import java.util.Date;

public class DataHandler {

    private final BinanceApiRestClient client;
    private final Account account;
    private final SensorArray sensorArray;
    private final DecimalFormat stableFormat = new DecimalFormat("0.00");
    private final Thread arrayThread;

    public DataHandler(BinanceApiRestClient client) {
        this.client = client;
        account = client.getAccount();
        sensorArray = new SensorArray(client, account);
        arrayThread = new Thread(sensorArray);
        arrayThread.start();
        updateConstants();
    }

    public SensorArray getSensorArray() {
        return sensorArray;
    }

    public float getLatestPrice()   {
        return(Float.parseFloat(sensorArray.getLastInstantCandlestick().getClose()));
    }

    public float getUSDTBalance()   {
        return (stableFormat(account.getAssetBalance("USDT").getFree()));
    }

    public float getBTCBalance() {
        return Float.parseFloat(account.getAssetBalance("BTC").getFree());
    }

    public Date getServerTime()  {
        return new Date(Long.parseLong(String.valueOf(client.getServerTime())));
    }

    private float stableFormat(String f) {
        return Float.parseFloat(stableFormat.format(Float.valueOf(f)));
    }

    public BinanceApiRestClient getClient()    {
        return client;
    }

    public void updateConstants()    {
        try {
            File file = new File("constants.dat");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null)  {
                if (line.contains("triggerMomentum"))   {
                    String[] splitter = line.split(":");
                    Constants.triggerMomentum = Float.parseFloat(splitter[1]);
                }
            }
        } catch (Exception e)   {
            e.printStackTrace();
        }
    }
}
