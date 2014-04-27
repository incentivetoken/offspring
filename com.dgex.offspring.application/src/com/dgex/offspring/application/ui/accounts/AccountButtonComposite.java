package com.dgex.offspring.application.ui.accounts;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

import nxt.Generator;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.application.utils.Layouts;
import com.dgex.offspring.config.CSSClasses;
import com.dgex.offspring.config.Clipboards;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.ui.SendMessageWizard;
import com.dgex.offspring.ui.SendMoneyWizard;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;
import com.dgex.offspring.wallet.IWallet;
import com.dgex.offspring.wallet.IWallet.AccountNotFoundException;
import com.dgex.offspring.wallet.IWallet.WalletBackupException;
import com.dgex.offspring.wallet.IWallet.WalletNotInitializedException;
import com.dgex.offspring.wallet.IWalletAccount;
import com.dgex.offspring.wallet.NXTAccount;

public class AccountButtonComposite extends Composite {

  private final Logger logger = Logger.getLogger(AccountButtonComposite.class);

  private final Link nameLabel;
  private final Label accountLabel;
  private final Label balanceLabel;
  private Button forgeButton;

  private IUser user;
  private boolean isActive = false;
  private String selectedNameLabel;
  private String normalNameLabel;
  private long deadline = Long.MAX_VALUE;

