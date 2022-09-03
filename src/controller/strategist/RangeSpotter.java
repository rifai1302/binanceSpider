package controller.strategist;

import controller.Controller;
import model.SensorArray;
import observable.Observer;
import controller.Status;
import java.util.ArrayList;
import java.util.List;

import static controller.Status.*;

public class RangeSpotter implements Runnable, Observer {

  private final SensorArray array;
  private final Controller controller;
  private volatile boolean arrayUpdated = false;
  private volatile int span = 3;
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

  private Status getStatus(List<float> list)  {
    int bullishVote = 0;
    int bearishVote = 0;
    float prev = 0;
    for (Float average: list) {
      if (prev == 0)  {
        prev = average;
        continue;
      }
      if (average < (prev - tolerance)) {
        bullishVote++;
      } else if (average > (prev + tolerance))  {
        bearishVote++;
      }
    }
    if (bearishVote > (bullishVote + 2))  {
      return Bearish;
    } else if (bullishVote > (bearishVote + 2)) {
      return Bullish;
    } else {
      return Inconclusive;
    }
  }

  @Override
  public void run() {
    ArrayList<float> averages = new ArrayList<float>();
    Status status = Inconclusive;
    if (arrayUpdated) {
      averages.add(array.getMovingAverage(span));
      if (averages.size() > 5)  {
        ArrayList<float> highSwitchPoints = new ArrayList<float>();
        ArrayList<float> lowSwitchPoints = new ArrayList<float>();
        float prev = 0;
        for (float average: averages) {
          if (prev == 0)  {
            prev = average;
            continue;
          }
          status = getStatus(averages);
          if ((average < (prev - tolerance)) && (status == Bearish)) {
            highSwitchPoints.add(average);
          } else if ((average > (prev + tolerance)) && (status != Bullish)){
            lowSwitchPoints.add(average);
          }
        }
      }
    }


  }

  @Override
  public void observableUpdated() {
    arrayUpdated = true;
  }
}
