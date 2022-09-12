package view;

import controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Bloom;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.DataHandler;
import model.SensorArray;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Interfacer extends Application implements Runnable {

  private static DataHandler dataHandler;
  private static Controller controller;
  private volatile Parent root;
  private ChronoStringFormat format;

  public void setDataHandler(DataHandler dataHandler)  {
    Interfacer.dataHandler = dataHandler;
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
    ImageView logo = (ImageView) root.lookup("#spiderLogo");
    primaryStage.setTitle("Păgangănul de Bitcoaie");
    SensorArray array = dataHandler.getSensorArray();
    Thread thread = new Thread(() -> {
      while (true) {
      ArrayList<Runnable> updater = new ArrayList<>();
      Runnable update = () -> currentBalance.setText(dataHandler.getUSDTBalance() + " USDT");
      updater.add(update);
      update = () -> lastTrade.setText(array.getLastProfit() + " USDT");
      updater.add(update);
      update = () -> uptime.setText(format.format(controller.getUpTime()));
      updater.add(update);
      update = () -> avgTrade.setText(array.getAverageProfit() + " USDT");
      updater.add(update);
      update = () -> totalProfit.setText(array.getTotalProfit() + " USDT");
      updater.add(update);
      update = () -> trades.setText(String.valueOf(controller.getTrades()));
      updater.add(update);
      update = () -> sTrades.setText(String.valueOf(array.getSuccessfulTrades()));
      updater.add(update);
      update = () -> fTrades.setText(String.valueOf(controller.getTrades() - array.getSuccessfulTrades()));
      updater.add(update);
      if (controller.getTrades() == 0)
        update = () -> success.setText("0%");
      else
        update = () -> success.setText(String.valueOf((array.getSuccessfulTrades() / controller.getTrades()) * 100) +
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
          Thread.sleep(500);
        } catch (InterruptedException ex) {
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
    if (controller.getStatus() == 1)  {
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

  }


}