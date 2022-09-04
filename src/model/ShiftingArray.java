package model;

import java.sql.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ShiftingArray<T> {

  private final int num;
  private final List<T> array;
  private int index = 0;

  public ShiftingArray(int num)  {
    this.num = num;
    array = new LinkedList<T>();
  }

  public void add (T obj) {
    if (array.size() >= num)  {
      for (int i = 0; i < num - 1; i++)  {
        array.set(i, array.get(i + 1));
      }
      array.remove(array.size() -1);
      array.add(obj);
    } else {
      array.add(obj);
    }
  }

  public T get (int index) {
    return array.get(index);
  }

  public boolean isFilled() {
    return (array.size() == num);
  }

  public List<T> getStandardArray() {
    List<T> list = new LinkedList<>();
    list = array.stream().toList();
    return list;
  }
}
