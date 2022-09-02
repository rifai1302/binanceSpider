package throwable;

public class UninitializedTradeException extends Exception {
  public UninitializedTradeException() {
    super("Attempted to get terminal statistics of an uninitialized trade.");
  }
}
