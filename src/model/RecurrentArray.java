package model;

import com.binance.api.client.domain.market.Candlestick;

import java.util.List;

public class RecurrentArray {

    Candlestick[] array;
    int index = 0;

    public RecurrentArray (int num) {
        array = new Candlestick[num];
    }


    public boolean containsNull()    {
        for (int i = 0; i < array.length; i++)  {
            if (array[i] == null)
                return true;
        }
        return false;
    }

    public void add(Candlestick candlestick)    {
        if (index > 4)  {
            index = 0;
        }
        array[index] = candlestick;
        index++;
    }

    public Candlestick get (int index)  {
        return array[index];
    }

    public int size()   {
        return array.length;
    }

    public void fillWithLast(List<Candlestick> list)  {
        int e = 0;
        for (int i = list.size() - array.length; i < list.size(); i++)   {
            array[e] = list.get(i);
            e++;
        }
        index = 0;
    }



}
