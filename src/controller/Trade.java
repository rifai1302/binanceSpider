package controller;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.binance.api.client.BinanceApiRestClient;
import model.SensorArray;
import throwable.InsufficientFundsException;
import throwable.OngoingTradeException;
import throwable.TerminatedTradeException;
import throwable.UninitializedTradeException;

import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static com.binance.api.client.domain.account.NewOrder.marketSell;

public class Trade {

    private final SensorArray sensorArray;
    private final BinanceApiRestClient client;
    private final float usd;
    private boolean open = false;
    private boolean terminated = false;
    private float openPrice = 0;
    private float endPrice = 0;
    private LocalDateTime openTime;
    private float secondsOpen = 0;
    private final String currency;
    private final NumberFormat formatter = new DecimalFormat("0.0000");

    public Trade(SensorArray sensorArray, float usd) {
        this.usd = usd;
        this.sensorArray = sensorArray;
        this.client = sensorArray.getClient();
        this.currency = sensorArray.getCryptoCoin() + sensorArray.getStableCoin();
    }

    public void open() throws Exception {
        if (usd > sensorArray.getStableBalance()) {
            throw new InsufficientFundsException();
        }
        if (open) {
            throw new OngoingTradeException();
        }
        if (terminated) {
            throw new TerminatedTradeException();
        }
        float quantity = usd / sensorArray.getLatestPrice();
        quantity -= 0.0001;
        openPrice = sensorArray.getLatestPrice();
        System.out.println(formatter.format(quantity));
        client.newOrder(marketBuy(currency, formatter.format(quantity).replace(",", ".")));
        open = true;
        openTime = LocalDateTime.now();
    }

    public void close() throws Exception {
        if (!open) {
            throw new UninitializedTradeException();
        }
        if (terminated) {
            throw new TerminatedTradeException();
        }
        String temp = cryptoFormat(client.getAccount().getAssetBalance(sensorArray.getCryptoCoin()).getFree());
        client.newOrder(marketSell(currency, temp));
        endPrice = sensorArray.getLatestPrice();
        open = false;
        terminated = true;
        secondsOpen = ChronoUnit.SECONDS.between(openTime, LocalDateTime.now());
    }

    public boolean isOpen() {
        return open;
    }

    public float getOpenPrice() throws UninitializedTradeException {
        if (!open && !terminated)
            throw new UninitializedTradeException();
        return openPrice;
    }

    public float getEndPrice() throws OngoingTradeException {
        if (open || !terminated)
            throw new OngoingTradeException();
        return endPrice;
    }

    public float getSecondsOpen() throws Exception {
        if (!open && !terminated)
            throw new UninitializedTradeException();
        if (open) {
            return ChronoUnit.SECONDS.between(openTime, LocalDateTime.now());
        }
        return secondsOpen;
    }

    public LocalDateTime getOpenTime() throws Exception {
        if (!open && !terminated)
            throw new UninitializedTradeException();
        return openTime;
    }

    private String cryptoFormat(String value)    {
        String[] splitter = value.split("\\.");
        String decimal = splitter[1];
        if (decimal.length() > 5)
            decimal = decimal.substring(0, 5);
        return splitter[0] + "." + decimal;
    }

}

