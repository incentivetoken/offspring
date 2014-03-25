package com.dgex.offspring.trader.mytrades;

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

public class MyTradesPart {

  private Composite mainComposite;
  private MyTradesViewer tradesViewer;
  private PaginationContainer paginationContainer;

  private volatile Long accountId = null;
  private volatile Long assetId = null;

  @PostConstruct
  public void postConstruct(Composite parent, INxtService nxt,
      final IUserService userService, IStylingEngine engine,
      UISynchronize sync, IAssetExchange exchange) {
    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(mainComposite);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(mainComposite);

    paginationContainer = new PaginationContainer(mainComposite, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    // tradesViewer = new TradeTableViewer(mainComposite, exchange);
    tradesViewer = new MyTradesViewer(paginationContainer.getViewerParent(),
        nxt, ContactsService.getInstance(), engine, userService, sync, exchange);
    paginationContainer.setTableViewer(tradesViewer, 100);
  }

  /**
   * It's difficult currently to list all trades for an account and then for a
   * single asset. The reason being that trades as such are not stored in the
   * blockchain. Instead they are calculated upon scanning the blockchain.
   * 
   * Trades are stored on the Trade class together with a buy order id and a
   * sell order id. These id's can then be linked to an account.
   * 
   * Easiest would probably be if we would ask for all Trades for an asset and
   * then filter that on sell order and buy order ids that belong to that
   * account.
   */

  // private void longRunningDataCollector() {
  // new Thread(new Runnable() {
  //
  // @Override
  // public void run() {
  // Long localAccountId = accountId;
  // Long localAssetId = assetId;
  //
  // /* Collect all Trade's for an asset */
  // List<Trade> trades = Trade.getTrades(assetId);
  //
  // // perform the long running operation here, the result is passed to the
  // // table viewer
  //
  // }
  // }).start();
  // }

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