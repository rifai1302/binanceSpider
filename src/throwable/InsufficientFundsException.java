package throwable;

public class InsufficientFundsException extends Exception {
  public InsufficientFundsException() {
    super("Attempted to get perform trade with insufficient funds.");
  }
}
