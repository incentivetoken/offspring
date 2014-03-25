package com.dgex.offspring.ui;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.util.Convert;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.config.IContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class InspectAccountDialog extends TitleAreaDialog {

  private final List<Long> history = new ArrayList<Long>();
  private int historyCursor = 0;

  private Long accountId;
  private final INxtService nxt;
  private final IStylingEngine engine;
  private Control tabFolder = null;
  private Composite container;
  private Button previousButton;
  private Button nextButton;
  private final IUserService userService;
  private Button sendMessageButton;
  private Button sendMoneyButton;
  private final UISynchronize sync;
  private final IContactsService contactsService;

  static InspectAccountDialog INSTANCE = null;

  public InspectAccountDialog(Shell shell, Long accountId, INxtService nxt,
      IStylingEngine engine, IUserService userService, UISynchronize sync,
      IContactsService contactsService) {
    super(shell);
    this.nxt = nxt;
    this.engine = engine;
    this.accountId = accountId;
    this.userService = userService;
    this.sync = sync;
    this.contactsService = contactsService;
  }

  @Override
  protected void setShellStyle(int newShellStyle) {
    super.setShellStyle(SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE
        | SWT.RESIZE);
    setBlockOnOpen(false);
  }

  /**
   * Static method that opens a new dialog or switches the existing dialog to
   * another account id. The dialog shows back and forward buttons to navigate
   * between accounts inspected.
   * 
   * @param accountId
   * @return
   */
  public static void show(final Long accountId, final INxtService nxt,
      final IStylingEngine engine, final IUserService userService,
      final UISynchronize sync, final IContactsService contactsService) {

    sync.syncExec(new Runnable() {

      @Override
      public void run() {
        Shell shell = Display.getCurrent().getActiveShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }
        if (INSTANCE == null) {
          INSTANCE = new InspectAccountDialog(shell, accountId, nxt, engine,
              userService, sync, contactsService);
          INSTANCE.history.add(accountId);
          INSTANCE.historyCursor = 0;
          INSTANCE.open();
        }
        else {
          INSTANCE.history.add(accountId);
          INSTANCE.historyCursor = INSTANCE.history.size() - 1;
          INSTANCE.setAccountId(accountId);
          INSTANCE.getShell().forceActive();
        }
      }
    });

  }

  @Override
  public void create() {
    super.create();
    setTitle("Account " + Convert.toUnsignedLong(accountId));
    setMessage("");
  }

  @Override
  public boolean close() {
    INSTANCE = null;
    return super.close();
  }

  public void setAccountId(Long accountId) {
    this.accountId = accountId;

    updateNavigateButtons();
    updatePaymentButtons();

    Account account = Account.getAccount(accountId);
    Long balance = account == null ? 0l : (long) account.getEffectiveBalance();

    setTitle("Account " + Convert.toUnsignedLong(accountId));
    setMessage("Balance " + Long.toString(balance));

    if (tabFolder != null) {
      tabFolder.dispose();
    }

    tabFolder = new AccountTabFolder(container, SWT.NONE, accountId, nxt,
        engine, userService, sync, contactsService);
    GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL)
        .span(5, 1).grab(true, true).applyTo(tabFolder);

    container.layout();
    ((AccountTabFolder) tabFolder).refresh();
  }

  private void updateNavigateButtons() {
    previousButton.setEnabled(historyCursor > 0);
    nextButton.setEnabled(historyCursor < (history.size() - 1));
  }

  private void updatePaymentButtons() {
    IUser user = userService.getActiveUser();
    boolean enabled = !user.getAccount().isReadOnly()
        && user.getAccount().getBalance() > 0;

    sendMoneyButton.setEnabled(enabled);
    sendMessageButton.setEnabled(enabled);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    initializeDialogUnits(parent);
    Composite outerContainer = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(5, false);
    layout.horizontalSpacing = 5;
    layout.marginTop = 5;
    layout.marginLeft = 5;

    GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
    gd.widthHint = Math
        .round((convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH) / 2) * 3);

    container = new Composite(outerContainer, SWT.NONE);
    container.setLayoutData(gd);
    container.setLayout(layout);

    sendMoneyButton = new Button(container, SWT.PUSH);
    sendMoneyButton.setEnabled(false);
    sendMoneyButton.setText("Payment");
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .applyTo(sendMoneyButton);

    sendMoneyButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Shell shell = getShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }
        WizardDialog dialog = new WizardDialog(shell, new SendMoneyWizard(
            userService, nxt, accountId));
        dialog.open();
      }
    });

    sendMessageButton = new Button(container, SWT.PUSH);
    sendMessageButton.setEnabled(false);
    sendMessageButton.setText("Message");
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .applyTo(sendMessageButton);

    Label filler = new Label(container, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, false).applyTo(filler);

    previousButton = new Button(container, SWT.PUSH);
    previousButton.setEnabled(false);
    previousButton.setText("<");
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .applyTo(previousButton);

    previousButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        historyCursor = Math.max(0, historyCursor - 1);
        setAccountId(history.get(historyCursor));
      }
    });

    nextButton = new Button(container, SWT.PUSH);
    nextButton.setEnabled(false);
    nextButton.setText(">");
    GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER)
        .applyTo(nextButton);

    nextButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        historyCursor = Math.min(history.size() - 1, historyCursor + 1);
        setAccountId(history.get(historyCursor));
      }
    });

    tabFolder = new Composite(container, SWT.NONE);
    GridDataFactory.swtDefaults().grab(true, true).span(5, 1)
        .align(SWT.BEGINNING, SWT.CENTER).applyTo(tabFolder);
    ((GridData) tabFolder.getLayoutData()).minimumHeight = 220;

    sync.asyncExec(new Runnable() {

      @Override
      public void run() {
        setAccountId(accountId);
      }
    });

    return outerContainer;
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.CLOSE_LABEL,
        true);
  }

  @Override
  protected boolean isResizable() {
    return true;
  }
}
