package com.dgex.offspring.swt.table;


public interface ICellDataProvider {

  public static int TEXT = 0;
  public static int IMAGE = 1;
  public static int FONT = 2;
  public static int FOREGROUND = 3;

  public void getCellData(Object element, Object[] data);

  public Object getCellValue(Object element);

  public int compare(Object v1, Object v2);
}
