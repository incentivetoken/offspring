package com.dgex.offspring.swt.table;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;


public interface IPageableStructeredTreeContentProvider extends
    ITreeContentProvider {

  public void setCurrentPage(int currentPage);

  public void setPageSize(int pageSize);

  public int getElementCount();

  void reset(Viewer viewer);
}
