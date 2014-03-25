package com.dgex.offspring.ui.controls;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.user.service.IUserService;

public class GeneratedBlocksControl extends Composite {

  private final GeneratedBlocksViewer generatedBlocksViewer;
  private final PaginationContainer paginationContainer;

  public GeneratedBlocksControl(Composite parent, int style, Long accountId,
      IStylingEngine engine, INxtService nxt, IUserService userService,
      UISynchronize sync, IContactsService contactsService) {
    super(parent, style);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(this);

    paginationContainer = new PaginationContainer(this, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    generatedBlocksViewer = new GeneratedBlocksViewer(
        paginationContainer.getViewerParent(), accountId, engine, nxt,
        userService, sync, contactsService);
    paginationContainer.setTableViewer(generatedBlocksViewer, 300);
  }

  public void refresh() {
    generatedBlocksViewer.refresh();
  }
}
