package trp.util;

public enum Direction
{
  NW, N, NE, W, C, E, SW, S, SE;
  
  protected double DirectionWeighting = 1;

  public double getWeighting(Direction d) {
    return d.DirectionWeighting;
  }

  public void setWeighting(Direction d, double f) {
    d.DirectionWeighting = f;
  }
  
  public String toString() {
    switch (this) {
      case NW:    return "NW";
      case N:     return "N";
      case NE:    return "NE";
      case W:     return "W";
      case C:     return "C";
      case E:     return "E";
      case SW:    return "SW";
      case S:     return "S";
      case SE:    return "SE";
    }
    return null;
  }

  public static Direction fromInt(int idx)
  {
    switch (idx) {
      case 0:     return NW;
      case 1:     return N;
      case 2:     return NE;
      case 3:     return W;
      case 4:     return C;
      case 5:     return E;
      case 6:     return SW;
      case 7:     return S;
      case 8:     return SE;
    }
    Readers.error("Bad direction index: "+idx);
    return null;
  }
    
  public int toInt()
  {
    switch (this) {
      case NW:    return 0;
      case N:     return 1;
      case NE:    return 2;
      case W:     return 3;
      case C:     return 4;
      case E:     return 5;
      case SW:    return 6;
      case S:     return 7;
      case SE:    return 8;
    }
    return -1;
  }
}
