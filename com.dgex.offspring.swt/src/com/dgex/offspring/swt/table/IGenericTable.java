package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.IStructuredContentProvider;

public interface IGenericTable {

  /* Mostly 0 */
  public IGenericTableColumn getDefaultSortColumn();

  public int getDefaultSortDirection();

  public IGenericTableColumn[] getColumns();

  public IStructuredContentProvider getContentProvider();

}
