package controller.strategist;

import controller.Controller;
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import io.github.amithkoujalgi.ollama4j.core.models.OllamaResult;
import io.github.amithkoujalgi.ollama4j.core.types.OllamaModelType;
import io.github.amithkoujalgi.ollama4j.core.utils.OptionsBuilder;
import model.SensorArray;
import observable.Observer;
import view.Interfacer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AI implements Runnable, Observer {

    private final SensorArray array;
    private final Controller controller;
    private final OllamaAPI api;
    private volatile boolean arrayUpdated = false;

    public AI(SensorArray array, Controller controller) throws IOException, URISyntaxException {
        this.array = array;
        this.controller = controller;
        array.addObserver(this);
        String host = "http://127.0.0.1:11434/";
        api = new OllamaAPI(host);
        api.setRequestTimeoutSeconds(30000);

        
}

    @Override
    public void observableUpdated() {
        arrayUpdated = true;
    }

    @Override
    public void run() {
        Interfacer.consolePrint("AI strategist started.");
        while (!Thread.currentThread().isInterrupted()) {
            if (arrayUpdated){
              List<Float> lastClose = array.getLastFifteenClose();
              List<Float> lastOpen = array.getLastFifteenOpen();
              List<Float> lastHigh = array.getLastFifteenHigh();
              List<Float> lastLow = array.getLastFifteenLow();
              List<Float> lastVolumes = array.getLastFifteenVolume();
              List<Float> lastquote = array.getLastFifteenQuoteAsset();
              List<Long> lastNumbers = array.getLastFifteenTrades();
              List<Float> lastBuyBase = array.getLastFifteenBuyBase();
              List<Float> lastBuyQuote = array.getLastFifteenBuyQuote();
              String message = "";
              if (controller.getStatus() == 1) {
                message = (
                    "You are a bitcoin day scalping bot. The following arrays represent the technical data of bitcoin's price history over the last few minutes, where the first value is the latest, and each value represents one minute.\n"
                     + "Closing prices of the last 15 minutes: " + lastClose + "\n"
                     + "High prices of the last 15 minutes: " + lastHigh + "\n"
                     + "Low prices of the last 15 minutes: " + lastLow + "\n"
                     + "Trade volumes of the last 15 minutes: " + lastVolumes + "\n"
                     + "Quote assets of the last 15 minutes: " + lastquote + "\n"
                     + "Number of trades in the last 15 minutes: " + lastNumbers + "\n"
                     + "Taker buy base volumes of the last 15 minutes: " + lastBuyBase + "\n"
                     + "Taker buy quote asset volumes of the last 15 minutes: " + lastBuyQuote + "\n"
                     + "Based on this data, you need to predict bitcoin's price movements and decide whether to hold or buy. Your current position is closed, meaning you can either hold or buy. Respond with only one word: HOLD or BUY."
                );
            } else {
                message = (
                    "You are a bitcoin day scalping bot. The following arrays represent the technical data of bitcoin's price history over the last few hours, where the first value is the latest, and each value represents one minute.\n"
                     + "Closing prices of the last 15 minutes: " + lastClose + "\n"
                     + "High prices of the last 15 minutes: " + lastHigh + "\n"
                     + "Low prices of the last 15 minutes: " + lastLow + "\n"
                     + "Trade volumes of the last 15 minutes: " + lastVolumes + "\n"
                     + "Quote assets of the last 15 minutes: " + lastquote + "\n"
                     + "Number of trades in the last 15 minutes: " + lastNumbers + "\n"
                     + "Taker buy base volumes of the last 15 minutes: " + lastBuyBase + "\n"
                     + "Taker buy quote asset volumes of the last 15 minutes: " + lastBuyQuote + "\n"
                     + "Based on this data, you need to predict bitcoin's price movements and decide whether to hold or buy. Your current position is open, meaning you can either hold or sell. You have bought bitcoin when its price was " + controller.getLastOpenPrice() + ". " + "You need to secure short-term profits, so if the price gives any sign of dropping, sell. Respond with only one word: HOLD or SELL."
                );}
              System.out.println("\n\n\n\n\n\n\n\n\n" + message);
              
              try {
                OptionsBuilder ooptions = new OptionsBuilder();
                OllamaResult result =
                    api.generate("nous-hermes2:34b", message, new OptionsBuilder().build());
                Interfacer.consolePrint("Message from AI pipeline: " + result.getResponse().toLowerCase());
                if(result.getResponse().toLowerCase().equals("buy")){
                    controller.buySignal();
                } else if (result.getResponse().toLowerCase().equals("sell")) {
                    controller.sellSignal();
                }
            } catch (OllamaBaseException | IOException | InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            arrayUpdated = false;
        }
        }
        System.out.println("Stopped.");
    }
}