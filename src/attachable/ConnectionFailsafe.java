package attachable;

import controller.Trade;
import model.SensorArray;

public class ConnectionFailsafe extends Attachable implements Runnable {

  private final Thread arrayThread;

  public ConnectionFailsafe(Thread arrayThread) {
    this.arrayThread = arrayThread;
  }

  public void run() {
    while (trade.isOpen()) {
      try {
        Thread.sleep(120000);
      } catch (InterruptedException ignored) {
      }
      if (!arrayThread.isAlive())
          while (trade.isOpen()) {
            try {
              trade.close();
            } catch (Exception ignored) {
            }
          }
      }
    }
  }
