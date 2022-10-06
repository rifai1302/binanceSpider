package attachable;

import controller.Controller;
import controller.Trade;
import model.SensorArray;

public class ConnectionFailsafe extends Attachable implements Runnable {

  private final Thread arrayThread;
  private final Controller controller;

  public ConnectionFailsafe(Thread arrayThread, Controller controller) {
    this.arrayThread = arrayThread;
    this.controller = controller;
  }

  public void run() {
    boolean bailed = false;
    while (!arrayThread.isAlive()) {
    }
     while (!bailed)  {
      if (!arrayThread.isAlive())
          while (trade.isOpen()) {
            System.out.println("sensorArray thread interrupted. Closing trade...");
            try {
              controller.sellSignal();
            } catch (Exception ignored) {
            }
          }
       bailed = true;
      }
    }
  }
