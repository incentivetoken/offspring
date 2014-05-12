package com.dgex.offspring.trader.mytrades;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Account;
import nxt.Asset;
import nxt.Block;
import nxt.Nxt;
import nxt.Trade;
import nxt.util.Convert;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.core.NXTTime;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.trader.api.IAssetExchange;
import com.dgex.offspring.ui.InspectTransactionDialog;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MyTradesViewer extends GenerericTableViewer {

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent("dd MMM yy hh:mm:ss ")
      .provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy H:mm:ss");

        @Override
        public Object getCellValue(Object element) {
          Trade trade = (Trade) element;
          Block block = Nxt.getBlockchain().getBlock(trade.getBlockId());
          return Long.valueOf(NXTTime.convertTimestamp(block.getTimestamp()));
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = dateFormat.format(new Date(
              (Long) getCellValue(element)));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnPrice = new GenericTableColumnBuilder("Price")
      .align(SWT.RIGHT).textExtent("##########")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Trade trade = (Trade) element;
          return Long.valueOf(trade.getPriceNQT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Utils
              .quantToString((Long) getCellValue(element), 8);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnQuantity = new GenericTableColumnBuilder(
      "Quantity").align(SWT.RIGHT).textExtent("##########")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Trade trade = (Trade) element;
          return Long.valueOf(trade.getQuantityQNT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Trade trade = (Trade) element;
          Asset asset = Asset.getAsset(trade.getAssetId());
          
          data[ICellDataProvider.TEXT] = Utils
              .quantToString((Long) getCellValue(element), asset.getDecimals());
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnAskOrder = new GenericTableColumnBuilder(
      "Ask Order").align(SWT.RIGHT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Trade trade = (Trade) element;
          Long id = trade.getAskOrderId();
          if (id != null) {
            InspectTransactionDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Trade trade = (Trade) element;
          return trade.getAskOrderId();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnBidOrder = new GenericTableColumnBuilder(
      "Bid Order").align(SWT.RIGHT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Trade trade = (Trade) element;
          Long id = trade.getBidOrderId();
          if (id != null) {
            InspectTransactionDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Trade trade = (Trade) element;
          return trade.getBidOrderId();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

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

  final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

    @Override
    public Object[] getElements(Object inputElement) {

      IUser user = userService.getActiveUser();
      if (user == null) {
        return new Object[0];
      }

      Account account = user.getAccount().getNative();
      if (account == null) {
        return new Object[0];
      }

      Asset asset = exchange.getSelectedAsset();
      if (asset == null) {
        return new Object[0];
      }

      List<Trade> elements = new ArrayList<Trade>();

      List<Trade> trades = Trade.getTrades(asset.getId());
      for (Trade trade : trades) {
        // trade
      }
      return elements.toArray(new Object[elements.size()]);
    }
  };

  public INxtService nxt;
  private IContactsService contactsService;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;
  private IAssetExchange exchange;

  public MyTradesViewer(Composite parent, INxtService nxt,
      IContactsService contactsService, IStylingEngine engine,
      IUserService userService, UISynchronize sync, IAssetExchange exchange) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.contactsService = contactsService;
    this.engine = engine;
    this.userService = userService;
    this.sync = sync;
    this.exchange = exchange;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return GenericComparator.DESCENDING;
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return columnPrice;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnDate, columnPrice,
            columnQuantity, columnAskOrder, columnBidOrder };
      }
    });
    refresh();
  }
}
