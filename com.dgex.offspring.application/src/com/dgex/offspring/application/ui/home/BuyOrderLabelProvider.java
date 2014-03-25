package com.dgex.offspring.application.ui.home;

import java.text.DecimalFormat;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import com.dgex.offspring.application.utils.ICellDataLabelProvider;
import com.dgex.offspring.providers.service.IBuyOrder;

public class BuyOrderLabelProvider extends ColumnLabelProvider implements
    ICellDataLabelProvider {

  @Override
  public void getCellData(Object o, int columnId, Object[] data) {
    IBuyOrder t = (IBuyOrder) o;
    switch (columnId) {

    case BuyOrderTable.COLUMN_CUMULATIVE:
      data[TEXT] = new DecimalFormat("##.##").format(t.getCumulative());
      break;

    case BuyOrderTable.COLUMN_QUANTITY:
      data[TEXT] = new DecimalFormat("##.##").format(t.getQuantity());
      break;

    case BuyOrderTable.COLUMN_QUOTE_PRICE:
      data[TEXT] = new DecimalFormat(t.getBase().getDecimalFormat()).format(t
          .getPrice());
      break;

    case BuyOrderTable.COLUMN_TOTAL_PRICE:
      data[TEXT] = new DecimalFormat("##.##").format(t.getPrice()
          * t.getQuantity());
      break;

    default:
      data[TEXT] = "UNKNOWN " + columnId;
      break;
    }
  }

  @Override
  public void update(ViewerCell cell) {
    super.update(cell);
    IBuyOrder t = (IBuyOrder) cell.getElement();
    Object[] data = { null, null, null, null };
    getCellData(t, BuyOrderTable.getColumns()[cell.getColumnIndex()], data);
    if (data[TEXT] != null)
      cell.setText((String) data[TEXT]);
    if (data[IMAGE] != null)
      cell.setImage((Image) data[IMAGE]);
    if (data[FONT] != null)
      cell.setFont((Font) data[FONT]);
    if (data[FOREGROUND] != null)
      cell.setForeground((Color) data[FOREGROUND]);
  }
}
