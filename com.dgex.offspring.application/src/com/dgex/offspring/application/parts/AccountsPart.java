package com.dgex.offspring.application.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import nxt.Generator;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.application.ui.accounts.AccountButtonComposite;
import com.dgex.offspring.application.ui.accounts.AccountTotalsComposite;
import com.dgex.offspring.application.utils.Layouts;
import com.dgex.offspring.config.Clipboards;
import com.dgex.offspring.config.Commands;
import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.ITransaction;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.providers.utils.ExchangeRates;
import com.dgex.offspring.ui.AccountTabFolder;
import com.dgex.offspring.ui.SendMessageWizard;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;
import com.dgex.offspring.wallet.IWallet;

@SuppressWarnings("restriction")
public class AccountsPart {

  static Logger logger = Logger.getLogger(AccountsPart.class);

  @Inject
  private IStylingEngine engine;

  @Inject
  private IUserService userService;

  @Inject
  private INxtService nxt;

  @Inject
  private IWallet wallet;

  private final IContactsService contactsService = new ContactsService();

  @Inject
  private EHandlerService handlerService;

  @Inject
  private ECommandService commandService;

  @Inject
  private UISynchronize sync;

  private Display display = null;
  private Composite accountsComposite = null;
  private Composite transactionsComposite = null;
  private Composite mainComposite = null;
  private Composite topContent;
  private Composite topComposite;
  private Composite bottomLeftComposite;
  private Composite summaryLeftComposite;
  private Composite bottomRightComposite;

  private AccountTotalsComposite accountTotalsComposite = null;
  private AccountTabFolder activeAccountTabFolder = null;
  // private AccountSummaryComposite summaryMiddleComposite;
  private ScrolledComposite scrolledAccountsComposite = null;

  private final List<AccountButtonComposite> accountButtonComposites = new ArrayList<AccountButtonComposite>();
  private final List<AccountTabFolder> tabFolders = new ArrayList<AccountTabFolder>();

  private Label totalNxt = null;
  private Label totalBTC;
  private Label totalEUR;
  private Link addAccountLink;

  private long totalNxtBalanceValueNQT = 0l;
  private Double totalBTCBalanceValue = (double) 0;
  private Double totalEURBalanceValue = (double) 0;

  private final Runnable updateGeneratorDeadlineRunnable = new Runnable() {

    @Override
    public void run() {
      for (AccountButtonComposite ab : accountButtonComposites) {
        if (!ab.isDisposed()) {
          ab.updateGenerationDeadline();
        }
      }
      if (!display.isDisposed()) {
        display.timerExec(1000, this);
      }
    }
  };

  private Link sendMoneyLink;
  private Link issueAssetLink;
  private Link createAuthTokenLink;
  private Link lookupAccountLink;
  private Link lookupTransactionLink;
  private Link lookupBlockLink;
  private Link showTraderPerspectiveLink;
  private Link showBlockExplorerLink;
  private Link showMessagesLink;
  private Link sendEncryptedMessage;
  private Link sendPlainMessage;

  @Inject
  @Optional
  private void onUserCreated(
      @UIEventTopic(IUserService.TOPIC_USER_CREATED) IUser user) {
    addUser(user);
    updateTotalBalance();
  }

  @Inject
  @Optional
  private void onUserRemoved(
      @UIEventTopic(IUserService.TOPIC_USER_REMOVED) IUser user) {
    removeUser(user);
    updateTotalBalance();
  }

  @Inject
  @Optional
  private void onActiveUserChanged(
      @UIEventTopic(IUserService.TOPIC_ACTIVEUSER_CHANGED) IUser user) {
    if (user != null)
      setActiveUser(user);
  }

  @Inject
  @Optional
  private void onGenerationDeadline(
      @UIEventTopic(INxtService.TOPIC_GENERATION_DEADLINE) Generator generator) {

    logger.info("Generator DEADLINE " + generator);

    IUser user = findUser(generator);
    if (user != null) {
      AccountButtonComposite ab = findAccountButtonComposite(user);
      if (ab != null && !ab.isDisposed()) {
        ab.setGeneratorDeadline(generator.getDeadline());
      }
    }
  }

