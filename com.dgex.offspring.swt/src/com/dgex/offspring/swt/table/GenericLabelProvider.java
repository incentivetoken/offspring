package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class GenericLabelProvider extends ColumnLabelProvider {

  private final ICellDataProvider cellDataProvider;
  private final Object[] data = new Object[4];

  public GenericLabelProvider(ICellDataProvider cellDataProvider) {
    this.cellDataProvider = cellDataProvider;
  }

  @Override
  public void update(ViewerCell cell) {
    super.update(cell);

    data[ICellDataProvider.TEXT] = null;
    data[ICellDataProvider.IMAGE] = null;
    data[ICellDataProvider.FONT] = null;
    data[ICellDataProvider.FOREGROUND] = null;

    cellDataProvider.getCellData(cell.getElement(), data);

    if (data[ICellDataProvider.TEXT] != null) {
      cell.setText((String) data[ICellDataProvider.TEXT]);
    }
    if (data[ICellDataProvider.IMAGE] != null) {
      cell.setImage((Image) data[ICellDataProvider.IMAGE]);
    }
    if (data[ICellDataProvider.FONT] != null) {
      cell.setFont((Font) data[ICellDataProvider.FONT]);
    }
    if (data[ICellDataProvider.FOREGROUND] != null) {
      cell.setForeground((Color) data[ICellDataProvider.FOREGROUND]);
    }
  }

}
