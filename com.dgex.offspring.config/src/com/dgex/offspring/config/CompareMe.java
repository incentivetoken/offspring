package com.dgex.offspring.config;

public class CompareMe {

  private static final String EMPTY_STRING = "";

  public static int compare(Long _v1, Long _v2) {
    Long v1 = _v1 == null ? -1l : _v1;
    Long v2 = _v2 == null ? -1l : _v2;
    return v1.compareTo(v2);
  }

  public static int compare(String _v1, String _v2) {
    String v1 = _v1 == null ? EMPTY_STRING : _v1;
    String v2 = _v2 == null ? EMPTY_STRING : _v2;
    return v1.compareTo(v2);
  }

  public static int compare(boolean _v1, boolean _v2) {
    return (!_v1 && _v2) ? -1 : ((_v1 == _v2) ? 0 : 1);
  }

  public static int compare(double x, double y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public static int compare(long x, long y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public static int compare(int x, int y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public static int compare(short x, short y) {
    return (x < y) ? -1 : ((x == y) ? 0 : 1);
  }

  public static int compare(Enum<?> x, Enum<?> y) {
    return compare(x.ordinal(), y.ordinal());
  }

}
