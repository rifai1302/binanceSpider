package view;

import model.SensorArray;
import observable.Observable;
import observable.Observer;

public class InterfacerTable implements Observer {

  private SensorArray array;

  private float currentBalance = 0;
  private float lastProfit = 0;
  private float avgProfit = 0;
  private float totalprofit = 0;
  private int runTime = 0;
  private int trades = 0;
  private byte status = 0;
  private int successRate = 100;

  public void setArray(SensorArray array) {
    this.array = array;
    array.addObserver(this);
  }

  public float getCurrentBalance() {
    return currentBalance;
  }

  public void setCurrentBalance(float currentBalance) {
    this.currentBalance = currentBalance;
  }

  public float getLastProfit() {
    return lastProfit;
  }

  public void setLastProfit(float lastProfit) {
    this.lastProfit = lastProfit;
  }

  public float getAvgProfit() {
    return avgProfit;
  }

  public void setAvgProfit(float avgProfit) {
    this.avgProfit = avgProfit;
  }

  public float getTotalprofit() {
    return totalprofit;
  }

  public void setTotalprofit(float totalprofit) {
    this.totalprofit = totalprofit;
  }

  public int getRunTime() {
    return runTime;
  }

  public void setRunTime(int runTime) {
    this.runTime = runTime;
  }

  public int getTrades() {
    return trades;
  }

  public void setTrades(int trades) {
    this.trades = trades;
  }

  public byte getStatus() {
    return status;
  }

  public void setStatus(byte status) {
    this.status = status;
  }

  public int getSuccessRate() {
    return successRate;
  }

  public void setSuccessRate(int successRate) {
    this.successRate = successRate;
  }

  @Override
  public void observableUpdated() {
    setCurrentBalance(array.getUSDTBalance());

  }
}
