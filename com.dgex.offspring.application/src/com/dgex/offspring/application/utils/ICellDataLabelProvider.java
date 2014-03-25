package com.dgex.offspring.application.utils;


public interface ICellDataLabelProvider {

  public static int TEXT = 0;
  public static int IMAGE = 1;
  public static int FONT = 2;
  public static int FOREGROUND = 3;

  public void getCellData(Object o, int columnId, Object[] data);

}
