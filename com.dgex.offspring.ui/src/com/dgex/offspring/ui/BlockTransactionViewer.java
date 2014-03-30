package com.dgex.offspring.ui;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import nxt.Block;
import nxt.Constants;
import nxt.Nxt;
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

import com.dgex.offspring.config.Colors;
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
import com.dgex.offspring.ui.controls.TransactionTypes;
import com.dgex.offspring.user.service.IUserService;

public class BlockTransactionViewer extends GenerericTableViewer {

  static Logger logger = Logger.getLogger(BlockTransactionViewer.class);
  static final String EMPTY_STRING = "";
  static final RGB DARK_GREEN = new RGB(4, 15, 12);
  static final RGB DARK_RED = new RGB(139, 0, 19);

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent("dd MMM yy hh:mm:ss ")
      .provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy H:mm:ss");

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
          Date date = new Date(((t.getTimestamp()) * 1000L)
              + (Constants.EPOCH_BEGINNING - 500L));
          return date; // Date
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

  final IGenericTableColumn columnAmount = new GenericTableColumnBuilder(
      "Amount").align(SWT.RIGHT).textExtent("1000000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
          return Integer.valueOf(t.getAmount());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Integer amount = (Integer) getCellValue(element);
          data[FONT] = JFaceResources.getFontRegistry().getBold("");
          data[TEXT] = Long.toString(amount);
          if (amount > 0)
            data[FOREGROUND] = Colors.getColor(DARK_GREEN);
          else if (amount < 0)
            data[FOREGROUND] = Colors.getColor(DARK_RED);
          else
            data[FOREGROUND] = null;
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnFee = new GenericTableColumnBuilder("Fee")
      .align(SWT.RIGHT).textExtent("1000000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
          return Integer.valueOf(t.getFee());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = Integer.toString((Integer) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnTransactionType = new GenericTableColumnBuilder(
      "Type").align(SWT.LEFT).textExtent("xxxxxxxx")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
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

  final IGenericTableColumn columnSender = new GenericTableColumnBuilder(
      "Sender").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Transaction t = (Transaction) element;
          InspectAccountDialog.show(t.getSenderId(), nxt, engine, userService,
              sync, contactsService);
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
          return Convert.toUnsignedLong(t.getSenderId());
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

  final IGenericTableColumn columnRecipient = new GenericTableColumnBuilder(
      "Recipient").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Transaction t = (Transaction) element;
          InspectAccountDialog.show(t.getRecipientId(), nxt, engine,
              userService, sync, contactsService);
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
          return Convert.toUnsignedLong(t.getRecipientId());
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

  final IGenericTableColumn columnID = new GenericTableColumnBuilder("ID")
      .align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          Transaction t = (Transaction) element;
          InspectTransactionDialog.show(t.getId(), nxt, engine, userService,
              sync, contactsService);
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          Transaction t = (Transaction) element;
          return Convert.toUnsignedLong(t.getId());
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

  final IStructuredContentProvider contentProvider = new IStructuredContentProvider() {

    private Long blockId = null;

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.blockId = (Long) newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (blockId == null) {
        return new Object[0];
      }

      Block block = Nxt.getBlockchain().getBlock(blockId);
      if (block == null) {
        return new Object[0];
      }

      List<? extends Transaction> transactions = block.getTransactions();
      return transactions.toArray(new Object[transactions.size()]);
    }
  };

  private IContactsService contactsService;
  private Long blockId;
  private INxtService nxt;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;

  public BlockTransactionViewer(Composite parent, Long blockId,
      IContactsService contactsService, INxtService nxt, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.contactsService = contactsService;
    this.nxt = nxt;
    this.blockId = blockId;
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
        return new IGenericTableColumn[] { columnDate, columnAmount, columnFee,
            columnSender, columnRecipient, columnID, columnTransactionType };
      }
    });
    setInput(blockId);
  }

  public Long getBlockId() {
    return blockId;
  }

  public IContactsService getContactsService() {
    return contactsService;
  }
}
