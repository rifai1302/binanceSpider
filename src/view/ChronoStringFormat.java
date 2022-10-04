package view;

public class ChronoStringFormat {

  public String format(int seconds) {
    int s = 0;
    int m = 0;
    int h = 0;
    int d = 0;
    int w = 0;
    int mo = 0;

    for (int i = 0; i < seconds; i++)  {
      s++;
      if (s == 60) {
        m++;
        s = 0;
      }
      if (m == 60) {
        h++;
        m = 0;
      }
      if (h == 24)  {
        d++;
        h = 0;
      }
      if (d == 7) {
        w++;
        d = 0;
      }
      if (w == 30)  {
        mo++;
        w = 0;
      }
    }
    String ret = "";
    if (mo > 0) {
      ret += mo + " mo ";
    }
    if (w > 0)  {
      ret += w + " w ";
    }
    if (d > 0)  {
      ret += d + " d ";
    }
    if ((h > 0) && (w < 1))  {
      ret += h + " h ";
    }
    if ((m > 0) && (d < 1)) {
      ret += m + " min ";
    }
    if ((h < 1) && (d < 1))
    ret += s + " s ";
    return ret;
  }
}
