package model;

import controller.Controller;

public class Pairwise {

  private final Controller controller;
  private final SensorArray sensorArray;

  public Pairwise(Controller controller, SensorArray sensorArray) {
    this.controller = controller;
    this.sensorArray = sensorArray;
  }

  public Controller getController() {
    return controller;
  }

  public SensorArray getSensorArray() {
    return sensorArray;
  }
}
