package model;

public class Constants {

    public static String crypto = "BTC";
    public static String stable = "USDT";
    public static double triggerMomentum = 0.3;

    public static String getCurrency() {
        return crypto + stable;
    }

}