  @Inject
  @Optional
  private void onGeneratorStart(
      @UIEventTopic(INxtService.TOPIC_START_FORGING) Generator generator) {

    logger.info("Generator START " + generator);

    IUser user = findUser(generator);
    if (user != null) {
      AccountButtonComposite ab = findAccountButtonComposite(user);
      if (ab != null && !ab.isDisposed()) {
        ab.startForging(generator);
      }
    }
  }

  @Inject
  @Optional
  private void onGeneratorStop(
      @UIEventTopic(INxtService.TOPIC_STOP_FORGING) Generator generator) {

    logger.info("Generator STOP " + generator);

    IUser user = findUser(generator);
    if (user != null) {
      AccountButtonComposite ab = findAccountButtonComposite(user);
      if (ab != null && !ab.isDisposed()) {
        ab.stopForging(generator);
      }
    }
  }

  /*
   * Handler for both TOPIC_ADD_FILTERED_TRANSACTION and
   * TOPIC_ADD_UNCONFIRMED_TRANSACTION
   */
  private void addTransaction(ITransaction transaction) {
    IUser active = userService.getActiveUser();
    if (active != null) {
      IUser sender = userService.getUser(transaction.getSender());
      IUser receiver = userService.getUser(transaction.getReceiver());

      if (active.equals(sender) || active.equals(receiver)) {
        setActiveUserBalanceNQT(active.getAccount().getBalanceNQT());
        if (activeAccountTabFolder != null) {
          activeAccountTabFolder.lazyRefresh();
        }
      }
    }
  }

  @Inject
  @Optional
  private void onAddTransaction(
      @UIEventTopic(INxtService.TOPIC_ADD_FILTERED_TRANSACTION) ITransaction transaction) {
    addTransaction(transaction);
  }

  @Inject
  @Optional
  private void onAddUnconfirmedTransaction(
      @UIEventTopic(INxtService.TOPIC_ADD_FILTERED_UNCONFIRMED_TRANSACTION) ITransaction transaction) {
    addTransaction(transaction);
  }

  @Inject
  @Optional
  private void onRemoveUnconfirmedTransaction(
      @UIEventTopic(INxtService.TOPIC_REMOVE_FILTERED_UNCONFIRMED_TRANSACTION) ITransaction transaction) {
    logger.warn("Handling of unconfirmed transactions not yet implemented");
  }

  @Inject
  @Optional
  private void onAddDoubleSpendingTransaction(
      @UIEventTopic(INxtService.TOPIC_ADD_DOUBLESPENDING_TRANSACTION) ITransaction transaction) {
    logger.warn("Handling of double spending transactions not yet implemented");
  }

  @Inject
  @Optional
  private void onBlockScanFinished(
      @UIEventTopic(INxtService.TOPIC_BLOCK_SCANNER_FINISHED) int dummy) {
    AccountTabFolder tabFolder = findAccountTabFolder(userService
        .getActiveUser());
    if (tabFolder != null) {
      tabFolder.refresh();
    }
  }

  @Inject
  @Optional
  private void onAccountUpdateBalance(
      @UIEventTopic(INxtService.TOPIC_ACCOUNT_UPDATE_BALANCE) IAccount account,
      IUserService userService) {

    IUser active = userService.getActiveUser();
    if (active != null) {

      IUser user = userService.getUser(account);
      if (user != null) {
        AccountButtonComposite c = findAccountButtonComposite(user);
        if (c != null) {
          c.setBalanceNQT(account.getBalanceNQT(),
              account.getUnconfirmedBalanceNQT());
        }
        if (user.equals(active)) {
          setActiveUserBalanceNQT(account.getBalanceNQT());
        }
      }
    }
    updateTotalBalance();
  }

  @Inject
  @Optional
  private void handleAccountUpdateUnconfirmedBalance(
      @UIEventTopic(INxtService.TOPIC_ACCOUNT_UPDATE_UNCONFIRMED_BALANCE) IAccount account) {

  }

  private void updateTotalBalance() {
    long total = 0l;
    for (IUser user : userService.getUsers())
      total += user.getAccount().getBalanceNQT();
    accountTotalsComposite.setTotalNQT(total);
  }

  @Focus
  public void onFocus() {
    mainComposite.setFocus();
  }

