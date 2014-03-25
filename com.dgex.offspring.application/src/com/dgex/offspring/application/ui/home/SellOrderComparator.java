package com.dgex.offspring.application.ui.home;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

import com.dgex.offspring.providers.service.ISellOrder;

public class SellOrderComparator extends ViewerComparator {

  // private static final String EMPTY_STRING = "";

  private int columnID;

  private static final int DESCENDING = 1;

  private int direction = DESCENDING;

  public SellOrderComparator() {
    this.columnID = SellOrderTable.COLUMN_QUOTE_PRICE;
    direction = -DESCENDING;
  }

  public int getDirection() {
    return direction == 1 ? SWT.DOWN : SWT.UP;
  }

  public void setColumn(int columnID) {
    if (columnID == this.columnID) {
      // Same column as last sort; toggle the direction
      direction = 1 - direction;
    }
    else {
      // New column; do an ascending sort
      this.columnID = columnID;
      direction = DESCENDING;
    }
  }

  private int compare(Double _v1, Double _v2) {
    Double v1 = _v1 == null ? -1l : _v1;
    Double v2 = _v2 == null ? -1l : _v2;
    return v1.compareTo(v2);
  }

  // private int compare(Long _v1, Long _v2) {
  // Long v1 = _v1 == null ? -1l : _v1;
  // Long v2 = _v2 == null ? -1l : _v2;
  // return v1.compareTo(v2);
  // }
  //
  // private int compare(String _v1, String _v2) {
  // String v1 = _v1 == null ? EMPTY_STRING : _v1;
  // String v2 = _v2 == null ? EMPTY_STRING : _v2;
  // return v1.compareTo(v2);
  // }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    ISellOrder t1 = (ISellOrder) e1;
    ISellOrder t2 = (ISellOrder) e2;
    int rc = 0;
    switch (columnID) {

    case SellOrderTable.COLUMN_CUMULATIVE:
      rc = compare(t1.getCumulative(), t2.getCumulative());
      break;

    case SellOrderTable.COLUMN_QUANTITY:
      rc = compare(t1.getQuantity(), t2.getQuantity());
      break;

    case SellOrderTable.COLUMN_QUOTE_PRICE:
      rc = compare(t1.getPrice(), t2.getPrice());
      break;

    case SellOrderTable.COLUMN_TOTAL_PRICE:
      double total1 = t1.getPrice() * t1.getQuantity();
      double total2 = t2.getPrice() * t2.getQuantity();
      rc = compare(total1, total2);
      break;

    default:
      rc = 0;
    }
    // If descending order, flip the direction
    if (direction == DESCENDING) {
      rc = -rc;
    }
    return rc;
  }
}
