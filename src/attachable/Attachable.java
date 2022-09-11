package attachable;

import controller.Trade;

public abstract class Attachable implements Runnable {

  protected Trade trade;

  public void attachToTrade(Trade trade)  {
    this.trade = trade;
  }

}
