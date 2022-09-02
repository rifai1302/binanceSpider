package throwable;

public class OngoingTradeException extends Exception {
  public OngoingTradeException() {
    super("Attempted to get terminal statistics of an ongoing trade.");
  }
}
