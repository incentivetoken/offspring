package com.dgex.offspring.swt.table;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;

public class GenericComparator extends ViewerComparator {

  static Logger logger = Logger.getLogger(GenericComparator.class);

  public static final int DESCENDING = 1;
  public static final int ASSCENDING = -1;

  private IGenericTableColumn column;
  private int direction = DESCENDING;

  public GenericComparator(IGenericTable table) {
    this.column = table.getDefaultSortColumn();
    // direction = -DESCENDING;
    direction = table.getDefaultSortDirection();
  }

  public int getDirection() {
    return direction == 1 ? SWT.DOWN : SWT.UP;
  }

  public void setColumn(IGenericTableColumn column) {
    if (column.equals(this.column)) {
      // Same column as last sort; toggle the direction
      direction = 1 - direction;
    }
    else {
      // New column; do an ascending sort
      this.column = column;
      direction = DESCENDING;
    }
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {
    if (this.column == null)
      return 0;

    int rc = this.column.getDataProvider().compare(
        this.column.getDataProvider().getCellValue(e1),
        this.column.getDataProvider().getCellValue(e2));
    // If descending order, flip the direction
    if (direction == DESCENDING) {
      rc = -rc;
    }
    return rc;
  }
}
