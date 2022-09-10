package controller.strategist;

import controller.Controller;
import model.SensorArray;
import model.ShiftingArray;
import observable.Observer;
import controller.Status;
import java.awt.*;
import java.util.List;

import static controller.Status.*;

public class RangeSpotter implements Runnable, Observer {

  private final SensorArray array;
  private final Controller controller;
  private volatile boolean arrayUpdated = false;
  private final int maRange;

  public RangeSpotter(SensorArray array, Controller controller, int maRange)  {
    this.array = array;
    this.controller = controller;
    this.maRange = maRange;
    array.addObserver(this);
  }

  private Status getStatus(List<Float> list)  {
    int bullishVote = 0;
    int bearishVote = 0;
    float prev = 0;
    for (Float average: list) {
      if (prev == 0)  {
        prev = average;
        continue;
      }
      if (average < prev) {
        bullishVote++;
      } else if (average > prev)  {
        bearishVote++;
      }
      prev = average;
    }
    if (bearishVote > list.size() / 2)  {
      return Bearish;
    } else if (bullishVote > list.size() / 2) {
      return Bullish;
    } else {
      return Inconclusive;
    }
  }

  @Override
  public void run() {
    ShiftingArray<Float> shifting = new ShiftingArray<>(5);
    byte expiration = 0;
    boolean inRange = false;
    float highSwitch = 0;
    float lowSwitch = 0;
    while (!Thread.currentThread().isInterrupted()) {
      if (arrayUpdated) {
        shifting.add(array.getMovingAverage(maRange));
        if (shifting.isFilled()) {
          List<Float> averages = shifting.getStandardArray();
          Status status = getStatus(averages);
            if ((status == Bullish) && (averages.get(averages.size() - 1) > array.getMovingAverage(20)))  {
              if ((highSwitch == 0) || ((averages.get(averages.size() - 1)) > highSwitch)) {
                highSwitch = averages.get(averages.size() - 1);
                System.out.println("High switch");
                Toolkit.getDefaultToolkit().beep();
              }
              expiration = 0;
                if ((lowSwitch != 0) && (!inRange)) {
                  inRange = true;
                }
                if (inRange)  {
                  controller.sellSignal();
                }
            } else if ((status == Bearish) && (averages.get(averages.size() - 1)) < array.getMovingAverage(20))  {
              if ((lowSwitch == 0) || ((averages.get(averages.size() - 1)) < lowSwitch)) {
                lowSwitch = averages.get(averages.size() - 1);
                System.out.println("Low switch");
                Toolkit.getDefaultToolkit().beep();
              }
              expiration = 0;
                if((highSwitch != 0) && (!inRange)) {
                  inRange = true;
                }
                if (inRange)  {
                  if ((highSwitch - lowSwitch < 50) || (((highSwitch - averages.get(averages.size() - 1)) < 25)
                          || ((averages.get(averages.size() - 1) - lowSwitch) < 25)))  {
                    inRange = false;
                    shifting = new ShiftingArray<>(5);
                    highSwitch = 0;
                    lowSwitch = 0;
                  } else {
                    controller.buySignal();
                  }
                }
              }
        }
        arrayUpdated = false;
        if ((highSwitch != 0) || (lowSwitch != 0))
        expiration++;

        if (expiration >= 60) {
          inRange = false;
          shifting = new ShiftingArray<>(5);
          highSwitch = 0;
          lowSwitch = 0;
          expiration = 0;
        }
      }
    }
    System.out.println("Stopped.");
  }

  @Override
  public void observableUpdated() {
    arrayUpdated = true;
  }

}
