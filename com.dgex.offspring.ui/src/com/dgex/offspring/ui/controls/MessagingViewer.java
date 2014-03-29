package com.dgex.offspring.ui.controls;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nxt.Account;
import nxt.Constants;
import nxt.Transaction;
import nxt.TransactionType;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import com.dgex.offspring.config.CompareMe;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.config.Images;
import com.dgex.offspring.nxtCore.core.TransactionDB;
import com.dgex.offspring.nxtCore.core.TransactionHelper;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;
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
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MessagingViewer extends GenerericTableViewer {

  static Logger logger = Logger.getLogger(MessagingViewer.class);
  static final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "dd MMM yy hh:mm:ss");
  static final Image MESSAGE_RECEIVED = Images.getImage("bullet_go.png");
  static final Image MESSAGE_SEND = Images.getImage("resultset_previous.png");
  static final Image ENCRYPTED_OPENED = Images.getImage("lock_open.png");
  static final Image ENCRYPTED_LOCKED = Images.getImage("lock_delete.png");
  static final Image NO_ENCRYPTION = Images.getImage("information.png");
  static final String EMPTY_STRING = "";

  /* Returns TRUE for messsage send and FALSE for message received */
  final IGenericTableColumn columnType = new GenericTableColumnBuilder(" ")
      .align(SWT.CENTER).textExtent("XXX").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          if (message.getSenderId().equals(accountId)) {
            return MESSAGE_SEND;
          }
          return MESSAGE_RECEIVED;
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

  final IGenericTableColumn columnType2 = new GenericTableColumnBuilder(" ")
      .align(SWT.CENTER).textExtent("XXX").provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          if (message.isEncrypted()) {
            if (secretPhrase == null) {
              return ENCRYPTED_LOCKED;
            }
            return ENCRYPTED_OPENED;
          }
          return NO_ENCRYPTION;
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

  final IGenericTableColumn columnDate = new GenericTableColumnBuilder("Date")
      .align(SWT.LEFT).textExtent("dd MMM yy hh:mm:ss ")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          Date date = new Date(((message.getTimestamp()) * 1000L)
              + (Constants.EPOCH_BEGINNING - 500L));
          return date;
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
      "Account").align(SWT.LEFT).textExtent("0123456789012345678901234")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          Long id;
          if (message.getSenderId().equals(accountId)) {
            id = message.getReceipientId();
          }
          else {
            id = message.getSenderId();
          }
          InspectAccountDialog.show(id, nxt, engine, userService, sync,
                contactsService);
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          if (message.getSenderId().equals(accountId)) {
            return message.getReceipientId();
          }
          return message.getSenderId();
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = Convert.toUnsignedLong((Long) getCellValue(element));
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare(((Long) v1), ((Long) v2));
        }
      }).build();

  final IGenericTableColumn columnID = new GenericTableColumnBuilder(
      "Transaction").align(SWT.LEFT).textExtent("12345678901234567890123")
      .activate(new ICellActivateHandler() {

        @Override
        public void activate(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          InspectTransactionDialog.show(message.getId(), nxt, engine,
              userService, sync, contactsService);
        }
      }).provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          return message.getId();
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

  final IGenericTableColumn columnText = new GenericTableColumnBuilder(
      "Subject").align(SWT.LEFT)
      .textExtent("Re. Re. Re. Re. Re. Re. Re. Re. Re.")
      .provider(new ICellDataProvider() {

        @Override
        public Object getCellValue(Object element) {
          MessageWrapper message = (MessageWrapper) element;
          String text = message.getMessage();
          return text.replaceAll("\\r\\n|\\r|\\n", " ");
        }

        @Override
        public void getCellData(Object element, Object[] data) {
          data[TEXT] = getCellValue(element);
        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare(((String) v1), ((String) v2));
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

      if (accountId == null)
        return;

      account = Account.getAccount(accountId);
      IUser user = userService.findUser(accountId);
      if (user != null) {
        secretPhrase = user.getAccount().getPrivateKey();
      }

      int timestamp = 0;
      Boolean orderAscending = Boolean.FALSE;
      Object referencedTransaction = null;
      TransactionType[] recipientTypes = { TransactionType.Messaging.ARBITRARY_MESSAGE };
      TransactionType[] senderTypes = { TransactionType.Messaging.ARBITRARY_MESSAGE };

      this.list = TransactionDB.getTransactions(accountId, recipientTypes,
          senderTypes, timestamp, orderAscending, referencedTransaction,
          ((MessagingViewer) viewer).nxt);
      this.currentPage = 1;
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

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
            if (t.getType().equals(TransactionType.Messaging.ARBITRARY_MESSAGE)) {
              pending.add(t);
            }
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

      /* Turn all Transaction objects into MessageWrapper objects */
      List<MessageWrapper> elements = new ArrayList<MessageWrapper>();
      for (ITransaction t : transactions) {
        elements.add(new MessageWrapper(t.getNative(), account, secretPhrase));
      }

      logger.info("getElements returns ELEMENTS.size=" + elements.size());
      return elements.toArray(new Object[elements.size()]);
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
  private Account account = null;
  private String secretPhrase = null;

  /* Subjects can be created by Apache StringUtils abbreviate */

  public MessagingViewer(Composite parent, Long accountId,
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
        return 99; // Not used
      }

      @Override
      public IGenericTableColumn getDefaultSortColumn() {
        return null; // Not used
      }

      @Override
      public IStructuredContentProvider getContentProvider() {
        return contentProvider;
      }

      @Override
      public IGenericTableColumn[] getColumns() {
        return new IGenericTableColumn[] { columnType, columnType2, columnDate,
            columnAccount, columnID, columnText };
      }
    });
    setInput(accountId);
	}

}
 