  @PreDestroy
  public void preDestroy() {
    display.timerExec(-1, updateGeneratorDeadlineRunnable);
  }

  @PostConstruct
  public void postConstruct(final Composite parent) {
    display = parent.getDisplay();
    display.timerExec(1000, updateGeneratorDeadlineRunnable);

    parent.setLayout(new FillLayout());
    parent.addControlListener(new ControlAdapter() {

      @Override
      public void controlResized(final ControlEvent e) {
        sync.asyncExec(new Runnable() {

          @Override
          public void run() {
            resizeScrolledAccountsComposite();
          }
        });
      }
    });

    mainComposite = new Composite(parent, SWT.NONE);
    mainComposite.setLayout(Layouts.Grid.create(2));
    mainComposite.setLayoutData(Layouts.Grid.fill());

    topComposite = new Composite(mainComposite, SWT.NONE);
    bottomLeftComposite = new Composite(mainComposite, SWT.NONE);
    bottomRightComposite = new Composite(mainComposite, SWT.NONE);

    GridData gd = new GridData();
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = false;
    // gd.heightHint = topRowHeight;xx
    gd.horizontalSpan = 2;
    topComposite.setLayoutData(gd);

    gd = new GridData(/* leftColumnWidth, SWT.DEFAULTxx */);
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = false;
    gd.grabExcessVerticalSpace = true;
    bottomLeftComposite.setLayoutData(gd);
    bottomRightComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
        true));

    topComposite.setLayout(new FillLayout());
    bottomLeftComposite.setLayout(new FillLayout());
    bottomRightComposite.setLayout(new FillLayout());

    topContent = createSummaryComposite(topComposite);

    /* Composite bottomLeftContent = */createAccountsComposite(bottomLeftComposite);
    /* Composite bottomRightContent = */createTransactionsComposite(bottomRightComposite);

    for (IUser user : userService.getUsers())
      addUser(user);

    updateTotalBalance();

    if (userService.getActiveUser() != null)
      setActiveUser(userService.getActiveUser());

    sync.asyncExec(new Runnable() {

      @Override
      public void run() {
        resizeScrolledAccountsComposite();
      }
    });
  }

  private Composite createTransactionsComposite(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FillLayout());

    transactionsComposite = new Composite(composite, SWT.NONE);
    transactionsComposite.setLayout(new StackLayout());

    return composite;
  }

  private Composite createAccountsComposite(Composite parent) {
    final Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new FillLayout());

    /* must resize */
    composite.addControlListener(new ControlListener() {

      @Override
      public void controlResized(ControlEvent e) {
        Point size = composite.getSize();
        if (size.x != summaryLeftComposite
            .computeSize(SWT.DEFAULT, SWT.DEFAULT).x) {
          GridDataFactory.fillDefaults().hint(size.x, SWT.DEFAULT)
              .applyTo(summaryLeftComposite);
          topContent.layout();
        }
      }

      @Override
      public void controlMoved(ControlEvent e) {}
    });

    scrolledAccountsComposite = new ScrolledComposite(composite, SWT.V_SCROLL);
    accountsComposite = new Composite(scrolledAccountsComposite, SWT.NONE);
    accountsComposite.setLayout(Layouts.Grid.create(1, 2, 4));

    scrolledAccountsComposite.setContent(accountsComposite);
    scrolledAccountsComposite.setMinSize(accountsComposite.computeSize(
        SWT.DEFAULT, SWT.DEFAULT));
    scrolledAccountsComposite.setExpandHorizontal(true);
    scrolledAccountsComposite.setExpandVertical(true);

    GridData gd = new GridData();
    // gd.grabExcessHorizontalSpace = true;
    // // gd.widthHint = leftColumnWidth - 8;xx
    // gd.horizontalIndent = 6;
    //
    // addAccountLink = new Link(accountsComposite, SWT.NONE);
    // addAccountLink
    //        .setText("<A>" + Messages.AccountsPart_label_add_account + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    // addAccountLink.setLayoutData(gd);
    // addAccountLink.addSelectionListener(new SelectionAdapter() {
    //
    // @Override
    // public void widgetSelected(SelectionEvent e) {
    // ParameterizedCommand cmd = commandService.createCommand(
    // Commands.CREATE_ACCOUNT_COMMAND, null);
    // handlerService.executeHandler(cmd);
    // }
    // });

    gd = new GridData();
    gd.grabExcessHorizontalSpace = true;
    // gd.widthHint = leftColumnWidth - 8;xx

    accountTotalsComposite = new AccountTotalsComposite(accountsComposite,
        SWT.NONE, engine);
    accountTotalsComposite.setLayoutData(gd);

    return composite;
  }

  private void resizeScrolledAccountsComposite() {
    if (scrolledAccountsComposite == null
        || scrolledAccountsComposite.isDisposed() || accountsComposite == null)
      return;
    scrolledAccountsComposite.setMinSize(accountsComposite.computeSize(
        SWT.DEFAULT, SWT.DEFAULT));
  }

  private Composite createSummaryComposite(final Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(6).spacing(15, 0)
        .margins(15, 15).applyTo(composite);

    summaryLeftComposite = new Composite(composite, SWT.NONE);
    // summaryLeftComposite.setBackground(Display.getCurrent().getSystemColor(
    // SWT.COLOR_YELLOW));
    GridDataFactory.fillDefaults().grab(false, true)
        .applyTo(summaryLeftComposite);

    // summaryMiddleComposite = new AccountSummaryComposite(composite,
    // SWT.NONE);
    // GridDataFactory.fillDefaults().grab(true, true)
    // .applyTo(summaryMiddleComposite);

    Composite rightComposite_1 = new Composite(composite, SWT.NONE);
    GridDataFactory.fillDefaults().grab(true, true).applyTo(rightComposite_1);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(rightComposite_1);

    Composite rightComposite_2 = new Composite(composite, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(rightComposite_2);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(rightComposite_2);

    Composite rightComposite_3 = new Composite(composite, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(rightComposite_3);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(rightComposite_3);

    Composite rightComposite_4 = new Composite(composite, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(rightComposite_4);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(rightComposite_4);

    Composite rightComposite_5 = new Composite(composite, SWT.NONE);
    GridDataFactory.fillDefaults().grab(false, true).applyTo(rightComposite_5);
    GridLayoutFactory.fillDefaults().numColumns(1).applyTo(rightComposite_5);

    /* LEFT */

    GridLayoutFactory.fillDefaults().numColumns(1)
        .applyTo(summaryLeftComposite);

    totalNxt = new Label(summaryLeftComposite, SWT.NONE);
    totalNxt.setText(Messages.AccountsPart_label_total_nxt_default);
    engine.setClassname(totalNxt, "totalNXT"); //$NON-NLS-1$
    GridDataFactory.swtDefaults().grab(true, true).applyTo(totalNxt);

    totalBTC = new Label(summaryLeftComposite, SWT.NONE);
    totalBTC.setText(Messages.AccountsPart_label_total_btc_default);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(totalBTC);

    totalEUR = new Label(summaryLeftComposite, SWT.NONE);
    totalEUR.setText(Messages.AccountsPart_label_total_eur_default);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(totalEUR);

    /* MIDDLE */

    // ...

    /* RIGHT (1) */

    sendMoneyLink = new Link(rightComposite_2, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(sendMoneyLink);
    sendMoneyLink
        .setText("<A>" + Messages.AccountsPart_label_send_money + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    sendMoneyLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.SEND_MONEY_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    issueAssetLink = new Link(rightComposite_2, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(issueAssetLink);
    issueAssetLink.setText("<A>" + "Issue Asset" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    issueAssetLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.ISSUE_ASSET_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    createAuthTokenLink = new Link(rightComposite_2, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(createAuthTokenLink);
    createAuthTokenLink
        .setText("<A>" + Messages.AccountsPart_label_authentication_token + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    createAuthTokenLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.AUTH_TOKEN_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    /* RIGHT (2) */

    lookupAccountLink = new Link(rightComposite_3, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(lookupAccountLink);
    lookupAccountLink.setText("<A>" + "Lookup Account" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    lookupAccountLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.LOOKUP_ACCOUNT_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    lookupTransactionLink = new Link(rightComposite_3, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true)
        .applyTo(lookupTransactionLink);
    lookupTransactionLink.setText("<A>" + "Lookup Transaction" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    lookupTransactionLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.LOOKUP_TRANSACTION_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    lookupBlockLink = new Link(rightComposite_3, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true).applyTo(lookupBlockLink);
    lookupBlockLink.setText("<A>" + "Lookup Block" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    lookupBlockLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.LOOKUP_BlOCK_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    /* RIGHT (3) */

    addAccountLink = new Link(rightComposite_4, SWT.NONE);
    addAccountLink
        .setText("<A>" + Messages.AccountsPart_label_add_account + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GridDataFactory.swtDefaults().grab(true, true).applyTo(addAccountLink);
    addAccountLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.CREATE_ACCOUNT_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    showTraderPerspectiveLink = new Link(rightComposite_4, SWT.NONE);
    showTraderPerspectiveLink.setText("<A>" + "Asset Exchange" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GridDataFactory.swtDefaults().grab(true, true)
        .applyTo(showTraderPerspectiveLink);
    showTraderPerspectiveLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.SHOW_TRADER_PERSPECTIVE_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    showBlockExplorerLink = new Link(rightComposite_4, SWT.NONE);
    showBlockExplorerLink.setText("<A>" + "Block Explorer" + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GridDataFactory.swtDefaults().grab(true, true)
        .applyTo(showBlockExplorerLink);
    showBlockExplorerLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        ParameterizedCommand cmd = commandService.createCommand(
            Commands.SHOW_NXT_PERSPECTIVE_COMMAND, null);
        handlerService.executeHandler(cmd);
      }
    });

    /* RIGHT (4) */

    showMessagesLink = new Link(rightComposite_5, SWT.NONE);
    showMessagesLink.setText("<A>Show Messages</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GridDataFactory.swtDefaults().grab(true, true).applyTo(showMessagesLink);
    showMessagesLink.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (activeAccountTabFolder != null) {
         activeAccountTabFolder.showMessagesTab();
        }
      }
    });

    sendEncryptedMessage = new Link(rightComposite_5, SWT.NONE);
    sendEncryptedMessage.setText("<A>Send Encrypted Message</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GridDataFactory.swtDefaults().grab(true, true)
        .applyTo(sendEncryptedMessage);
    sendEncryptedMessage.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        for (IUser user : userService.getUsers()) {
          if (!user.getAccount().isReadOnly()) {
            Shell shell = parent.getShell();
            if (shell != null) {
              while (shell.getParent() != null) {
                shell = shell.getParent().getShell();
              }
            }
            new WizardDialog(shell, new SendMessageWizard(userService, nxt,
                null, null, true)).open();
            break;
          }
        }
      }
    });

    sendPlainMessage = new Link(rightComposite_5, SWT.NONE);
    sendPlainMessage.setText("<A>Send Plain Message</A>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    GridDataFactory.swtDefaults().grab(true, true).applyTo(sendPlainMessage);
    sendPlainMessage.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        for (IUser user : userService.getUsers()) {
          if (!user.getAccount().isReadOnly()) {
            Shell shell = parent.getShell();
            if (shell != null) {
              while (shell.getParent() != null) {
                shell = shell.getParent().getShell();
              }
            }
            new WizardDialog(shell, new SendMessageWizard(userService, nxt,
                null, null, false)).open();
            break;
          }
        }
      }
    });

    /* Context Menus */

    Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
    totalNxt.setMenu(menu);
    totalBTC.setMenu(menu);
    totalEUR.setMenu(menu);
    totalNxt.setToolTipText(Messages.AccountsPart_tooltip_right_click);
    totalBTC.setToolTipText(Messages.AccountsPart_tooltip_right_click);
    totalEUR.setToolTipText(Messages.AccountsPart_tooltip_right_click);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountsPart_label_copy_nxt_amount);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(),
            Utils.quantToString(totalNxtBalanceValueNQT, 8));
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountsPart_label_copy_btc_amount);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(),
            Double.toString(totalBTCBalanceValue));
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountsPart_label_copy_eur_amount);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(),
            Double.toString(totalEURBalanceValue));
      }
    });

    return composite;
  }

  private void removeUser(IUser user) {

    /* remove account button */
    AccountButtonComposite composite = null;
    for (AccountButtonComposite c : accountButtonComposites) {
      if (user.equals(c.getUser())) {
        composite = c;
        break;
      }
    }
    if (composite != null) {
      accountButtonComposites.remove(composite);
      composite.dispose();
    }

    /* remove tabfolder */
    AccountTabFolder tabFolder = findAccountTabFolder(user);
    if (tabFolder != null) {
      tabFolders.remove(tabFolder);
      tabFolder.dispose();
    }

    accountsComposite.pack();
    accountsComposite.layout();
  }

  private void addUser(IUser user) {
    GridData gd = new GridData();
    gd.grabExcessHorizontalSpace = true;

    AccountButtonComposite c = new AccountButtonComposite(accountsComposite,
        user, userService, wallet, engine, nxt);
    c.pack();
    c.setLayoutData(gd);
    c.setBalanceNQT(user.getAccount().getBalanceNQT(), user.getAccount()
        .getUnconfirmedBalanceNQT());
    accountButtonComposites.add(c);

    c.moveAbove(accountTotalsComposite);

    accountsComposite.layout(new Control[] { c });

    AccountTabFolder tabFolder = new AccountTabFolder(transactionsComposite,
        SWT.NONE, user.getAccount().getId(), nxt, engine, userService, sync,
        contactsService);
    tabFolders.add(tabFolder);
  }

  private void setActiveUser(IUser user) {
    boolean visible = user.getAccount().getBalanceNQT() > 0l;

    AccountTabFolder tabFolder = findAccountTabFolder(user);
    if (tabFolder != null) {
      StackLayout stack = (StackLayout) transactionsComposite.getLayout();
      stack.topControl = tabFolder;
      activeAccountTabFolder = tabFolder;
      activeAccountTabFolder.refresh();
    }
    else
      logger.warn("Could not find BuyOrderTableViewer for user " + user); //$NON-NLS-1$

    for (AccountButtonComposite v : accountButtonComposites)
      v.setActive(false);

    AccountButtonComposite comp = findAccountButtonComposite(user);
    if (comp != null) {
      comp.setBalanceNQT(user.getAccount().getBalanceNQT(), user.getAccount()
          .getUnconfirmedBalanceNQT());
      comp.setActive(true);
    }

    // summaryMiddleComposite.setActiveUser(user);

    setActiveUserBalanceNQT(user.getAccount().getBalanceNQT());

    topComposite.pack();
    bottomLeftComposite.pack();
    transactionsComposite.layout();
    mainComposite.layout();
  }

  private void setActiveUserBalanceNQT(long balance) {
    totalNxtBalanceValueNQT = balance;
    totalBTCBalanceValue = ExchangeRates.convertNqtToBtc(balance);
    totalEURBalanceValue = ExchangeRates.convertNqtToEur(balance);

    String nxtStr = "NXT " + Utils.quantToString(balance, 8);  //$NON-NLS-1$
    String btcStr = "BTC " + String.format("%.5f", totalBTCBalanceValue);  //$NON-NLS-1$  //$NON-NLS-1$
    String eurStr = "EUR " + String.format("%.2f", totalEURBalanceValue);   //$NON-NLS-1$  //$NON-NLS-1$

    // logger.info("NXT = " + nxtStr);
    // logger.info("BTC = " + btcStr);
    // logger.info("EUR = " + eurStr);

    totalNxt.setText(nxtStr); //$NON-NLS-1$
    totalBTC.setText(btcStr); //$NON-NLS-1$
    totalEUR.setText(eurStr); //$NON-NLS-1$
    summaryLeftComposite.layout();
  }

  private AccountButtonComposite findAccountButtonComposite(IUser user) {
    for (AccountButtonComposite v : accountButtonComposites) {
      if (v.getUser().equals(user)) {
        return v;
      }
    }
    return null;
  }

  private AccountTabFolder findAccountTabFolder(IUser user) {
    for (AccountTabFolder folder : tabFolders) {
      if (folder.getAccountId().equals(user.getAccount().getId())) {
        return folder;
      }
    }
    return null;
  }

  private IUser findUser(Generator generator) {
    for (IUser user : userService.getUsers()) {
      byte[] publicKey = user.getAccount().getPublicKey();
      if (publicKey != null) {
        if (Arrays.equals(publicKey, generator.getPublicKey())) {
          return user;
        }
      }
    }
    return null;
  }

}