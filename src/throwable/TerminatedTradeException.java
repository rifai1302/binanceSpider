package throwable;

public class TerminatedTradeException extends Exception {
  public TerminatedTradeException() {
    super("Attempted to change the state of a terminated trade.");
  }
}
