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
    private final DecimalFormat format = new DecimalFormat("#.#####");

    public Trade(DataHandler dataHandler, float usd) {
        this.usd = usd;
        this.dataHandler = dataHandler;
        this.client = dataHandler.getClient();
        format.setRoundingMode(RoundingMode.FLOOR);
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
        client.newOrder(marketBuy(Constants.getCurrency(), format.format(quantity)));
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
        float temp = Float.parseFloat(client.getAccount().getAssetBalance(Constants.crypto).getFree());
        client.newOrder(marketSell(Constants.getCurrency(), format.format(temp)));
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

}

