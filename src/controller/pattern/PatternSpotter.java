package controller.pattern;

import controller.Observer;
import model.SensorArray;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

public class PatternSpotter implements Runnable, Observer {

    private final SensorArray array;
    private volatile boolean patternFound = false;
    private volatile boolean arrayUpdated = false;
    private volatile ArrayList<String> patterns = new ArrayList<>();

    public PatternSpotter (SensorArray array) {
        this.array = array;
        array.addObserver(this);
    }

    public boolean patternFound()   {
        return patternFound;
    }

    public String[] getFoundPatterns() {
        String[] array = new String[patterns.size()];
        array = patterns.toArray(array);
        return array;
    }

    @Override
    public void run() {
        while (true) {
            if (arrayUpdated) {
                patterns = new ArrayList<>();
                patternFound = false;
                Pattern[] pattern = new Pattern[Patterns.values().length];
                try {
                    for (int i = 0; i < pattern.length; i++) {
                        ClassLoader loader = Pattern.class.getClassLoader();
                        Class<?> driver = Class.forName("controller.pattern." + Patterns.values()[i], true, loader);
                        Constructor<?> constructor = driver.getConstructor();
                        pattern[i] = (Pattern) constructor.newInstance();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                for (Pattern value : pattern) {
                    if (value.examine(array.getCandlesticks())) {
                        patterns.add(value.getClass().getName());
                        patternFound = true;
                    }
                }
                arrayUpdated = false;
            }
        }
    }

    @Override
    public void observableUpdated() {
        arrayUpdated = true;
    }
}
