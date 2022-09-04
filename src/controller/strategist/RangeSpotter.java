package controller.strategist;

import controller.Controller;
import model.SensorArray;
import model.ShiftingArray;
import observable.Observer;
import controller.Status;
import java.util.ArrayList;
import java.util.List;

import static controller.Status.*;

public class RangeSpotter implements Runnable, Observer {

  private final SensorArray array;
  private final Controller controller;
  private volatile boolean arrayUpdated = false;
  private volatile int span = 4;
  private final int tolerance;

  public RangeSpotter(SensorArray array, Controller controller, int tolerance)  {
    this.array = array;
    this.controller = controller;
    this.tolerance = tolerance;
    array.addObserver(this);
  }

  public void setSpan(int span) {
    this.span = span;
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
      if (average < (prev)) {
        bullishVote++;
      } else if (average > (prev))  {
        bearishVote++;
      }
      prev = average;
    }
    if (bearishVote > bullishVote)  {
      return Bearish;
    } else if (bullishVote > bearishVote) {
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
    while (1 == 1) {
      if (arrayUpdated) {
        shifting.add(array.getMovingAverage(10));
        if (shifting.isFilled()) {
          List<Float> averages = shifting.getStandardArray();
          Status status = getStatus(averages);
            if ((status == Bullish) && (averages.get(averages.size() - 1)) > array.getMovingAverage(20))  {
                if (highSwitch == 0) {
                  highSwitch = averages.get(averages.size() - 1);
                  System.out.println("High switch");
                }
                expiration = 0;
                if ((lowSwitch != 0) && (!inRange)) {
                  inRange = true;
                }
                if (inRange)  {
                  controller.sellSignal();
                }
            } else if ((status == Bearish) && (averages.get(averages.size() - 1)) < array.getMovingAverage(20))  {
                if (lowSwitch == 0) {
                  lowSwitch = averages.get(averages.size() - 1);
                  System.out.println("Low switch");
                }
                expiration = 0;
                if((highSwitch != 0) && (!inRange)) {
                  inRange = true;
                }
                if (inRange)  {
                  controller.buySignal();
                }
              }
        }
        arrayUpdated = false;
        expiration++;
        if (expiration >= 5 & ((highSwitch == 0) || (lowSwitch == 0)))  {
          highSwitch = 0;
          lowSwitch = 0;
        }
        if (expiration >= 30) {
          highSwitch = 0;
          lowSwitch = 0;
          inRange = false;
        }
      }
    }
  }

  @Override
  public void observableUpdated() {
    arrayUpdated = true;
  }
}
