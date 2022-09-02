package view;


import model.Constants;
import controller.Controller;
import controller.Trade;

import java.util.Scanner;

public class Interfacer implements Runnable {

    private Controller controller;

    private final String ANSI_RESET = "\u001B[0m";
    private final String ANSI_RED = "\u001B[31m";
    private final String ANSI_GREEN = "\u001B[32m";
    private final String ANSI_YELLOW = "\u001B[33m";
    private final String ANSI_BLUE = "\u001B[34m";
    private final String ANSI_PURPLE = "\u001B[35m";
    private final String ANSI_CYAN = "\u001B[36m";

    public void setController(Controller controller)    {
        this.controller = controller;
    }

    private String goldBTC()    {
        return (ANSI_YELLOW + "BTC" + ANSI_RESET);
    }

    private void gigelSay(String string)    {
        System.out.print(ANSI_CYAN + "Gigel: " + ANSI_RESET);
        System.out.println(string);
    }

    public void constantsUpdated(){
        gigelSay("Constantele au fost reincarcate!");
    }

    public void announceStart() {
        gigelSay("S-a început comerțul! " +
                "Data serverului: " + ANSI_BLUE +  controller.getServerTime() + ANSI_RESET);
    }

    public void announceStop()  {
        gigelSay(ANSI_YELLOW + "Comerțul s-a oprit. \n" + ANSI_RESET +
                "Data serverului: " + ANSI_BLUE +  controller.getServerTime() + ANSI_RESET);
    }

    public void notStarted()    {
        System.out.println(ANSI_RED + "EROARE: Comerțul este deja oprit. \n" + ANSI_RESET);
    }

    public void alreadyStarted()    {
        System.out.println(ANSI_RED + "EROARE: Comerțul este deja în desfășurare. \n" + ANSI_RESET);
    }

    public void tradeOpened(int number)   {
        gigelSay(ANSI_GREEN + "S-a deschis comerțul cu numărul " + number + ANSI_RESET +
                ". Ora serverului: " + controller.getServerTime());
        gigelSay("Prețul curent al Bitcoin-ului: " + controller.getLatestPrice() +
                " " + ANSI_GREEN + Constants.stable + ANSI_RESET);
    }

    public void tradeClosed(int number)   {
        gigelSay(ANSI_PURPLE + "S-a închis comerțul cu numărul " + number + ANSI_RESET +
                ". Ora serverului: " + controller.getServerTime());
        gigelSay("Prețul curent al Bitcoin-ului: " + controller.getLatestPrice() +
                " " + ANSI_GREEN + Constants.stable + ANSI_RESET);
    }

    @Override
    public void run()   {
        Scanner scan = new Scanner(System.in);
        System.out.println("");
        gigelSay("Interfață pornită cu succes.");
        gigelSay("În balanță se află "
                + ANSI_GREEN + controller.getUSDTBalance() +
                " USDT" + ANSI_RESET + " și "
                + ANSI_YELLOW + controller.getBTCBalance() +
                " BTC" + ANSI_RESET + ".");
        gigelSay("Prețul curent al " + goldBTC() + ": " + ANSI_GREEN + controller.getLatestPrice() + " USDT" + ANSI_RESET + ".");
        while(true) {
            String input = scan.nextLine();
            if (!controller.parseCommand(input))    {
                gigelSay(ANSI_RED + "Comandă necunoscută." + ANSI_RESET);
            }
        }
    }


}
