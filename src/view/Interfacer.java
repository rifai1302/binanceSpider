package view;

import controller.Controller;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.effect.*;
import javafx.scene.Node.*;

import javax.swing.text.html.ImageView;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class Interfacer extends Application implements Runnable {

  private Controller controller;
  private InterfacerTable table = new InterfacerTable();
  private volatile Parent root;

  public void setController(Controller controller) {
    this.controller = controller;
  }

  public InterfacerTable getTable() {
    return table;
  }

  @Override
  public void start(Stage primaryStage) throws IOException {
    URL url = new File("fxml/main.fxml").toURI().toURL();
    root = FXMLLoader.load(url);
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
      ArrayList<Runnable> updater = new ArrayList<>();
      Runnable update = () -> currentBalance.setText(String.valueOf(table.getCurrentBalance()));
      updater.add(update);
      update = () -> uptime.setText(String.valueOf(table.getRunTime()) + " s");
      updater.add(update);
      while (true) {
        try {
          Thread.sleep(500);
        } catch (InterruptedException ex) {
          ex.printStackTrace();
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
    event.consume();
  }

  public void onLogoMouseExited(MouseEvent event) {
    event.consume();
    Bloom bloom = new Bloom();
    bloom.setThreshold(table.getBloom());
    ((Node) event.getSource()).setEffect(bloom);
  }

  public void onLogoMouseClicked(MouseEvent event) {
    event.consume();
    ((Node) event.getSource()).setEffect(null);
  }
}