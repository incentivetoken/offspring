package com.dgex.offspring.ui.controls;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
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
import com.dgex.offspring.swt.table.GenerericTreeViewer;
import com.dgex.offspring.swt.table.GenericTableColumnBuilder;
import com.dgex.offspring.swt.table.ICellDataProvider;
import com.dgex.offspring.swt.table.IGenericTable;
import com.dgex.offspring.swt.table.IGenericTableColumn;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MessagingTreeViewer extends GenerericTreeViewer {

  static Logger logger = Logger.getLogger(MessagingTreeViewer.class);

  static final SimpleDateFormat dateFormat = new SimpleDateFormat(
      "dd MMM yy H:mm:ss");
  static final Image MESSAGE_RECEIVED = Images.getImage("bullet_go.png");
  static final Image MESSAGE_SEND = Images.getImage("resultset_previous.png");
  static final Image ENCRYPTED_OPENED = Images.getImage("lock_open.png");
  static final Image ENCRYPTED_LOCKED = Images.getImage("lock_delete.png");
  static final Image NO_ENCRYPTION = Images.getImage("information.png");
  static final Image UNKNOWN_MESSAGE = Images.getImage("page_white_text.png");

  static final String EMPTY_STRING = "";

  /* Returns TRUE for messsage send and FALSE for message received */
  final IGenericTableColumn columnMain = new GenericTableColumnBuilder(" ")
      .align(SWT.LEFT).textExtent("###############################")
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
          MessageWrapper message = (MessageWrapper) element;
          StringBuilder sb = new StringBuilder();
          boolean received = message.getReceipientId().equals(accountId);
          boolean[] recent = new boolean[] { false };

          sb.append(received ? "[IN] " : "[OUT] ");
          sb.append("[")
              .append(
                  formatToYesterdayOrToday((Date) getCellValue(element), recent))
              .append("] ");
          sb.append(" (")
              .append(
                  Convert.toUnsignedLong(received ? message.getSenderId()
                      : message.getReceipientId())).append(") ");
          sb.append(message.getMessage());

          if (message.isMessage()) {
            if (message.isEncrypted()) {
              if (secretPhrase == null) {
                data[IMAGE] = ENCRYPTED_LOCKED;
              }
              else {
                data[IMAGE] = ENCRYPTED_OPENED;
              }
            }
            else {
              data[IMAGE] = NO_ENCRYPTION;
            }
          }
          else {
            data[IMAGE] = UNKNOWN_MESSAGE;
          }

          data[TEXT] = sb.toString().replaceAll("\\r\\n|\\r|\\n", " ");

          if (!received) {
            // messages we send
            data[FONT] = JFaceResources.getFontRegistry().getItalic("");
          }
          else if (recent[0]) {
            // messages received in last two days
            data[FONT] = JFaceResources.getFontRegistry().getBold("");
          }

        }

        @Override
        public int compare(Object v1, Object v2) {
          return CompareMe.compare(((Date) v1).getTime(), ((Date) v2).getTime());
        }
      }).build();

  final ITreeContentProvider contentProvider = new ITreeContentProvider() {

    @Override
    public void dispose() {}

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
      account = Account.getAccount(accountId);
      IUser user = userService.findUser(accountId);
      if (user != null) {
        secretPhrase = user.getAccount().getPrivateKey();
      }
    }

    @Override
    public Object[] getElements(Object inputElement) {
      if (accountId == null) {
        return new Object[0];
      }
      
      List<MessageWrapper> elements = getMessages(null);
      // logger.info("getElements returns ELEMENTS.size=" + elements.size());
      return elements.toArray(new Object[elements.size()]);
    }

    @Override
    public Object[] getChildren(Object parentElement) {
      MessageWrapper parent = (MessageWrapper) parentElement;
      List<MessageWrapper> elements = getMessages(parent.getId());
      // logger.info("getChildren returns ELEMENTS.size=" + elements.size());
      return elements.toArray(new Object[elements.size()]);
    }

    @Override
    public Object getParent(Object element) {
      return null;
    }

    @Override
    public boolean hasChildren(Object element) {
      MessageWrapper parent = (MessageWrapper) element;
      if (parent.isMessage()) {
        int count = getMessageCount(parent.getId());
        return count > 0;
      }
      return false;
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
  
  public MessagingTreeViewer(Composite parent, Long accountId,
      IContactsService contactsService, INxtService nxt, IStylingEngine engine,
      IUserService userService, UISynchronize sync) {
    super(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
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
        return new IGenericTableColumn[] { columnMain };
      }
    });
    setInput(accountId);
  }

  private int getMessageCount(Long referencedTransactionId) {
    int count = TransactionDB.getMessageTransactionCount(accountId, 0,
        referencedTransactionId == null ? Boolean.TRUE
            : referencedTransactionId, nxt);

    List<ITransaction> pending = new ArrayList<ITransaction>();
    addPendingTransactions(pending, referencedTransactionId);
    return count + pending.size();
  }

  private List<MessageWrapper> getMessages(Long referencedTransactionId) {
    List<ITransaction> transactions = TransactionDB.getMessageTransactions(
        accountId, 0, Boolean.FALSE,
        referencedTransactionId == null ? Boolean.TRUE
            : referencedTransactionId, nxt);

    addPendingTransactions(transactions, referencedTransactionId);

    List<MessageWrapper> elements = new ArrayList<MessageWrapper>();
    for (ITransaction t : transactions) {
      elements.add(new MessageWrapper(t.getNative(), account, secretPhrase));
    }
    return elements;
  }

  private void addPendingTransactions(List<ITransaction> transactions, Long referencedTransactionId) {
    List<Transaction> pending = new ArrayList<Transaction>();
    for (Transaction t : nxt.getPendingTransactions()) {
      if (accountId.equals(t.getSenderId())
          || accountId.equals(t.getRecipientId())) {
        if (t.getType().equals(TransactionType.Messaging.ARBITRARY_MESSAGE)) {
          if ((referencedTransactionId == null && t
              .getReferencedTransactionId() == null)
              || (referencedTransactionId != null && referencedTransactionId
                  .equals(t.getReferencedTransactionId()))) {
            pending.add(t);
          }
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

  public static String formatToYesterdayOrToday(Date date, boolean recent[]) {
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    Calendar today = Calendar.getInstance();
    Calendar yesterday = Calendar.getInstance();
    yesterday.add(Calendar.DATE, -1);
    DateFormat timeFormatter = new SimpleDateFormat("H:mm:ss");

    if (calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
        && calendar.get(Calendar.DAY_OF_YEAR) == today
            .get(Calendar.DAY_OF_YEAR)) {
      recent[0] = true;
      return "Today " + timeFormatter.format(date);
    }
    else if (calendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR)
        && calendar.get(Calendar.DAY_OF_YEAR) == yesterday
            .get(Calendar.DAY_OF_YEAR)) {
      recent[0] = true;
      return "Yesterday " + timeFormatter.format(date);
    }

    recent[0] = false;
    return dateFormat.format(date);
  }

}
