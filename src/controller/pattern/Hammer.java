package controller.pattern;

import com.binance.api.client.domain.market.Candlestick;

import java.util.List;

public class Hammer implements Pattern {

    @Override
    public boolean examine(List<Candlestick> list) {
        Candlestick last = list.get(list.size()-2);
        final float close = Float.parseFloat(last.getClose());
        final float open = Float.parseFloat(last.getOpen());
        final float low = Float.parseFloat(last.getLow());
        if (close > open)
        return (close > open) && ((open - low) > 3 * (close - open));
        return ((close < open) && ((open - low) > 3 * (open - close)));
    }
}