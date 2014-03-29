package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;

public interface IGenericTableColumn {

  public ICellDataProvider getDataProvider();

  public String getLabel();

  public int getAlignMent();

  public String getTextExtent();

  public int getWidth();

  public boolean getStretch();

  public boolean getResizable();

  public boolean getSortable();

  public boolean getEditable();

  public EditingSupport getEditingSupport(ColumnViewer viewer);

  public ICellActivateHandler getCellActivateHandler();
}
