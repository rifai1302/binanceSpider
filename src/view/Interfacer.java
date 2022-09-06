package view;

import controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import model.SensorArray;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class Interfacer extends Application implements Runnable {

  private Controller controller;
  private InterfacerTable table = new InterfacerTable();

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public InterfacerTable getTable() {
    return table;
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    URL url = new File("fxml/main.fxml").toURI().toURL();
    Parent root = FXMLLoader.load(url);
    Scene scene = new Scene(root);

    Text currentBalance = (Text) root.lookup("#currentBalanceValue");
    Text lastTrade = (Text) root.lookup("#lastTradeValue");
    Text avgTrade = (Text) root.lookup("#averageTradeValue");
    Text totalProfit = (Text) root.lookup("#totalProfitValue");
    Text uptime = (Text) root.lookup("#uptimeValue");
    Text trades = (Text) root.lookup("#tradesValue");
    Text status = (Text) root.lookup("#currentStatusValue");
    Text success = (Text) root.lookup("#successRateValue");

    primaryStage.setTitle("Păgangănul de Bitcoaie");
    Thread thread = new Thread(() -> {
      Runnable updater = () -> currentBalance.setText(String.valueOf(table.getCurrentBalance()));
      while (true) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
        Platform.runLater(updater);
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
}