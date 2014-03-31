package com.dgex.offspring.ui.messaging;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nxt.Account;
import nxt.Alias;
import nxt.Constants;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.ui.SendMessageWizard;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MessagingControl extends Composite {

  static Logger logger = Logger.getLogger(MessagingControl.class);
  static String ID_KEY = "com.dgex.offspring.ui.controls.MessagingControl.ID_KEY";
  static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
  static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yy H:mm:ss");

  private final INxtService nxt;
  private final IStylingEngine engine;
  private final IUserService userService;
  private final UISynchronize sync;
  private final Long accountId;

  private Button replyButton;
  private Text messageText;
  private Composite recipientsComposite;
  private Composite topComposite;
  private Composite bottomComposite;
  private boolean accountReadonly = true;
  private Label statusLabel;
  private MessagingTreeViewer treeViewer;

  interface AccountLinkListener {

    void remove(AccountLink link);

    void click(AccountLink link);

    void requestLayout(AccountLink link);
  }

  static class AccountLink {

    private final String id;
    private final AccountLinkListener listener;
    private Control control = null;

    public AccountLink(String id, AccountLinkListener listener) {
      if (id == null)
        throw new RuntimeException("Id cannot be null");
      this.id = id.trim().toLowerCase();
      this.listener = listener;
    }

    public void create(Composite parent) {
      control = createControl(parent);
      control.setData(ID_KEY, id);
      control.pack();
      // createMenu(control);
      registerMouseEvents(control);
    }

    public String getId() {
      return id;
    }

    public Control getControl() {
      return control;
    }

    private Control createControl(Composite parent) {
      Account account = getAccount();
      if (account == null) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(id);
        logger.info("Lable text=" + id);
        return label;
      }
      Link link = new Link(parent, SWT.NONE);
      link.setText("<A>" + id + "</A>");
      logger.info("Link text=<A>" + id + "</A>");
      link.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(SelectionEvent e) {
          listener.click(AccountLink.this);
        }
      });
      return link;
    }

    // private void createMenu(Control control) {
    // Menu contextMenu = new Menu(control);
    // control.setMenu(contextMenu);
    // MenuItem menuItem = new MenuItem(contextMenu, SWT.PUSH);
    // menuItem.setText("Remove");
    // menuItem.addSelectionListener(new SelectionAdapter() {
    //
    // @Override
    // public void widgetSelected(SelectionEvent e) {
    // listener.remove(AccountLink.this);
    // }
    // });
    // }

    private void registerMouseEvents(final Control control) {
      control.addMouseTrackListener(new MouseTrackListener() {

        @Override
        public void mouseHover(MouseEvent e) {
          control.setFont(JFaceResources.getFontRegistry().getBold(""));
          doLayout(control);
        }

        @Override
        public void mouseExit(MouseEvent e) {
          control.setFont(null);
          doLayout(control);
        }

        @Override
        public void mouseEnter(MouseEvent e) {
          control.setFont(JFaceResources.getFontRegistry().getBold(""));
          doLayout(control);
        }
      });
    }

    private void doLayout(Control control) {
      listener.requestLayout(this);
    }

    public Account getAccount() {
      Account account = getAccount(id);
      if (account == null) {
        Alias alias = Alias.getAlias(id);
        if (alias != null) {
          return getAccount(alias.getURI());
        }
      }
      return account;
    }

    private Account getAccount(String accountId) {
      try {
        return Account.getAccount(Convert.parseUnsignedLong(accountId));
      }
      catch (NullPointerException e) {}
      catch (NumberFormatException e) {}
      return null;
    }
  }

  private final AccountLinkListener accountLinkListener = new AccountLinkListener() {

    @Override
    public void remove(AccountLink link) {
      // StringBuilder sb = new StringBuilder();
      // for (Control control : recipientsComposite.getChildren()) {
      // String id = (String)
      // ((Composite)control).getChildren()[0].getData(ID_KEY);
      // if (id.compareTo(link.getId()) != 0) {
      // sb.append(id).append(" ");
      // }
      // }
      // accountText.setText(sb.toString().trim());
    }

    @Override
    public void click(AccountLink link) {
      Account account = link.getAccount();
      if (account != null) {
        InspectAccountDialog.show(account.getId(), nxt, engine, userService,
            sync,
              ContactsService.getInstance());
      }
    }

    @Override
    public void requestLayout(AccountLink link) {
      link.getControl().pack();
      link.getControl().getParent().pack();
      recipientsComposite.pack();
      messageText.pack();
      bottomComposite.layout(true);
    }
  };
  
  public MessagingControl(Composite parent, int style, final Long accountId,
      final INxtService nxt, IStylingEngine engine,
      final IUserService userService,
      UISynchronize sync) {
    super(parent, style);
    this.nxt = nxt;
    this.engine = engine;
    this.userService = userService;
    this.sync = sync;
    this.accountId = accountId;

    IUser user = userService.findUser(accountId);
    if (user != null) {
      this.accountReadonly = user.getAccount().isReadOnly();
    }

    createControls();

    treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) treeViewer
            .getSelection();
        Object element = selection.getFirstElement();
        if (element instanceof MessageWrapper) {
          MessageWrapper message = (MessageWrapper) element;

          /* remove the old controls */
          while (recipientsComposite.getChildren().length > 0) {
            recipientsComposite.getChildren()[0].dispose();
          }

          Long senderId = message.getSenderId();
          Long receiverId = message.getReceipientId();

          Label label = new Label(recipientsComposite, SWT.NONE);
          label.setFont(JFaceResources.getFontRegistry().getBold(""));
          Date date = new Date(((message.getTimestamp()) * 1000L)
              + (Constants.EPOCH_BEGINNING - 500L));
          label.setText(formatToYesterdayOrToday(date,
              new boolean[1]) + " ");

          label = new Label(recipientsComposite, SWT.NONE);
          label.setText("Sender: ");
          // label.setFont(JFaceResources.getFontRegistry().getBold(""));

          if (!senderId.equals(accountId)) {
            AccountLink link = new AccountLink(
                Convert.toUnsignedLong(senderId), accountLinkListener);
            link.create(recipientsComposite);
          }
          else {
            label = new Label(recipientsComposite, SWT.NONE);
            label.setText("YOU ");
            label.setFont(JFaceResources.getFontRegistry().getBold(""));
          }

          label = new Label(recipientsComposite, SWT.NONE);
          label.setText(" Receiver: ");
          // label.setFont(JFaceResources.getFontRegistry().getBold(""));

          if (!receiverId.equals(accountId)) {
            AccountLink link = new AccountLink(Convert
                .toUnsignedLong(receiverId), accountLinkListener);
            link.create(recipientsComposite);
          }
          else {
            label = new Label(recipientsComposite, SWT.NONE);
            label.setText("YOU ");
            label.setFont(JFaceResources.getFontRegistry().getBold(""));
          }

          recipientsComposite.pack(true);
          messageText.setText(message.getMessage());
          bottomComposite.layout();
        }
      }
    });

    Menu contextMenu = new Menu(treeViewer.getTree());
    treeViewer.getTree().setMenu(contextMenu);

    MenuItem item = new MenuItem(contextMenu, SWT.PUSH);
    item.setText("Reply To Sender");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) treeViewer
            .getSelection();
        Object element = selection.getFirstElement();
        if (element instanceof MessageWrapper) {
          MessageWrapper message = (MessageWrapper) element;
          Long transactionId = message.getId();
          openReplyDialog(message.getSenderId(), transactionId);
          refresh();
        }
      }
    });

    item = new MenuItem(contextMenu, SWT.PUSH);
    item.setText("Reply To Recipient");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) treeViewer
            .getSelection();
        Object element = selection.getFirstElement();
        if (element instanceof MessageWrapper) {
          MessageWrapper message = (MessageWrapper) element;
          Long transactionId = message.getId();
          openReplyDialog(message.getReceipientId(), transactionId);
          refresh();
        }
      }
    });
  }

  private void openReplyDialog(Long recipientId, Long transactionId) {
    Shell shell = getShell();
    if (shell != null) {
      while (shell.getParent() != null) {
        shell = shell.getParent().getShell();
      }
    }
    WizardDialog dialog = new WizardDialog(shell, new SendMessageWizard(
        userService, nxt, recipientId, transactionId));
    dialog.open();
  }

  private void createControls() {
    setLayout(new FillLayout());

    SashForm form = new SashForm(this, SWT.VERTICAL);
    form.setLayout(new FillLayout());

    topComposite = new Composite(form, SWT.NONE);
    topComposite.setLayout(new FillLayout());

    bottomComposite = new Composite(form, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(2).spacing(5, 0).margins(0, 0)
        .applyTo(bottomComposite);

    treeViewer = new MessagingTreeViewer(topComposite, accountId, null, nxt,
        engine, userService, sync);

    recipientsComposite = new Composite(bottomComposite, SWT.NONE);
    GridDataFactory.fillDefaults().grab(true, false)
        .align(SWT.FILL, SWT.CENTER).applyTo(recipientsComposite);
    // recipientsComposite.setBackground(Colors.getColor(Colors.YELLOW));
    recipientsComposite.setLayout(new RowLayout());

    replyButton = new Button(bottomComposite, SWT.PUSH);
    replyButton.setText("Reply to this message");
    GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
        .exclude(accountReadonly)
        .applyTo(replyButton);
    replyButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) treeViewer
            .getSelection();
        Object element = selection.getFirstElement();
        if (element instanceof MessageWrapper) {
          MessageWrapper message = (MessageWrapper) element;
          Long transactionId = message.getId();
          Long recipientId;
          if (accountReadonly) {
            recipientId = message.getSenderId();
          }
          else {
            if (message.getSenderId().equals(accountId)) {
              recipientId = message.getReceipientId();
            }
            else {
              recipientId = message.getSenderId();
            }
          }
          openReplyDialog(recipientId, transactionId);
        }
      }
    });

    messageText = new Text(bottomComposite, SWT.MULTI | SWT.BORDER | SWT.WRAP
        | SWT.V_SCROLL);
    messageText.setEditable(false);

    GridDataFactory.defaultsFor(messageText).align(SWT.FILL, SWT.FILL)
        .grab(true, true).span(3, 1)
        .applyTo(messageText);

    form.setWeights(new int[] { 60, 30 });
  }

  public void refresh() {
    treeViewer.setInput(accountId);
    treeViewer.expandAll();
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
