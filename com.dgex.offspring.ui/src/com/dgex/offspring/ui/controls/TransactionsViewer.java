package com.dgex.offspring.ui.controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Transaction;
import nxt.TransactionType;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.Colors;
import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContact;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.config.Images;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.core.TransactionDB;
import com.dgex.offspring.nxtCore.core.TransactionHelper;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;
import com.dgex.offspring.swt.table.GenerericTableViewer;
import com.dgex.offspring.swt.table.GenericComparator;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellActivateHandler;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.swt.table.IPageableStructeredContentProvider;
import com.dgex.offspring.swt.table.Pageable;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.ui.InspectTransactionDialog;
import com.dgex.offspring.user.service.IUserService;

public class TransactionsViewer extends GenerericTableViewer {

  static Logger logger = Logger.getLogger(TransactionsViewer.class);

  static final RGB DARK_GREEN = new RGB(4, 15, 12);
  static final RGB DARK_RED = new RGB(139, 0, 19);
  static final String EMPTY_STRING = "";
  static final Image MONEY_RECEIVED = Images.getImage("money_add.png");
  static final Image MONEY_SEND = Images.getImage("money_delete.png");
  static final Image MONEY = Images.getImage("money.png");

  final IGenericTableColumn columnType = new GenericTableColumnBuilder("")
      .align(SWT.CENTER).textExtent("XXX").editable(false)
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          // OK
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          if (accountId.equals(t.getSenderId())) {
            if (accountId.equals(t.getRecipientId())) {
              return MONEY;
            }
            return MONEY_SEND;
          }
          else if (accountId.equals(t.getRecipientId())) {
            return MONEY_RECEIVED;
          }
          return null;
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[IMAGE] = getCellValue(element);
          data[TEXT] = EMPTY_STRING;
        }

        @Override
        public int compare(Object v1, Object v2) {
          return 0;
        }
      }).build();

  final IGenericTableColumn columnAmount = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_amount_label).align(SWT.RIGHT)
      .textExtent("1000000000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          // NOT OK
          ITransaction t = (ITransaction) element;
          long received = t.getAmountReceived(accountId);
          long spend = t.getAmountSpend(accountId);
          return Long.valueOf(received - spend);
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Long amount = (Long) getCellValue(element);

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
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnBalance = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_balance_label).align(SWT.RIGHT)
      .textExtent("1000000000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          // NOT OK
          ITransaction t = (ITransaction) element;
          return Long.valueOf(t.getRunningTotal()); // Long
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Long balance = (Long) getCellValue(element);
          if (balance < 0l)
            data[TEXT] = EMPTY_STRING;
          else
            data[TEXT] = Long.toString(balance);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Long) v1, (Long) v2);
        }
      }).build();

  final IGenericTableColumn columnConfirmations = new GenericTableColumnBuilder(
      "").align(SWT.CENTER).textExtent("1000000")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction t = (ITransaction) element;
          return Integer.valueOf(t.getNumberOfConfirmations());
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          Integer confirmations = (Integer) getCellValue(element);
          data[TEXT] = confirmations > 10 ? "10+" : Integer
              .toString(confirmations);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare((Integer) v1, (Integer) v2);
        }
      }).build();

  final IGenericTableColumn columnTransactionType = new GenericTableColumnBuilder(
      "type").align(SWT.LEFT).textExtent("xxxxxxxx")
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

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_date_label).align(SWT.LEFT)
      .textExtent("dd MMM yy hh:mm:ss ").provider(new ICellDataProvider() {

        private final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "dd MMM yy hh:mm:ss");

        @Override
        public Object getCellValue(Object element) {
          ITransaction t = (ITransaction) element;
          return t.getTimestamp(); // Date
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

  final IGenericTableColumn columnAccount = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_account_label).align(SWT.LEFT)
      .textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();
          Long id = null;
          if (accountId.equals(t.getSenderId())) {
            id = t.getRecipientId();
          }
          else {
            id = t.getSenderId();
          }
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
          if (accountId.equals(t.getSenderId())) {
            return Convert.toUnsignedLong(t.getRecipientId());
          }
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

  final IGenericTableColumn columnName = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_name_label).align(SWT.LEFT)
      .textExtent("12345678901234567890123").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction transaction = (ITransaction) element;
          Transaction t = transaction.getNative();

          IContact contact;
          if (t.getSenderId().equals(accountId))
            contact = getContactsService().getContact(
                Convert.toUnsignedLong(t.getRecipientId()));
          else
            contact = getContactsService().getContact(
                Convert.toUnsignedLong(t.getSenderId()));
          if (contact != null)
            return contact.getName();

          return EMPTY_STRING;
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

  final IGenericTableColumn columnFee = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_fee_label).align(SWT.RIGHT)
      .textExtent("1000000").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          ITransaction t = (ITransaction) element;
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

  final IGenericTableColumn columnID = new GenericTableColumnBuilder(
      Messages.TransactionTable_column_transaction_label).align(SWT.LEFT)
      .textExtent("12345678901234567890123")
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

  final IPageableStructeredContentProvider contentProvider = new IPageableStructeredContentProvider() {

    private Long accountId = null;
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

      if (accountId == null)
        return;

      int timestamp = 0;
      Boolean orderAscending = Boolean.FALSE;
      Object referencedTransaction = null;
      TransactionType[] recipientTypes = { TransactionType.Payment.ORDINARY, };
      TransactionType[] senderTypes = { TransactionType.Payment.ORDINARY,
          TransactionType.Messaging.ALIAS_ASSIGNMENT,
          TransactionType.Messaging.ARBITRARY_MESSAGE,
          TransactionType.Messaging.POLL_CREATION,
          TransactionType.Messaging.VOTE_CASTING,
          TransactionType.ColoredCoins.ASK_ORDER_CANCELLATION,
          TransactionType.ColoredCoins.ASK_ORDER_PLACEMENT,
          TransactionType.ColoredCoins.ASSET_ISSUANCE,
          TransactionType.ColoredCoins.BID_ORDER_CANCELLATION,
          TransactionType.ColoredCoins.BID_ORDER_PLACEMENT };

      this.list = TransactionDB.getTransactions(accountId, recipientTypes,
          senderTypes, timestamp, orderAscending, referencedTransaction,
          ((TransactionsViewer) viewer).nxt);
      this.currentPage = 1;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      this.accountId = (Long) newInput;
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (accountId == null || list == null) {
        return new Object[0];
      }

      logger.info("getElements page=" + currentPage + " size=" + pageSize);
      list.ensureCapacity(currentPage * pageSize + 1);

      Pageable<ITransaction> pageable = new Pageable<ITransaction>(
          list.getList(), pageSize);
      pageable.setPage(currentPage);

      List<ITransaction> transactions = pageable.getListForPage();

      /* Add all pending transactions for this account */
      if (currentPage == 1) {
        List<Transaction> pending = new ArrayList<Transaction>();
        for (Transaction t : nxt.getPendingTransactions()) {
          if (accountId.equals(t.getSenderId())
              || accountId.equals(t.getRecipientId())) {
            pending.add(t);
          }
        }
        if (pending.size() > 0) {
          List<Transaction> remove = new ArrayList<Transaction>();
          for (Transaction t : pending) {
            for (ITransaction it : transactions) {
              if (t.getId().equals(it.getNative().getId())) {
                remove.add(t);
                break;
              }
            }
          }
          for (Transaction t : remove) {
            nxt.getPendingTransactions().remove(t);
            pending.remove(t);
          }
          for (Transaction t : pending) {
            transactions.add(0, new TransactionHelper(nxt, t));
          }
        }
      }

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

  private IContactsService contactsService;
  private Long accountId;
  public INxtService nxt;
  private IStylingEngine engine;
  private IUserService userService;
  private UISynchronize sync;

  public TransactionsViewer(Composite parent, Long accountId,
      IContactsService contactsService, INxtService nxt, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE
        | SWT.BORDER);
    this.contactsService = contactsService;
    this.nxt = nxt;
    this.accountId = accountId;
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
        return new IGenericTableColumn[] { columnType, columnConfirmations,
            columnDate, columnAmount, columnFee, columnAccount, columnID,
            columnTransactionType };
      }
    });
    setInput(accountId);
  }

  public Long getAccountId() {
    return accountId;
  }

  public IContactsService getContactsService() {
    return contactsService;
  }
}
