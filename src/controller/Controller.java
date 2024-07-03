package controller;

import attachable.Attachable;
import attachable.AverageStopLoss;
import attachable.ConnectionFailsafe;
import attachable.TrailingStopLoss;
import controller.commands.Command;
import controller.strategist.AI;
import controller.strategist.RangeSpotter;
import model.SensorArray;
import throwable.UninitializedTradeException;
import view.Interfacer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import javax.websocket.DeploymentException;

public class Controller {

    private final SensorArray sensorArray;
    private final Thread arrayThread;
    private Trade trade;
    private int status = 0;
    private LocalDateTime startTime;
    private int trades = 0;
    private volatile int percentage;
    private boolean showUI = false;
    private volatile ArrayList<Runnable> strategists = new ArrayList<>();
    private volatile ArrayList<Thread> threads = new ArrayList<>();
    private volatile ArrayList<Attachable> attachables = new ArrayList<>();
    private String lastBuyPrice;

    public Controller (SensorArray sensorArray, int percentage, boolean enableTray) throws DeploymentException, IOException, URISyntaxException {
        this.sensorArray = sensorArray;
        this.percentage = percentage;
        arrayThread = new Thread(sensorArray);
        TrayIcon trayIcon = null;
        if (enableTray) {
            File iconFile = new File("fxml/trayicon.png");
            Image icon = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
            PopupMenu popup = new PopupMenu();
            MenuItem item = new MenuItem();
            item.setLabel("Inchide");
            popup.add(item);
            trayIcon = new TrayIcon(icon, "The Binance Spider", popup);
        }
        //addStrategist(new RangeSpotter(sensorArray, this, 5));
        addStrategist(new AI(sensorArray, this));
        //addAttachable(new AverageStopLoss(sensorArray, this));
        addAttachable(new TrailingStopLoss(sensorArray, this));
        if (trayIcon != null)
        trayIcon.addActionListener(e -> {
            showUI = true;
            try {
                Thread.sleep(550);
            } catch (Exception ignored)   {
            }
            showUI = false;
        });
        if (trayIcon != null)
        try {
            SystemTray tray = SystemTray.getSystemTray();
            tray.add(trayIcon);
        } catch (Exception e)   {
            e.printStackTrace();
        }

    }

    public boolean parseCommand(String command) {
        try {
            ClassLoader loader = Command.class.getClassLoader();
            Class<?> driver = Class.forName("controller.commands." + command, true, loader);
            Constructor<?> commandConstructor = driver.getConstructor();
            Command com = (Command)  commandConstructor.newInstance();
            com.execute(this);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void printCrypto()  {
        Interfacer.consolePrint(String.valueOf(sensorArray.getCryptoBalance()));
    }

    public void addStrategist(Runnable strategist)   {
        strategists.add(strategist);
    }

    public void addAttachable(Attachable attachable)    {
        attachables.add(attachable);
    }

    public boolean showUI() {
        return showUI;
    }

    public void start() {
        arrayThread.start();
        for (Runnable runnable: strategists)    {
            Thread thread = new Thread(runnable);
            threads.add(thread);
            thread.start();
        }
            status = 3;
            Toolkit.getDefaultToolkit().beep();
            status = 1;
            startTime = LocalDateTime.now();
    }

    public void stop() {
        for (Thread thread: threads)    {
            thread.interrupt();
        }
        arrayThread.interrupt();
        status = 0;
        startTime = null;
    }

    public int getUpTime()  {
        if (startTime == null)
            return 0;
        return (int) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }

    public void buySignal() {
        if (trade == null) {
            Toolkit.getDefaultToolkit().beep();
            float price = ((float) percentage / 100) * sensorArray.getStableBalance();
            trade = new Trade(sensorArray, price);
            try {
                trade.open();
                status = 2;
                for (Attachable attachable: attachables)    {
                    attachable.attachToTrade(trade);
                    Thread thread = new Thread(attachable);
                    thread.start();
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Interfacer.consolePrint("Open signal.");
        }
    }

    public void sellSignal()    {
        if(trade != null)   {
            try {
                trade.close();
                tradeClosed();
                trades++;
                status = 1;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Interfacer.consolePrint("Close signal.");
    }

    public void tradeClosed()   {
        trade = null;
    }

    public float getLastOpenPrice() {
        try {
            return trade.getOpenPrice();
        } catch (UninitializedTradeException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getStatus() {
        return status;
    }

    public int getTrades()  {
        return trades;
    }
}