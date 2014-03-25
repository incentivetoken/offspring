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
import com.dgex.offspring.ui.controls.TransactionsControl;
import com.dgex.offspring.user.service.IUserService;

public class AccountTabFolder extends Composite {

  static Logger logger = Logger.getLogger(AccountTabFolder.class);
  private final Long accountId;
  private final TabFolder tabFolder;
  private final IUserService userService;
  private final IContactsService contactsService;

  private final TabItem transactionsTab;
  private final TabItem generatedBlocksTab;
  private final TabItem aliasesTab;
  private final TabItem assetsTab;
  // private final TabItem messagesTab;

  private TabItem selectedTab = null;

  private final TransactionsControl transactionsControl;
  private final GeneratedBlocksControl generatedBlocksControl;
  private final AliasControl aliasesControl;
  private final AssetsControl assetsControl;
  // private final MessagesControl messagesControl;

  private final Runnable lazyRefresh = new Runnable() {

    @Override
    public void run() {
      if (!isDisposed() && !getDisplay().isDisposed()) {
        refresh();
      }
    }
  };

  public AccountTabFolder(Composite parent, int style, Long accountId,
      INxtService nxt, IStylingEngine engine, IUserService userService,
      UISynchronize sync, IContactsService contactsService) {
    super(parent, style);
    this.accountId = accountId;
    this.userService = userService;
    this.contactsService = contactsService;
    setLayout(new FillLayout());
    tabFolder = new TabFolder(this, SWT.NONE);

    transactionsTab = new TabItem(tabFolder, SWT.NONE);
    transactionsTab.setText("Transactions");
    transactionsControl = new TransactionsControl(tabFolder, SWT.NONE,
        accountId, nxt, engine, userService, sync);
    transactionsTab.setControl(transactionsControl);

    generatedBlocksTab = new TabItem(tabFolder, SWT.NONE);
    generatedBlocksTab.setText("Blocks");
    generatedBlocksControl = new GeneratedBlocksControl(tabFolder, SWT.NONE,
        accountId, engine, nxt, userService, sync, contactsService);
    generatedBlocksTab.setControl(generatedBlocksControl);

    aliasesTab = new TabItem(tabFolder, SWT.NONE);
    aliasesTab.setText("Aliases");
    aliasesControl = new AliasControl(tabFolder, SWT.NONE, accountId, nxt,
        engine, userService, sync, contactsService);
    aliasesTab.setControl(aliasesControl);

    assetsTab = new TabItem(tabFolder, SWT.NONE);
    assetsTab.setText("Assets");
    assetsControl = new AssetsControl(tabFolder, SWT.NONE, accountId, nxt,
        userService, contactsService, sync, engine);
    assetsTab.setControl(assetsControl);

    // messagesTab = new TabItem(tabFolder, SWT.NONE);
    // messagesTab.setText("Messages");
    // messagesControl = new MessagesControl(tabFolder, SWT.NONE, user, nxt);
    // messagesTab.setControl(messagesControl);

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
    if (tab.equals(transactionsTab)) {
      transactionsControl.refresh();
    }
    else if (tab.equals(generatedBlocksTab)) {
      generatedBlocksControl.refresh();
    }
    else if (tab.equals(aliasesTab)) {
      aliasesControl.refresh();
    }
    // else if (tab.equals(messagesTab)) {
    // messagesControl.refresh();
    // }
    else if (tab.equals(assetsTab)) {
      assetsControl.refresh();
    }
  }

  public void lazyRefresh() {
    getDisplay().timerExec(-1, lazyRefresh);
    getDisplay().timerExec(3000, lazyRefresh);
  }
}
