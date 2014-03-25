package com.dgex.offspring.ui.controls;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.user.service.IUserService;

public class AssetsControl extends Composite {

  static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

  private final AssetsViewer assetsViewer;
  private final Long accountId;

  private final PaginationContainer paginationContainer;

  // private final IUser user;

  public AssetsControl(Composite parent, int style, Long accountId,
      INxtService nxt, IUserService userService,
      IContactsService contactsService, UISynchronize sync,
      IStylingEngine engine) {
    super(parent, style);
    this.accountId = accountId;
    // this.user = userService.findUser(accountId);
    GridLayoutFactory.fillDefaults().spacing(10, 5).numColumns(1).applyTo(this);

    paginationContainer = new PaginationContainer(this, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    assetsViewer = new AssetsViewer(paginationContainer.getViewerParent(),
        accountId, nxt, userService, contactsService, sync, engine);
    paginationContainer.setTableViewer(assetsViewer, 100);
  }

  public void refresh() {
    assetsViewer.setInput(accountId);
  }

}
