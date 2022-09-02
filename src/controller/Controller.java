package controller;

import controller.commands.Command;
import model.DataHandler;
import view.Interfacer;
import java.lang.reflect.Constructor;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import java.awt.Toolkit;

public class Controller {

    private final DataHandler dataHandler;
    private final Interfacer interfacer;
    private Watcher watcher;
    private Trade trade;
    private int tradeIndex = 1;
    private final DecimalFormat format = new DecimalFormat("#.##");

    public Controller(Interfacer interfacer, DataHandler dataHandler) {
        this.dataHandler = dataHandler;
        this.interfacer = interfacer;
        interfacer.setController(this);
        format.setRoundingMode(RoundingMode.FLOOR);
    }

    public boolean parseCommand(String command) {
        try {
            ClassLoader loader = Command.class.getClassLoader();
            Class<?> driver = Class.forName("controller.commands." + command, true, loader);
            Constructor<?> commandConstructor = driver.getConstructor();
            Command com = (Command)  commandConstructor.newInstance();
            com.execute(this);
        } catch (Exception e) {
            return (false);
        }
        return true;
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

    public void start() {
        if (watcher != null)    {
            interfacer.alreadyStarted();
        } else {
            Toolkit.getDefaultToolkit().beep();
            watcher = new Watcher(dataHandler.getSensorArray(), this);
            final Thread watcherThread = new Thread(watcher);
            watcherThread.start();
            interfacer.announceStart();
        }
    }

    public void stop() {
        try {
            watcher.stop();
            interfacer.announceStop();
        } catch (Exception e)    {
            interfacer.notStarted();
        }
    }

    public void updateConstants()   {
        dataHandler.updateConstants();
        interfacer.constantsUpdated();
    }

    public void buySignal() {
        if (trade == null) {
            Toolkit.getDefaultToolkit().beep();
            trade = new Trade(dataHandler, dataHandler.getUSDTBalance());
            try {
                trade.open();
                interfacer.tradeOpened(tradeIndex);
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sellSignal()    {
        if(trade != null)   {
            try {
                trade.close();
                tradeClosed();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void tradeClosed()   {
        interfacer.tradeClosed(tradeIndex);
        tradeIndex++;
        trade = null;
        watcher.pause();
    }
}