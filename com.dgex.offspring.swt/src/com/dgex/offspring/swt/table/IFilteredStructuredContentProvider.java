package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.IStructuredContentProvider;

public interface IFilteredStructuredContentProvider extends
    IStructuredContentProvider {

  public void setFilter(String id, String filter);

}
