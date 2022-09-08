package controller;

import controller.commands.Command;
import controller.strategist.RangeSpotter;
import model.DataHandler;
import view.Interfacer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Constructor;
import java.math.RoundingMode;
import java.nio.Buffer;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class Controller {

    private final DataHandler dataHandler;
    private Watcher watcher;
    private Trade trade;
    private final DecimalFormat format = new DecimalFormat("#.##");
    private int status = 0;
    private LocalDateTime startTime;
    private int trades = 0;
    private SystemTray tray = SystemTray.getSystemTray();
    private boolean showUI = false;

    public Controller (DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        format.setRoundingMode(RoundingMode.FLOOR);
        RangeSpotter spotter = new RangeSpotter(dataHandler.getSensorArray(), this, 2);
        Thread thread = new Thread(spotter);
        thread.start();
        File iconFile = new File("fxml/trayicon.png");
        Image icon = Toolkit.getDefaultToolkit().getImage(iconFile.getAbsolutePath());
        PopupMenu popup = new PopupMenu();
        TrayIcon trayIcon = new TrayIcon(icon, "Păgangănul de Bitcoaie", popup);
        trayIcon.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showUI = true;
                try {
                    Thread.sleep(550);
                } catch (Exception h)   {
                }
                showUI = false;
            }
        });

        try {
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

    public float getLatestPrice() {
        return dataHandler.getLatestPrice();
    }

    public float getUSDTBalance() {
        return Float.parseFloat(format.format(dataHandler.getUSDTBalance()));
    }

    public float getBTCBalance() {
        return dataHandler.getBTCBalance();
    }

    public Date getServerTime() {
        return dataHandler.getServerTime();
    }

    public boolean showUI() {
        return showUI;
    }

    public void start() {
        if (watcher == null) {
            status = 3;
            Toolkit.getDefaultToolkit().beep();
            watcher = new Watcher(dataHandler.getSensorArray(), this);
            final Thread watcherThread = new Thread(watcher);
            watcherThread.start();
            status = 1;
            startTime = LocalDateTime.now();
        }
    }

    public void stop() {
        try {
            watcher.stop();
            status = 0;
        } catch (Exception e)    {
            //interfacer.notStarted();
        }
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
        System.out.println("Close signal.");
    }

    public void tradeClosed()   {
        //interfacer.tradeClosed(tradeIndex);
        trade = null;
        watcher.pause();
    }

    public int getStatus() {
        return status;
    }

    public int getTrades()  {
        return trades;
    }
}