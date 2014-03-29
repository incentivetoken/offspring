package com.dgex.offspring.ui;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.ui.controls.AliasControl;
import com.dgex.offspring.ui.controls.AssetsControl;
import com.dgex.offspring.ui.controls.GeneratedBlocksControl;
import com.dgex.offspring.ui.controls.MessagingControl;
import com.dgex.offspring.ui.controls.TransactionsControl;
import com.dgex.offspring.user.service.IUserService;

public class AccountTabFolder extends Composite {

  static Logger logger = Logger.getLogger(AccountTabFolder.class);
  private final Long accountId;
  private final TabFolder tabFolder;
  private final IUserService userService;
  private final IContactsService contactsService;
  private final INxtService nxt;
  private final IStylingEngine engine;
  private final UISynchronize sync;

  private final TabItem transactionsTab;
  private final TabItem generatedBlocksTab;
  private final TabItem aliasesTab;
  private final TabItem assetsTab;
  private final TabItem messagesTab;

  private TabItem selectedTab = null;

  private TransactionsControl transactionsControl = null;
  private GeneratedBlocksControl generatedBlocksControl = null;
  private AliasControl aliasesControl = null;
  private AssetsControl assetsControl = null;
  private MessagingControl messagesControl = null;

  private final Runnable lazyRefresh = new Runnable() {

    @Override
    public void run() {
      if (!isDisposed() && !getDisplay().isDisposed()) {
        refresh();
      }
    }
  };



  public AccountTabFolder(Composite parent, int style, final Long accountId,
      final INxtService nxt, final IStylingEngine engine,
      final IUserService userService, final UISynchronize sync,
      final IContactsService contactsService) {
    super(parent, style);
    this.accountId = accountId;
    this.userService = userService;
    this.contactsService = contactsService;
    this.nxt = nxt;
    this.engine = engine;
    this.sync = sync;
    setLayout(new FillLayout());
    tabFolder = new TabFolder(this, SWT.NONE);

    transactionsTab = new TabItem(tabFolder, SWT.NONE);
    transactionsTab.setText("Transactions");

    generatedBlocksTab = new TabItem(tabFolder, SWT.NONE);
    generatedBlocksTab.setText("Blocks");

    aliasesTab = new TabItem(tabFolder, SWT.NONE);
    aliasesTab.setText("Aliases");

    assetsTab = new TabItem(tabFolder, SWT.NONE);
    assetsTab.setText("Assets");

    messagesTab = new TabItem(tabFolder, SWT.NONE);
    messagesTab.setText("Messages");

    tabFolder.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        TabItem[] items = tabFolder.getSelection();
        if (items != null && items.length > 0) {
          TabItem tab = items[0];
          if (selectedTab == null || !selectedTab.equals(tab)) {
            selectedTab = tab;
            refresh();
          }
        }
      }
    });
  }

  private void createTabContents(TabItem tab) {
    if (tab.equals(transactionsTab) && transactionsControl == null) {
      transactionsControl = new TransactionsControl(tabFolder, SWT.NONE,
          accountId, nxt, engine, userService, sync);
      transactionsTab.setControl(transactionsControl);
    }
    else if (tab.equals(generatedBlocksTab) && generatedBlocksControl == null) {
      generatedBlocksControl = new GeneratedBlocksControl(tabFolder, SWT.NONE,
          accountId, engine, nxt, userService, sync, contactsService);
      generatedBlocksTab.setControl(generatedBlocksControl);
    }
    else if (tab.equals(aliasesTab) && aliasesControl == null) {
      aliasesControl = new AliasControl(tabFolder, SWT.NONE, accountId, nxt,
          engine, userService, sync, contactsService);
      aliasesTab.setControl(aliasesControl);
    }
    else if (tab.equals(messagesTab) && messagesControl == null) {
      messagesControl = new MessagingControl(tabFolder, SWT.NONE, accountId,
          nxt, engine, userService, sync);
      messagesTab.setControl(messagesControl);
    }
    else if (tab.equals(assetsTab) && assetsControl == null) {
      assetsControl = new AssetsControl(tabFolder, SWT.NONE, accountId, nxt,
          userService, contactsService, sync, engine);
      assetsTab.setControl(assetsControl);
    }
  }

  public Long getAccountId() {
    return accountId;
  }

  public void refresh() {
    if (tabFolder == null || tabFolder.isDisposed())
      return;

    TabItem[] items = tabFolder.getSelection();
    if (items == null || items.length == 0)
      return;

    TabItem tab = items[0];

    createTabContents(tab);

    if (tab.equals(transactionsTab) && transactionsControl != null) {
      transactionsControl.refresh();
    }
    else if (tab.equals(generatedBlocksTab) && generatedBlocksControl != null) {
      generatedBlocksControl.refresh();
    }
    else if (tab.equals(aliasesTab) && aliasesControl != null) {
      aliasesControl.refresh();
    }
    else if (tab.equals(messagesTab) && messagesControl != null) {
      messagesControl.refresh();
    }
    else if (tab.equals(assetsTab) && assetsControl != null) {
      assetsControl.refresh();
    }
  }

  public void lazyRefresh() {
    logger.info("lazyRefresh");
    getDisplay().timerExec(-1, lazyRefresh);
    getDisplay().timerExec(3000, lazyRefresh);
  }
}
