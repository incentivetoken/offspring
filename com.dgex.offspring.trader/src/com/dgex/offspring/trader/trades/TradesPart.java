package com.dgex.offspring.trader.trades;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.Asset;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.trader.api.IAssetExchange;
import com.dgex.offspring.user.service.IUserService;

public class TradesPart {

  private Composite mainComposite;
  private TradesViewer tradesViewer;
  private PaginationContainer paginationContainer;

  @PostConstruct
  public void postConstruct(Composite parent, INxtService nxt,
      final IUserService userService, IStylingEngine engine,
 UISynchronize sync) {

    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(mainComposite);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(mainComposite);

    paginationContainer = new PaginationContainer(mainComposite, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    tradesViewer = new TradesViewer(paginationContainer.getViewerParent(), nxt,
        ContactsService.getInstance(), engine, userService, sync);
    paginationContainer.setTableViewer(tradesViewer, 100);
  }

  @Inject
  @Optional
  private void onAssetSelected(
      @UIEventTopic(IAssetExchange.TOPIC_ASSET_SELECTED) Asset asset) {
    if (tradesViewer != null && !tradesViewer.getControl().isDisposed()) {
      tradesViewer.setInput(asset.getId());
      tradesViewer.refresh();
    }
  }
}