import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import controller.Controller;
import model.Pairwise;
import model.SensorArray;
import view.Interfacer;

import java.io.BufferedReader;
import java.io.FileReader;


public class binanceSpider {



    public static void main(String[] args) throws Exception
    {
        BufferedReader reader = new BufferedReader(
                new FileReader("cred.dat"));
        final String apiKey = reader.readLine();
        final String secret = reader.readLine();
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey, secret);
        BinanceApiRestClient client = factory.newRestClient();
        SensorArray btcSensorArray = new SensorArray(client, 30000, "BTC", "USDT", 0);
        SensorArray ethSensorArray = new SensorArray(client, 30000, "ETH", "USDT", 30000);
        Interfacer interfacer = new Interfacer();
        Controller btcController = new Controller(btcSensorArray, 100);
        Controller ethController = new Controller(ethSensorArray, 100);
        Pairwise btc = new Pairwise(btcController, btcSensorArray);
        Pairwise eth = new Pairwise(ethController, ethSensorArray);
        interfacer.addPairwise(btc);
        interfacer.addPairwise(eth);
        Thread view = new Thread(interfacer);
        view.start();
    }
}
