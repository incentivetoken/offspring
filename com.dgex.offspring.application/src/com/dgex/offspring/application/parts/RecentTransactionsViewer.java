package com.dgex.offspring.application.parts;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nxt.Constants;
import nxt.Transaction;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.core.TransactionDB;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.swt.table.IPageableStructeredContentProvider;
import com.dgex.offspring.swt.table.Pageable;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.ui.InspectTransactionDialog;
import com.dgex.offspring.ui.controls.TransactionTypes;
import com.dgex.offspring.user.service.IUserService;

public class RecentTransactionsViewer extends GenerericTableViewer {

  static Logger logger = Logger.getLogger(RecentTransactionsViewer.class);

  static final RGB DARK_GREEN = new RGB(4, 15, 12);
  static final RGB DARK_RED = new RGB(139, 0, 19);
  static final String EMPTY_STRING = "";
  static final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "dd MMM yy H:mm:ss");

  final IGenericTableColumn columnAmount = new GenericTableColumnBuilder(
      "Amount").align(SWT.RIGHT).textExtent("1000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return Long.valueOf(t.getAmountNQT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Long amountNQT = (Long) getCellValue(element);
          data[FONT] = JFaceResources.getFontRegistry().getBold("");
          data[TEXT] = Utils.quantToString(amountNQT, 8);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnHeight = new GenericTableColumnBuilder(
      "Height").align(SWT.RIGHT).textExtent("000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return Integer.valueOf(t.getHeight());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Integer height = (Integer) getCellValue(element);
          data[TEXT] = Integer.toString(height);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnTransactionType = new GenericTableColumnBuilder(
      "Type").align(SWT.LEFT).textExtent("bid order cancel x")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return TransactionTypes.getTransactionType(t);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent("dd MMM yy hh:mm:ss ")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return new Date(((t.getTimestamp()) * 1000l)
              + (Constants.EPOCH_BEGINNING - 500L));
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = dateFormat.format(getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare(((Date) v1).getTime(), ((Date) v2).getTime());
        }
      }).build();

  final IGenericTableColumn columnSender = new GenericTableColumnBuilder(
      "Sender").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          Long id = t.getSenderId();
          if (id != null) {
            InspectAccountDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return t.getSenderId();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = Convert.toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnReceiver = new GenericTableColumnBuilder(
      "Receiver").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          Long id = t.getRecipientId();
          if (id != null) {
            InspectAccountDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return t.getRecipientId();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = Convert.toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnFee = new GenericTableColumnBuilder("Fee")
      .align(SWT.RIGHT).textExtent("10000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          return Long.valueOf(t.getFeeNQT());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = Utils.quantToString((Long) getCellValue(element), 8);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnID = new GenericTableColumnBuilder("ID")
      .align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          Long id = t.getId();
          if (id != null) {
            InspectTransactionDialog.show(id, nxt, engine, userService, sync,
                contactsService);
          }
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction t = (ITransaction) element;
          return t.getStringId();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((String) v1, (String) v2);
        }
      }).build();

  final IGenericTableColumn columnEmpty = new GenericTableColumnBuilder("")
      .align(SWT.RIGHT).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          return null;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[ICellDataProvider.TEXT] = "";
        }

        @Override
        public int compare(Object v1, Object v2) {
          return 0;
        }
      }).build();

  final IPageableStructeredContentProvider contentProvider = new IPageableStructeredContentProvider() {

    private int currentPage = 1;
    private int pageSize = -1;
    private TransactionDB.LazyList list;

    @Override
    public void dispose() {
      if (list != null) {
        list.dispose();
        list = null;
      }
    }

    @Override
    public void reset(Viewer viewer) {
      if (list != null)
        list.dispose();

      if (pageSize < 1)
        throw new RuntimeException("Illegal page size");

      Boolean orderAscending = Boolean.FALSE;
      this.list = TransactionDB.getTransactions(orderAscending, nxt);
      this.currentPage = 1;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

    @Override
    public Object[] getElements(Object inputElement) {
      if (list == null) {
        return new Object[0];
      }

      logger.info("getElements page=" + currentPage + " size=" + pageSize);
      list.ensureCapacity(currentPage * pageSize + 1);

      Pageable<ITransaction> pageable = new Pageable<ITransaction>(
          list.getList(), pageSize);
      pageable.setPage(currentPage);

      List<ITransaction> transactions = pageable.getListForPage();
      logger.info("getElements returns ELEMENTS.size=" + transactions.size());
      return transactions.toArray(new Object[transactions.size()]);
    }

    @Override
    public void setCurrentPage(int currentPage) {
      this.currentPage = currentPage;
    }

    @Override
    public void setPageSize(int pageSize) {
      this.pageSize = pageSize;
    }

    @Override
    public int getElementCount() {
      return list == null ? 0 : list.available();
    }
  };

  public INxtService nxt;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;
  private IContactsService contactsService;

  public RecentTransactionsViewer(Composite parent, INxtService nxt,
      IStylingEngine engine, IUserService userService, UISynchronize sync,
      IContactsService contactsService) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.nxt = nxt;
    this.engine = engine;
    this.userService = userService;
    this.sync = sync;
    this.contactsService = contactsService;
    setGenericTable(new IGenericTable() {

      @Override
      public int getDefaultSortDirection() {
        return 0; // not used
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return null;
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnHeight, columnDate, columnID,
            columnSender, columnReceiver, columnAmount, columnFee,
            columnTransactionType, columnEmpty };
      }
    });
    refresh();
  }
}
