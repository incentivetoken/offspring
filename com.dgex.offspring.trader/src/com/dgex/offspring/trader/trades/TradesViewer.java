package com.dgex.offspring.trader.trades;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

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
import com.dgex.offspring.ui.InspectTransactionDialog;
import com.dgex.offspring.user.service.IUserService;

public class TradesViewer extends GenerericTableViewer {

  static String EXTENT_COLUMN_DATE = "dd MMM yy hh:mm:ss ";
  static String EXTENT_COLUMN_PRICE = "1000000000";
  static String EXTENT_COLUMN_ID = "012345678901234567890";
  static String EXTENT_COLUMN_QUANTITY = "1000000000";
  static DecimalFormat formatDouble = new DecimalFormat("#.##");

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent(EXTENT_COLUMN_DATE)
      .provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy hh:mm:ss");

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
  
  final IGenericTableColumn columnTotal = new GenericTableColumnBuilder("Total")
    .align(SWT.RIGHT).textExtent(EXTENT_COLUMN_PRICE)
    .provider(new ICellDataProvider() {

    @Override
    public Object getCellValue(Object element) {
      Trade trade = (Trade) element;
      return Long.valueOf(Utils.calculateOrderTotalNQT(trade.getQuantityQNT(), trade.getPriceNQT()));
    }

    @Override
    public void getCellData(Object element, Object[] data) {
      Long totalNQT = (Long) getCellValue(element);
      if (totalNQT == null)
        data[ICellDataProvider.TEXT] = "-";
      else
        data[ICellDataProvider.TEXT] = Utils.quantToString(totalNQT, 8);
    }

    @Override
    public int compare(Object v1, Object v2) {
      return CompareMe.compare((Long) v1, (Long) v2);
    }
  }).build();

  final IGenericTableColumn columnPrice = new GenericTableColumnBuilder("Price")
      .align(SWT.RIGHT).textExtent(EXTENT_COLUMN_PRICE)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Trade trade = (Trade) element;
          Asset asset = Asset.getAsset(trade.getAssetId());
          return Long.valueOf(Utils.calculateOrderPricePerWholeQNT_InNQT(trade.getPriceNQT(), asset.getDecimals()));          
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Utils.quantToString((Long) getCellValue(element), 8);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnQuantity = new GenericTableColumnBuilder(
      "Quantity").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_QUANTITY)
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
      "Ask Order").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_ID)
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
      "Bid Order").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_ID)
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

  final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

    private TradesViewer viewer;
    private Long assetId;

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (TradesViewer) viewer;
      this.assetId = (Long) newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (assetId == null)
        return new Object[0];

      List<Trade> trades = viewer.nxt.getTrades(assetId);
      return trades.toArray(new Object[trades.size()]);
    }
  };

  public INxtService nxt;
  private IContactsService contactsService;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;

  public TradesViewer(Composite parent, INxtService nxt,
      IContactsService contactsService, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.contactsService = contactsService;
    this.engine = engine;
    this.userService = userService;
    this.sync = sync;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return GenericComparator.DESCENDING;
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return columnDate;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnDate, columnPrice,
            columnQuantity, columnTotal, columnAskOrder, columnBidOrder };
      }
    });
    refresh();
  }
}