  public AccountButtonComposite(final Composite parent, final IUser user,
      final IUserService userService, final IWallet wallet,
      IStylingEngine engine, final INxtService nxt) {
    super(parent, SWT.NONE);
    this.user = user;

    initializeCroppedNames(user.getName(), engine);

    setLayout(Layouts.Grid.create(1, 0, 12, 5, 0, 0, 2));
    engine.setClassname(this, CSSClasses.ACCOUNTBUTTON);

    nameLabel = new Link(this, SWT.NONE);
    nameLabel.setText("<A>" + normalNameLabel + "</A>"); //$NON-NLS-1$ //$NON-NLS-2$
    nameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    nameLabel.setToolTipText(Messages.AccountButtonComposite_label_right_click);
    nameLabel.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        userService.setActiveUser(user);
      }
    });
    engine.setClassname(nameLabel, CSSClasses.ACCOUNT_LABEL);

    accountLabel = new Label(this, SWT.NONE);
    accountLabel.setText(user.getAccount().getStringId());
    accountLabel
        .setToolTipText(Messages.AccountButtonComposite_label_right_click);
    accountLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    engine.setClassname(accountLabel, CSSClasses.ACCOUNT_NUMBER);

    balanceLabel = new Label(this, SWT.NONE);
    balanceLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
    balanceLabel
        .setToolTipText(Messages.AccountButtonComposite_label_right_click
            + " - account balance based on 42 confirmations");
    engine.setClassname(balanceLabel, CSSClasses.ACCOUNT_BALANCE);

    forgeButton = new Button(this, SWT.CHECK);
    forgeButton.setSelection(user.getAccount().isForging());
    forgeButton.setText("Forge");
    forgeButton.setEnabled(!user.getAccount().isReadOnly());
    forgeButton
        .setToolTipText("Block generation deadline is estimated, actual deadline may vary");
    forgeButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (forgeButton.getSelection()) {
          if (!user.getAccount().startForging()) {
            forgeButton.removeSelectionListener(this);
            forgeButton.setSelection(false);
            forgeButton.setText("Forge");
            forgeButton.addSelectionListener(this);
          }
        }
        else {
          user.getAccount().stopForging();
          forgeButton.setText("Forge");
        }
      }
    });

    setBalanceNQT(0l, 0l);

    Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
    nameLabel.setMenu(menu);
    balanceLabel.setMenu(menu);
    accountLabel.setMenu(menu);

    MenuItem item = new MenuItem(menu, SWT.PUSH);
    item.setText("Send money");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Shell shell = getShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }
        WizardDialog dialog = new WizardDialog(shell, new SendMoneyWizard(
            userService, nxt, user.getAccount().getId()));
        dialog.open();
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Send Encrypted Message");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Shell shell = getShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }
        WizardDialog dialog = new WizardDialog(shell, new SendMessageWizard(
            userService, nxt, user.getAccount().getId(), null, true));
        dialog.open();
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText("Send Plain Message");
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Shell shell = getShell();
        if (shell != null) {
          while (shell.getParent() != null) {
            shell = shell.getParent().getShell();
          }
        }
        WizardDialog dialog = new WizardDialog(shell, new SendMessageWizard(
            userService, nxt, user.getAccount().getId(), null, false));
        dialog.open();
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountButtonComposite_label_copy_account_number);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(), user.getAccount().getStringId());
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountButtonComposite_label_copy_private_key);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(), user.getAccount().getPrivateKey());
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountButtonComposite_label_copy_balance);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(),
            Convert.toNXT(user.getAccount().getBalanceNQT()));
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountButtonComposite_label_copy_account_label);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        Clipboards.copy(parent.getDisplay(), user.getName());
      }
    });

    item = new MenuItem(menu, SWT.PUSH);
    item.setText(Messages.AccountButtonComposite_label_remove_account);
    item.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent event) {

        if (!MessageDialog.openConfirm(getShell(),
            Messages.AccountButtonComposite_confirm_remove_dialog_title,
            Messages.AccountButtonComposite_confirm_remove_dialog_message))
          return;

        IWalletAccount walletAccount = NXTAccount.create(user.getName(), user
            .getAccount().getStringId(), user.getAccount().getPrivateKey(),
            Long.valueOf(user.getAccount().getBalanceNQT()));

        try {
          wallet.removeAccount(walletAccount);

          boolean active = user.equals(userService.getActiveUser());
          userService.removeUser(user);
          if (active && userService.getUsers().size() > 0)
            userService.setActiveUser(userService.getUsers().get(0));
        }
        catch (WalletNotInitializedException e) {
          logger.error("Not initialized", e); //$NON-NLS-1$
        }
        catch (AccountNotFoundException e) {
          logger.error("Account not found", e); //$NON-NLS-1$
        }
        catch (WalletBackupException e) {
          logger.error("Backup error", e); //$NON-NLS-1$
        }
      }
    });

    pack();
    layout();
  }

  @Override
  public void dispose() {
    this.user = null;
    super.dispose();
  }

  public IUser getUser() {
    return user;
  }

  public void setBalanceNQT(Long balance, Long unconfirmedBalance) {
    String text = "NXT ";
    if ((unconfirmedBalance - balance) > 1
        || (unconfirmedBalance - balance) < -1)
      text += Convert.toNXT(balance) + " (" + Convert.toNXT(unconfirmedBalance)
          + ")";
    else
      text += Convert.toNXT(balance);

    balanceLabel.setText(text);
    layout();
  }

  public void setActive(boolean active) {
    if (active != isActive) {
      isActive = active;
      nameLabel.setText("<A>" + (active ? selectedNameLabel : normalNameLabel) //$NON-NLS-1$
          + "</A>"); //$NON-NLS-1$
      nameLabel.pack();
    }
  }

  public void setGeneratorDeadline(long deadline) {
    this.deadline = System.currentTimeMillis() + (deadline * 1000);
  }

  public void startForging(Generator generator) {
    if (forgeButton.getSelection() == false) {
      forgeButton.setSelection(true);
    }
  }

  public void stopForging(Generator generator) {
    if (forgeButton.getSelection() == true) {
      forgeButton.setSelection(false);
    }
  }

  public void updateGenerationDeadline() {
    if (forgeButton == null || forgeButton.isDisposed()
        || deadline == Long.MAX_VALUE)
      return;

    long remaining = deadline - System.currentTimeMillis();
    String text = "Forge";
    if (user.getAccount().isForging()) {
      if (remaining < 0) {
        text = "Forge in unknown";
      }
      else {
        Duration duration;
        try {
          duration = DatatypeFactory.newInstance().newDuration(remaining);
          text = "Forge in "
              + String.format("%dh:%dm:%ds",
                  duration.getDays() * 24 + duration.getHours(),
                  duration.getMinutes(), duration.getSeconds());
        }
        catch (DatatypeConfigurationException e) {
          logger.error("Not able to format deadline", e);
        }
      }
    }
    forgeButton.setText(text);
    forgeButton.pack();
    forgeButton.getParent().layout();
  }

  /* Calculates the text for name and selected name (with a > in front) */
  private void initializeCroppedNames(String name, IStylingEngine engine) {

    /* Maximum length of an account number is 22 */
    Label label = new Label(this, SWT.NONE);
    engine.setClassname(label, CSSClasses.ACCOUNT_NUMBER);
    GC gc = new GC(label);
    Point size = gc.textExtent("0000000000000000000000"); //$NON-NLS-1$
    gc.dispose();
    label.dispose();

    int maxWidth = size.x;
    Link link = new Link(this, SWT.NONE);
    engine.setClassname(link, CSSClasses.ACCOUNT_LABEL);
    gc = new GC(link);

    /* Calculate text for selected account */
    String text = "> " + name; //$NON-NLS-1$
    for (int i = text.length(); i >= 1; i--) {
      size = gc.textExtent(text);
      if (size.x > maxWidth)
        text = text.substring(0, text.length() - 2);
      else
        break;
    }
    selectedNameLabel = text;

    /* Calculate the text for none selected account */
    text = name;
    for (int i = text.length(); i >= 1; i--) {
      size = gc.textExtent(text);
      if (size.x > maxWidth)
        text = text.substring(0, text.length() - 2);
      else
        break;
    }
    normalNameLabel = text;

    gc.dispose();
    link.dispose();
  }

};