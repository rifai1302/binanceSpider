import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import controller.Controller;
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
        SensorArray sensorArray = new SensorArray(client, 45000);
        Interfacer interfacer = new Interfacer();
        Controller controller = new Controller(sensorArray);
        interfacer.setController(controller);
        interfacer.setSensorArray(sensorArray);
        Thread view = new Thread(interfacer);
        view.start();
    }
}
