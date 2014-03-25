package com.dgex.offspring.application.dialogs;

import java.io.File;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.config.Config;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.wallet.IWallet;
import com.dgex.offspring.wallet.IWallet.WalletInitializedException;
import com.dgex.offspring.wallet.IWallet.WalletInvalidPassword;
import com.dgex.offspring.wallet.IWallet.WalletSaveException;

public class LoginDialog extends TitleAreaDialog {

  private static Logger logger = Logger.getLogger(LoginDialog.class);

  private final IWallet wallet;

  private Text textPassword;
  private Text textlLocation;
  private Text textPassword2;
  private Label labelPassword2;
  private Button buttonBrowse;
  private Composite mainContainer;

  private Button testNetCheckbox;

  public LoginDialog(Shell shell, IWallet wallet) {
    super(shell);
    this.wallet = wallet;
  }

  @Override
  public void create() {
    super.create();

    String message = wallet.getWalletFile().exists() ? Messages.LoginDialog_message_login
        : Messages.LoginDialog_message_newuser;

    setTitle(Messages.LoginDialog_title);
    setMessage(message);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    String okLabel = wallet.getWalletFile().exists() ? Messages.LoginDialog_label_login
        : Messages.LoginDialog_label_create_wallet;

    createButton(parent, IDialogConstants.OK_ID, okLabel, true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        Messages.LoginDialog_label_exit, false);

    getButton(IDialogConstants.OK_ID).setEnabled(false);
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    initializeDialogUnits(parent);
    Composite container = (Composite) super.createDialogArea(parent);

    GridLayout layout = new GridLayout(3, false);
    layout.horizontalSpacing = 15;
    layout.marginTop = 10;
    layout.marginLeft = 10;

    GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
    gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);

    mainContainer = new Composite(container, SWT.NONE);
    mainContainer.setLayoutData(gd);
    mainContainer.setLayout(layout);

    // ----------- password ------------

    Label label = new Label(mainContainer, SWT.NONE);
    label.setText(Messages.LoginDialog_label_password);
    label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
        false));

    gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
    gd.horizontalSpan = 2;

    textPassword = new Text(mainContainer, SWT.BORDER | SWT.PASSWORD);
    textPassword.setLayoutData(gd);

    // ----------- password (repeat) ------------

    labelPassword2 = new Label(mainContainer, SWT.NONE);
    labelPassword2.setText(Messages.LoginDialog_label_password_repeat);
    labelPassword2.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
        false, false));

    gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
    gd.horizontalSpan = 2;

    textPassword2 = new Text(mainContainer, SWT.BORDER | SWT.PASSWORD);
    textPassword2.setLayoutData(gd);

    // ----------- wallet ------------

    label = new Label(mainContainer, SWT.NONE);
    label.setText(Messages.LoginDialog_label_wallet);
    label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false,
        false));
    label.setVisible(false);

    textlLocation = new Text(mainContainer, SWT.BORDER);
    textlLocation.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
        true, false));
    textlLocation.setEnabled(false);

    buttonBrowse = new Button(mainContainer, SWT.PUSH);
    buttonBrowse.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
        false, false));
    buttonBrowse.setText(Messages.LoginDialog_label_browse);

    // ----------- test net ------------

    new Label(mainContainer, SWT.NONE);

    gd = new GridData(GridData.FILL, GridData.CENTER, true, false);
    gd.horizontalSpan = 2;

    testNetCheckbox = new Button(mainContainer, SWT.CHECK);
    testNetCheckbox.setLayoutData(gd);
    testNetCheckbox.setText("Connect to NXT test net (uses different wallet)");
    testNetCheckbox.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (testNetCheckbox.getSelection()) {
          updateWalletPath(Config.getAppPath("testnet.wallet")
              .getAbsolutePath());
          Config.nxtIsTestNet = true;
        }
        else {
          updateWalletPath(wallet.getDefaultWalletFile().getAbsolutePath());
          Config.nxtIsTestNet = false;
        }
      }
    });

    setupControls();

    updateWalletPath(wallet.getWalletFile().getPath());
    return container;
  }

  private void setupControls() {
    buttonBrowse.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent event) {
        FileDialog fd = new FileDialog(getShell(), SWT.SAVE);
        fd.setText(Messages.LoginDialog_FileDialog_title);
        String path = fd.open();
        if (path != null) {
          updateWalletPath(path);
        }
      };
    });

    ModifyListener listener = new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        updateDialogButtons();
      }
    };
    textPassword.addModifyListener(listener);
    textPassword2.addModifyListener(listener);
  }

  @Override
  protected void buttonPressed(int buttonId) {
    if (buttonId == IDialogConstants.OK_ID) {
      if (wallet.getWalletFile().exists()) {
        try {
          /* unlock existing wallet */
          wallet.initialize(textPassword.getText());
        }
        catch (WalletInvalidPassword e) {
          setMessage(NLS.bind(Messages.LoginDialog_message_wrong_password,
              DateFormatUtils.format(System.currentTimeMillis(),
                  Messages.LoginDialog_date_format)));
          return;
        }
      }
      else {
        try {
          /* create new wallet */
          wallet.initialize(textPassword.getText());

          /* save wallet to disk */
          wallet.createWalletFile();
        }
        catch (WalletInvalidPassword e) {}
        catch (WalletSaveException e) {
          setMessage(Messages.LoginDialog_message_save_wallet_error);
        }
      }
    }
    else if (buttonId == IDialogConstants.CANCEL_ID) {
      setMessage(Messages.LoginDialog_message_offspring_shutdown);
      Display.getCurrent().asyncExec(new Runnable() {

        @Override
        public void run() {
          System.exit(0);
        }
      });
      return;
    }
    super.buttonPressed(buttonId);
  }

  private void updateDialogButtons() {
    boolean enabled = true;

    String password = textPassword.getText();
    String password2 = textPassword2.getText();

    /* User is creating new wallet */
    if (!wallet.getWalletFile().exists()) {
      if (password.isEmpty() || password2.isEmpty())
        enabled = false;
      if (!password.equals(password2))
        enabled = false;
    }
    else if (password.isEmpty()) {
      enabled = false;
    }

    Button okBtn = getButton(IDialogConstants.OK_ID);
    if (okBtn != null)
      okBtn.setEnabled(enabled);
  }

  private void updateWalletPath(String path) {
    try {
      File file = new File(path);
      wallet.setWalletFile(file);
      textlLocation.setText(wallet.getWalletFile().getPath());

      if (file.exists()) {
        setMessage(Messages.LoginDialog_message_login);
        ((GridData) textPassword2.getLayoutData()).exclude = true;
        ((GridData) labelPassword2.getLayoutData()).exclude = true;
        labelPassword2.setVisible(false);
        textPassword2.setVisible(false);
        if (getButton(IDialogConstants.OK_ID) != null)
          getButton(IDialogConstants.OK_ID).setText(
              Messages.LoginDialog_label_login);
      }
      else {
        setMessage(Messages.LoginDialog_message_newuser);
        ((GridData) textPassword2.getLayoutData()).exclude = false;
        ((GridData) labelPassword2.getLayoutData()).exclude = false;
        labelPassword2.setVisible(true);
        textPassword2.setVisible(true);
        if (getButton(IDialogConstants.OK_ID) != null)
          getButton(IDialogConstants.OK_ID).setText(
              Messages.LoginDialog_label_create_wallet);
      }

      mainContainer.layout();
    }
    catch (WalletInitializedException e) {
      setMessage(Messages.LoginDialog_message_wallet_initialized);
    }
  }

  @Override
  protected boolean canHandleShellCloseEvent() {
    return false;
  }
}
