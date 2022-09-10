package controller;

import controller.commands.Command;
import controller.strategist.RangeSpotter;
import model.DataHandler;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Constructor;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Controller {

    private final DataHandler dataHandler;
    private Trade trade;
    private final DecimalFormat format = new DecimalFormat("#.##");
    private int status = 0;
    private LocalDateTime startTime;
    private int trades = 0;
    private boolean showUI = false;
    private volatile ArrayList<Runnable> strategists = new ArrayList<>();
    private volatile ArrayList<Thread> threads = new ArrayList<>();

    public Controller (DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        format.setRoundingMode(RoundingMode.FLOOR);
        File iconFile = new File("fxml/trayicon.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
        PopupMenu popup = new PopupMenu();
        MenuItem item = new MenuItem();
        item.setLabel("Inchide");
        popup.add(item);
        TrayIcon trayIcon = new TrayIcon(icon, "Păgangănul de Bitcoaie", popup);
        attachStrategist(new RangeSpotter(dataHandler.getSensorArray(), this, 4));
        trayIcon.addActionListener(e -> {
            showUI = true;
            try {
                Thread.sleep(550);
            } catch (Exception h)   {
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

    public void parseCommand(String command) {
        try {
            ClassLoader loader = Command.class.getClassLoader();
            Class<?> driver = Class.forName("controller.commands." + command, true, loader);
            Constructor<?> commandConstructor = driver.getConstructor();
            Command com = (Command)  commandConstructor.newInstance();
            com.execute(this);
        } catch (Exception e) {
        }
    }

    public void attachStrategist(Runnable strategist)   {
        strategists.add(strategist);
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

    public void updateConstants()   {
        dataHandler.updateConstants();
        //interfacer.constantsUpdated();
    }

    public int getUpTime()  {
        if (startTime == null)
            return 0;
        return (int) ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }

    public void buySignal() {
        if (trade == null) {
            /*Toolkit.getDefaultToolkit().beep();
            trade = new Trade(dataHandler, dataHandler.getUSDTBalance());
            try {
                trade.open();
                interfacer.tradeOpened(tradeIndex);
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }*/
            System.out.println("Open signal.");
            status = 2;
        }
    }

    public void sellSignal()    {
        if(trade != null)   {
            /*try {
                trade.close();
                tradeClosed();
                trades++;
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
        status = 1;
        System.out.println("Close signal.");
        trades++;
    }

    public void tradeClosed()   {
        //interfacer.tradeClosed(tradeIndex);
        trade = null;
    }

    public int getStatus() {
        return status;
    }

    public int getTrades()  {
        return trades;
    }
}