package controller;

import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.binance.api.client.BinanceApiRestClient;
import throwable.InsufficientFundsException;
import model.Constants;
import model.DataHandler;
import throwable.OngoingTradeException;
import throwable.TerminatedTradeException;
import throwable.UninitializedTradeException;
import java.text.DecimalFormat;
import static com.binance.api.client.domain.account.NewOrder.marketBuy;
import static com.binance.api.client.domain.account.NewOrder.marketSell;

public class Trade {

    private final DataHandler dataHandler;
    private final BinanceApiRestClient client;
    private final float usd;
    private boolean open = false;
    private boolean terminated = false;
    private float openPrice = 0;
    private float endPrice = 0;
    private LocalDateTime openTime;
    private float secondsOpen = 0;

    public Trade(DataHandler dataHandler, float usd) {
        this.usd = usd;
        this.dataHandler = dataHandler;
        this.client = dataHandler.getClient();
    }

    public void open() throws Exception {
        if (usd > dataHandler.getUSDTBalance()) {
            throw new InsufficientFundsException();
        }
        if (open) {
            throw new OngoingTradeException();
        }
        if (terminated) {
            throw new TerminatedTradeException();
        }
        float quantity = (usd - (float) 0.5) / dataHandler.getLatestPrice();
        openPrice = dataHandler.getLatestPrice();
        quantity = (float)((float)Math.round(quantity * 10000.0) / 10000.0);
        quantity = (float) (quantity - 0.0001);
        client.newOrder(marketBuy(Constants.getCurrency(), String.valueOf(quantity)));
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
        String temp = btcFormat(client.getAccount().getAssetBalance("BTC").getFree());
        System.out.println(temp);
        client.newOrder(marketSell(Constants.getCurrency(), String.valueOf(temp)));
        endPrice = dataHandler.getLatestPrice();
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

    public String btcFormat(String value)    {
        System.out.println("btcFormat input: " + value);
        String[] splitter = value.split("\\.");
        String decimal = splitter[1];
        System.out.println("splitter[0]: " + splitter[0]);
        System.out.println("splitter[1]: " + splitter[1]);
        if (decimal.length() > 5)
            decimal = decimal.substring(0, 5);
        System.out.println("decimal: " + decimal);
        return splitter[0] + decimal;
    }

}

