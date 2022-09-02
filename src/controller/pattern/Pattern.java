package controller.pattern;

import com.binance.api.client.domain.market.Candlestick;

import java.util.List;

public interface Pattern {

    boolean examine(List<Candlestick> list);

}
