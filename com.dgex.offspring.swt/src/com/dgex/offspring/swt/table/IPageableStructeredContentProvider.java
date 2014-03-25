package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public interface IPageableStructeredContentProvider extends
    IStructuredContentProvider {

  public void setCurrentPage(int currentPage);

  public void setPageSize(int pageSize);

  public int getElementCount();

  void reset(Viewer viewer);

}
