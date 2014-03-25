package com.dgex.offspring.application.ui.home;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.swt.SWT;

public class SellOrderTable {

  public final static int COLUMN_CUMULATIVE = 32;
  public final static int COLUMN_TOTAL_PRICE = 33;
  public final static int COLUMN_QUANTITY = 34;
  public final static int COLUMN_QUOTE_PRICE = 35;

  private final static int[] columns = new int[] {

  COLUMN_QUOTE_PRICE,

  COLUMN_QUANTITY,

  COLUMN_TOTAL_PRICE,

  COLUMN_CUMULATIVE

  };

  public static int[] getColumns() {
    return columns;
  }

  public static String getColumnLabel(int id) {
    switch (id) {
    case COLUMN_CUMULATIVE:
      return "Cumulative";
    case COLUMN_TOTAL_PRICE:
      return "Total";
    case COLUMN_QUANTITY:
      return "Quantity";
    case COLUMN_QUOTE_PRICE:
      return "Price";
    }
    return "FAILURE"; //$NON-NLS-1$
  }

  public static int getColumnWidth(int id) {
    switch (id) {
    case COLUMN_CUMULATIVE:
    case COLUMN_TOTAL_PRICE:
    case COLUMN_QUANTITY:
    case COLUMN_QUOTE_PRICE:
      return 80;
    }
    return 10;
  }

  public static String getColumnTextExtent(int id) {
    switch (id) {
    case COLUMN_CUMULATIVE:
      return "##########";
    case COLUMN_TOTAL_PRICE:
      return "##########";
    case COLUMN_QUANTITY:
      return "########";
    case COLUMN_QUOTE_PRICE:
      return "##########";
    }
    return "xx";
  }

  public static CellLabelProvider createLabelProvider(int id) {
    return new SellOrderLabelProvider();
  }

  public static int getColumnAlignment(int id) {
    switch (id) {
    case COLUMN_CUMULATIVE:
    case COLUMN_TOTAL_PRICE:
    case COLUMN_QUANTITY:
    case COLUMN_QUOTE_PRICE:
      return SWT.RIGHT;
    }
    return SWT.LEFT;
  }

  public static boolean getColumnResizable(int id) {
    return true;
  }
}
