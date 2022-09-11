package attachable;

import controller.Trade;

public abstract class Attachable {

  protected final Trade trade;

  public Attachable (Trade trade) {
    this.trade = trade;
  }
}
