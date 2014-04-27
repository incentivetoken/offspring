package com.dgex.offspring.trader.askorders;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import nxt.Order;
import nxt.Order.Ask;
import nxt.util.Convert;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.user.service.IUserService;

public class AskOrdersViewer extends GenerericTableViewer {

  static String EXTENT_COLUMN_TOTAL = "000000";
  static String EXTENT_COLUMN_PRICE = "000000";
  static String EXTENT_COLUMN_QUANTITY = "000000";
  static String EXTENT_COLUMN_SELLER = "000000";

  static DecimalFormat formatDouble = new DecimalFormat("#.##");

  final IGenericTableColumn columnTotal = new GenericTableColumnBuilder("Total")
      .align(SWT.RIGHT).textExtent(EXTENT_COLUMN_TOTAL)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Order order = (Order) element;

          try {
            long total = Convert.safeMultiply(order.getPriceNQT(),
                order.getQuantityQNT());
            return Long.valueOf(total);
          }
          catch (ArithmeticException e) {
            return null;
          }
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Long total = (Long) getCellValue(element);
          if (total == null)
            data[ICellDataProvider.TEXT] = "-";
          else
            data[ICellDataProvider.TEXT] = Convert.toNXT(total);
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
          Order order = (Order) element;
          return Long.valueOf(order.getPriceNQT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toNXT((Long) getCellValue(element));
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
          Order order = (Order) element;
          return Long.valueOf(order.getQuantityQNT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = Convert
              .toNXT((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnSeller = new GenericTableColumnBuilder(
      "Seller").align(SWT.RIGHT).textExtent(EXTENT_COLUMN_SELLER)
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Order order = (Order) element;
          Long id = order.getAccount().getId();
          if (id != null) {
            InspectAccountDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Order order = (Order) element;
          return order.getAccount().getId();
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

    private AskOrdersViewer viewer;
    private Long assetId;

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.viewer = (AskOrdersViewer) viewer;
      this.assetId = (Long) newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (assetId == null)
        return new Object[0];

      List<Order> orders = new ArrayList<Order>(Ask.getSortedOrders(assetId));
      return orders.toArray(new Object[orders.size()]);
    }
  };

  public INxtService nxt;
  private IContactsService contactsService;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;

  public AskOrdersViewer(Composite parent, INxtService nxt,
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
        return GenericComparator.ASSCENDING;
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
        return new IGenericTableColumn[] { columnTotal, columnPrice,
            columnQuantity, columnSeller };
      }
    });
    refresh();
  }
}
