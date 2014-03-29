package com.dgex.offspring.ui.controls;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.IPageableStructeredContentProvider;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.user.service.IUserService;

public class TransactionsControl extends Composite {

  private final TransactionsViewer transactionViewer;
  private final PaginationContainer paginationContainer;

  public TransactionsControl(Composite parent, int style, Long accountId,
      INxtService nxt, IStylingEngine engine, IUserService userService,
      UISynchronize sync) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(this);

    paginationContainer = new PaginationContainer(this, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    transactionViewer = new TransactionsViewer(
        paginationContainer.getViewerParent(), accountId, null, nxt, engine,
        userService, sync);
    paginationContainer.setTableViewer(transactionViewer, 300);

    transactionViewer.getControl().pack();
  }

  public void refresh() {
    IPageableStructeredContentProvider contentProvider = (IPageableStructeredContentProvider) transactionViewer
        .getGenericTable().getContentProvider();
    contentProvider.reset(transactionViewer);
    transactionViewer.refresh();
  }

}
