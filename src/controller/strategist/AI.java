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

    }

    @Override
    public void run() {
        Interfacer.consolePrint("AI strategist started.");
        while (!Thread.currentThread().isInterrupted()) {
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
                message = "You are a bitcoin day scalping bot. Following arrays represent technical data of the last fifteen minutes in ascending order (first value is the earliest), where each value represents one minute.\nClosing prices of last fifteen minutes: " + lastClose + "\nOpen prices of last fifteen minutes: " + lastOpen + "\nHigh prices of last fifteen minutes: " + lastHigh + "\nLow prices of last fifteen minutes: " + lastLow + "\nTrade volumes of last fifteen minutes: " + lastVolumes + "\nQuote assets of last fifteen minutes: " + lastquote + "\nNumber of trades in the last fifteen minutes: " + lastNumbers + "\nTaker buy base volumes of the last fifteen minutes: " + lastBuyBase + "\nTaker buy quote asset volumes of the last fifteen minutes: " + lastBuyQuote + "\nYour position is currently closed, which means you can hold or buy. Answer with only one word representing your recommended action.";
              } else {
                message = "You are a bitcoin day scalping bot. Following arrays represent technical data of the last fifteen minutes in ascending order (first value is the earliest), where each value represents one minute.\nClosing prices of last fifteen minutes: " + lastClose + "\nOpen prices of last fifteen minutes: " + lastOpen + "\nHigh prices of last fifteen minutes: " + lastHigh + "\nLow prices of last fifteen minutes: " + lastLow + "\nTrade volumes of last fifteen minutes: " + lastVolumes + "\nQuote assets of last fifteen minutes: " + lastquote + "\nNumber of trades in the last fifteen minutes: " + lastNumbers + "\nTaker buy base volumes of the last fifteen minutes: " + lastBuyBase + "\nTaker buy quote asset volumes of the last fifteen minutes: " + lastBuyQuote + "\nYour position is currently open, which means you can hold or sell. Answer with only one word representing your recommended action.";
              }
              System.out.println("\n\n\n\n\n\n\n\n\n" + message);
              
              try {
                OptionsBuilder ooptions = new OptionsBuilder();
                OllamaResult result =
                    api.generate(OllamaModelType.LLAMA2, message, new OptionsBuilder().build());
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
            try {
                Thread.sleep(30000);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        System.out.println("Stopped.");
    }
}