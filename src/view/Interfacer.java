package view;

import controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.SensorArray;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Interfacer extends Application implements Runnable {

  private static SensorArray sensorArray;
  private static Controller controller;
  private volatile Parent root;
  private ChronoStringFormat format;
  private static final ArrayList<String> consoleLog = new ArrayList<>();

  public void setSensorArray(SensorArray sensorArray)  {
    Interfacer.sensorArray = sensorArray;
  }

  public void setController (Controller controller) {
    Interfacer.controller = controller;
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    Platform.setImplicitExit(false);
    URL url = new File("fxml/main.fxml").toURI().toURL();
    root = FXMLLoader.load(url);
    format = new ChronoStringFormat();
    Scene scene = new Scene(root);
    primaryStage.setResizable(false);
    Text currentBalance = (Text) root.lookup("#currentBalanceValue");
    Text lastTrade = (Text) root.lookup("#lastTradeValue");
    Text avgTrade = (Text) root.lookup("#averageTradeValue");
    Text totalProfit = (Text) root.lookup("#totalProfitValue");
    Text uptime = (Text) root.lookup("#uptimeValue");
    Text trades = (Text) root.lookup("#tradesValue");
    Text status = (Text) root.lookup("#currentStatusValue");
    Text sTrades = (Text) root.lookup("#successfulTradesValue");
    Text fTrades = (Text) root.lookup("#failedTradesValue");
    Text success = (Text) root.lookup("#successRateValue");
    Text console = (Text) root.lookup("#consoleOutput");
    LineChart chart = (LineChart) root.lookup("#bitChart");
    NumberAxis xAxis = (NumberAxis) root.lookup("#xAxis");
    NumberAxis yAxis = (NumberAxis) root.lookup("#yAxis");
    xAxis.setAutoRanging(false);
    yAxis.setAutoRanging(false);
    xAxis.setLowerBound(0);
    xAxis.setUpperBound(30);
    yAxis.setTickUnit(35);
    primaryStage.setTitle("Păgangănul de Bitcoaie");
    consolePrint("Consolă inițializată");
    Thread thread = new Thread(() -> {
      while (true) {
      ArrayList<Runnable> updater = new ArrayList<>();
      Runnable update = () -> currentBalance.setText(sensorArray.getUSDTBalance() + " USDT");
      updater.add(update);
      XYChart.Series data = sensorArray.getData();
      if (data != null) {
        int index = Integer.parseInt((data.getData().toString().split(",")[0]).split("\\[")[2]);
        float price = Float.parseFloat(data.getData().get(0).toString().split(",")[1]);
        yAxis.setUpperBound(price + 175);
        yAxis.setLowerBound(price - 175);
        xAxis.setLowerBound(index - 20);
        xAxis.setUpperBound(index + 10);
        update = () -> chart.getData().add(data);
        updater.add(update);
      }
      update = () -> lastTrade.setText(sensorArray.getLastProfit() + " USDT");
      updater.add(update);
      update = () -> uptime.setText(format.format(controller.getUpTime()));
      updater.add(update);
      update = () -> avgTrade.setText(sensorArray.getAverageProfit() + " USDT");
      updater.add(update);
      update = () -> totalProfit.setText(sensorArray.getTotalProfit() + " USDT");
      updater.add(update);
      update = () -> trades.setText(String.valueOf(controller.getTrades()));
      updater.add(update);
      update = () -> sTrades.setText(String.valueOf(sensorArray.getSuccessfulTrades()));
      updater.add(update);
      update = () -> fTrades.setText(String.valueOf(controller.getTrades() - sensorArray.getSuccessfulTrades()));
      updater.add(update);
      String format = consoleFormat();
      if (!console.getText().equals(format)) {
        if (console.getBoundsInLocal().getHeight() > 151) {
          consoleLog.remove(0);
        }
        update = () -> console.setText(format);
        updater.add(update);
      }
      if (controller.getTrades() == 0)
        update = () -> success.setText("0%");
      else
        update = () -> success.setText((sensorArray.getSuccessfulTrades() / controller.getTrades()) * 100 +
                " %");
      updater.add(update);
      if(controller.showUI()) {
        update = () -> {
          primaryStage.setScene(scene);
            primaryStage.show();
        };
        updater.add(update);
      }
      switch (controller.getStatus()) {
        case 0 -> {
          update = () -> status.setText("Oprit");
          updater.add(update);
          update = () -> status.setStyle("-fx-text-fill: red;");
          updater.add(update);
        }
        case 1 -> {
          update = () -> status.setText("Activ");
          updater.add(update);
          update = () -> status.setStyle("-fx-text-fill: blue;");
          updater.add(update);
        }
        case 2 -> {
          update = () -> status.setText("In trade");
          updater.add(update);
          update = () -> status.setStyle("-fx-text-fill: yellow;");
          updater.add(update);
        }
        case 3 -> {
          update = () -> status.setText("Pornire...");
          updater.add(update);
          update = () -> status.setStyle("-fx-text-fill: yellow;");
          updater.add(update);
        }
      }
        try {
          Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
        for (Runnable runnable: updater) {
          Platform.runLater(runnable);
        }
      }
    });
    thread.setDaemon(true);
    thread.start();
    primaryStage.setScene(scene);
    primaryStage.show();
  }

  private String consoleFormat()  {
    StringBuilder s = new StringBuilder();
    for (String string: Interfacer.consoleLog) {
      s.append(string).append("\n");
    }
    return s.toString();
  }

  public static void consolePrint (String string)  {
    consoleLog.add(string);
  }

  @Override
  public void run() {
    launch();
  }

  public void onLogoMouseEntered(MouseEvent event) {
    event.consume();
    Glow glow = new Glow();
    glow.setLevel(1.0);
    ((Node) event.getSource()).setEffect(glow);
  }

  public void onLogoMouseExited(MouseEvent event) {
    event.consume();
    ((Node) event.getSource()).setEffect(null);
    if ((controller.getStatus() == 1) || (controller.getStatus() == 2))  {
      Bloom bloom = new Bloom();
      bloom.setThreshold(0.23);
      ((Node) event.getSource()).setEffect(bloom);
    } else {
      ((Node) event.getSource()).setEffect(null);
    }
  }

  public void onLogoMouseClicked(MouseEvent event)  {
    if (controller.getStatus() == 0) {
      controller.parseCommand("Begin");
      Bloom bloom = new Bloom();
      bloom.setThreshold(0.23);
      ((Node) event.getSource()).setEffect(bloom);
    }
    else if (controller.getStatus() == 1) {
      controller.parseCommand("Stop");
      ((Node) event.getSource()).setEffect(null);
    }
  }

  public void inputKeyPressed(KeyEvent event) {
    if (event.getCode() == KeyCode.ENTER) {
      TextField field = ((TextField) event.getSource());
      consolePrint(field.getText());
      if (!controller.parseCommand(field.getText()))
        consolePrint("Comandă necunoscută");
      field.setText("");
    }
  }


}