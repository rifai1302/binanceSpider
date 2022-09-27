package controller;

import attachable.Attachable;
import attachable.AverageStopLoss;
import attachable.TrailingStopLoss;
import controller.commands.Command;
import controller.strategist.RangeSpotter;
import model.SensorArray;
import view.Interfacer;

import java.awt.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Controller {

    private final SensorArray sensorArray;
    private Trade trade;
    private int status = 0;
    private LocalDateTime startTime;
    private int trades = 0;
    private boolean showUI = false;
    private volatile ArrayList<Runnable> strategists = new ArrayList<>();
    private volatile ArrayList<Thread> threads = new ArrayList<>();
    private volatile ArrayList<Attachable> attachables = new ArrayList<>();

    public Controller (SensorArray sensorArray) {
        this.sensorArray = sensorArray;
        Thread thread = new Thread(sensorArray);
        thread.start();
        File iconFile = new File("fxml/trayicon.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
        PopupMenu popup = new PopupMenu();
        MenuItem item = new MenuItem();
        item.setLabel("Inchide");
        popup.add(item);
        TrayIcon trayIcon = new TrayIcon(icon, "The Binance Spider", popup);
        addStrategist(new RangeSpotter(sensorArray, this, 5));
        //addAttachable(new AverageStopLoss(sensorArray, this));
        addAttachable(new TrailingStopLoss(sensorArray, this));
        trayIcon.addActionListener(e -> {
            showUI = true;
            try {
                Thread.sleep(550);
            } catch (Exception ignored)   {
            }
            showUI = false;
        });

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

    public void printBTC()  {
        Interfacer.consolePrint(String.valueOf(sensorArray.getBTCBalance()));
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
            trade = new Trade(sensorArray, sensorArray.getUSDTBalance());
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

    public int getStatus() {
        return status;
    }

    public int getTrades()  {
        return trades;
    }
}