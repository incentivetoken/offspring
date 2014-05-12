package com.dgex.offspring.application.dialogs;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.LayoutConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.application.utils.DiceWords;
import com.dgex.offspring.messages.Messages;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.wallet.IWallet;
import com.dgex.offspring.wallet.IWallet.DuplicateAccountException;
import com.dgex.offspring.wallet.IWallet.WalletBackupException;
import com.dgex.offspring.wallet.IWallet.WalletNotInitializedException;
import com.dgex.offspring.wallet.NXTAccount;

public class AddAccountDialog extends TitleAreaDialog {

  static Logger logger = Logger.getLogger(AddAccountDialog.class);

  private final long balance = 0l;
  private Text textAccountLabel;
  private Text textPrivateKey;
  private Text textAccountNumber;
  private Text textBalance;
  private ControlDecoration decoAccountLabel;
  private ControlDecoration decoPrivateKey;
  private Button buttonGenerate;
  private final IWallet wallet;
  private final INxtService nxt;

  private static Image errorImage = FieldDecorationRegistry.getDefault()
      .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();

  private final ModifyListener modifyPrivateKeyListener = new ModifyListener() {

    @Override
    public void modifyText(ModifyEvent e) {
      textAccountNumber.setText(""); //$NON-NLS-1$
      textBalance.setText(""); //$NON-NLS-1$

      String account = nxt.getAccountForPrivateKey(textPrivateKey.getText());
      textAccountNumber.setText(account);

      Long balanceNQT = nxt.getBalanceForAccountNQT(account);
      if (balanceNQT == null)
        textBalance.setText("");
      else
        textBalance.setText(Utils.quantToString(balanceNQT, 8));
      verifyInput();
    }
  };

  private final ModifyListener modifyAccountNumberListener = new ModifyListener() {

    @Override
    public void modifyText(ModifyEvent e) {
      Long balance = nxt.getBalanceForAccountNQT(textAccountNumber.getText()
          .trim());
      if (balance == null)
        textBalance.setText("");
      else
        textBalance.setText(Utils.quantToString(balance.longValue(), 8));
      verifyInput();
    }
  };

  private final ModifyListener modifyLabelListener = new ModifyListener() {

    @Override
    public void modifyText(ModifyEvent e) {
      verifyInput();
    }
  };
  private final SelectionListener selectGenerateListener = new SelectionAdapter() {

    @Override
    public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
      // textPrivateKey.setText(generatePassphrase());
      try {
        textPrivateKey.setText(generatePassphrase2());
      }
      catch (NoSuchAlgorithmException e1) {
        logger.error("Error in key generator", e1);
      }
      catch (NoSuchProviderException e1) {
        logger.error("Error in key generator", e1);
      }
    };
  };
  private Button readonlyButton;
  private ControlDecoration decoAccountNumber;

  public AddAccountDialog(Shell shell, IWallet wallet, INxtService nxt) {
    super(shell);
    this.wallet = wallet;
    this.nxt = nxt;
  }

  @Override
  public void create() {
    super.create();
    setTitle(Messages.AddAccountDialog_title);
    setMessage(Messages.AddAccountDialog_message_default);
  }

  @Override
  protected void createButtonsForButtonBar(Composite parent) {
    createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
        true);
    createButton(parent, IDialogConstants.CANCEL_ID,
        IDialogConstants.CANCEL_LABEL, false);
    getButton(IDialogConstants.OK_ID).setEnabled(false);
  }

  @Override
  protected void buttonPressed(int buttonId) {
    if (buttonId == IDialogConstants.OK_ID) {
      boolean readonly = readonlyButton.getSelection();
      String label = textAccountLabel.getText();
      String account = textAccountNumber.getText();
      String privateKey = readonly ? null : textPrivateKey.getText();

      try {
        wallet.addAccount(NXTAccount
            .create(label, account, privateKey, balance));
        super.buttonPressed(buttonId);
      }
      catch (WalletNotInitializedException e) {
        setMessage(Messages.AddAccountDialog_message_wallet_not_initialized,
            IMessageProvider.ERROR);
        // e.printStackTrace(System.err);
      }
      catch (DuplicateAccountException e) {
        setMessage(Messages.AddAccountDialog_message_duplicate_account,
            IMessageProvider.ERROR);
        // e.printStackTrace(System.err);
      }
      catch (WalletBackupException e) {
        setMessage(Messages.AddAccountDialog_message_backup_error,
            IMessageProvider.ERROR);
        // e.printStackTrace(System.err);
      }
    }
    else {
      super.buttonPressed(buttonId);
    }
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    initializeDialogUnits(parent);
    Composite parentComposite = (Composite) super.createDialogArea(parent);

    GridData gd = new GridData(GridData.FILL, GridData.FILL, false, true);
    gd.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);

    Composite contents = new Composite(parentComposite, SWT.NONE);
    contents.setLayoutData(gd);

    Label label = new Label(contents, SWT.WRAP);
    label.setText(Messages.AddAccountDialog_label_descr_1);
    GridDataFactory.generate(label, 3, 1);

    new Label(contents, SWT.NONE)
        .setText(Messages.AddAccountDialog_label_account_label);
    textAccountLabel = new Text(contents, SWT.SINGLE | SWT.BORDER);
    GridDataFactory.generate(textAccountLabel, 2, 1);
    textAccountLabel.addModifyListener(modifyLabelListener);

    decoAccountLabel = new ControlDecoration(textAccountLabel, SWT.TOP
        | SWT.RIGHT);
    decoAccountLabel.setImage(errorImage);
    decoAccountLabel.hide();

    label = new Label(contents, SWT.WRAP);
    label.setText(Messages.AddAccountDialog_label_descr_2);
    GridDataFactory.generate(label, 3, 1);

    label = new Label(contents, SWT.NONE);
    label.setText(Messages.AddAccountDialog_label_private_key);
    GridDataFactory.defaultsFor(label).align(SWT.BEGINNING, SWT.BEGINNING)
        .indent(0, 4).applyTo(label);

    textPrivateKey = new Text(contents, SWT.MULTI | SWT.BORDER | SWT.WRAP
        | SWT.V_SCROLL);
    textPrivateKey.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
    textPrivateKey.addModifyListener(modifyPrivateKeyListener);

    GC gc = new GC(textPrivateKey);
    FontMetrics fm = gc.getFontMetrics();
    int height = fm.getHeight();
    gc.dispose();
    GridDataFactory.defaultsFor(textPrivateKey)
        .hint(SWT.DEFAULT, (height * 3) + 6).applyTo(textPrivateKey);

    buttonGenerate = new Button(contents, SWT.PUSH);
    buttonGenerate.setText(Messages.AddAccountDialog_label_generate);
    GridDataFactory.defaultsFor(buttonGenerate)
        .align(SWT.BEGINNING, SWT.BEGINNING).indent(0, 4)
        .applyTo(buttonGenerate);
    buttonGenerate.addSelectionListener(selectGenerateListener);

    decoPrivateKey = new ControlDecoration(textPrivateKey, SWT.TOP | SWT.RIGHT);
    decoPrivateKey.setImage(errorImage);
    decoPrivateKey.hide();

    new Label(contents, SWT.NONE);
    readonlyButton = new Button(contents, SWT.CHECK);
    readonlyButton.setText("Readonly account (without private key)");
    readonlyButton.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        if (readonlyButton.getSelection()) {
          buttonGenerate.setEnabled(false);
          textPrivateKey.setEnabled(false);
          textAccountNumber.setEditable(true);
          decoPrivateKey.hide();
          textAccountNumber.addModifyListener(modifyAccountNumberListener);
        }
        else {
          textAccountNumber.removeModifyListener(modifyAccountNumberListener);
          buttonGenerate.setEnabled(true);
          textPrivateKey.setEnabled(true);
          textAccountNumber.setEditable(false);
          decoAccountNumber.hide();
        }
        verifyInput();
      }
    });
    new Label(contents, SWT.NONE);

    new Label(contents, SWT.NONE)
        .setText(Messages.AddAccountDialog_label_account_number);
    textAccountNumber = new Text(contents, SWT.SINGLE | SWT.BORDER);
    textAccountNumber.setEditable(false);
    new Label(contents, SWT.NONE);

    decoAccountNumber = new ControlDecoration(textAccountNumber, SWT.TOP
        | SWT.RIGHT);
    decoAccountNumber.setImage(errorImage);
    decoAccountNumber.hide();

    new Label(contents, SWT.NONE)
        .setText(Messages.AddAccountDialog_label_account_balance);
    textBalance = new Text(contents, SWT.SINGLE | SWT.BORDER);
    textBalance.setEditable(false);
    new Label(contents, SWT.NONE);

    Point defaultMargins = LayoutConstants.getMargins();
    Point defaultSpacing = LayoutConstants.getSpacing();

    GridLayoutFactory.fillDefaults().numColumns(3)
        .spacing(defaultSpacing.x, defaultSpacing.y)
        .margins(defaultMargins.x, defaultMargins.y).generateLayout(contents);
    return contents;
  }

  private boolean verifyInput() {
    boolean verified = true;
    if (textAccountLabel.getText().trim().isEmpty()) {
      decoAccountLabel
          .setDescriptionText(Messages.AddAccountDialog_label_account_label_empty);
      decoAccountLabel.show();
      verified = false;
    }
    else {
      decoAccountLabel.hide();
    }

    if (readonlyButton.getSelection()) {
      if (textAccountNumber.getText().trim().isEmpty()) {
        decoAccountNumber.setDescriptionText("Account number can not be empty");
        decoAccountNumber.show();
        verified = false;
      }
      else {
        decoAccountNumber.hide();
      }
    }
    else {
      if (textPrivateKey.getText().isEmpty()) {
        decoPrivateKey
            .setDescriptionText(Messages.AddAccountDialog_label_private_key_empty);
        decoPrivateKey.show();
        verified = false;
      }
      else {
        decoPrivateKey.hide();
      }
    }

    getButton(IDialogConstants.OK_ID).setEnabled(verified);
    return verified;
  }

  // Fisher–Yates shuffle
  private static void shuffleSeedText(char[] ar) {
    Random rnd = new SecureRandom();
    for (int i = ar.length - 1; i > 0; i--) {
      int index = rnd.nextInt(i + 1);
      char a = ar[index];
      ar[index] = ar[i];
      ar[i] = a;
    }
  }

  private String generatePassphrase() {
    // No space, backslash, newline, tab
    String symbols = "!\"$%^&*()-_=+[{]};:'@#~|,<.>/?"; //$NON-NLS-1$
    String alphaNum = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890"; //$NON-NLS-1$
    char[] chars = new String(symbols + alphaNum).toCharArray();
    shuffleSeedText(chars);

    int low = 70;
    int high = 90;
    Random random = new Random();
    int count = random.nextInt(high - low) + low;
    return RandomStringUtils.random(count, 0, 0, false, false, chars,
        new SecureRandom());
  }

  private String generatePassphrase2() throws NoSuchAlgorithmException,
      NoSuchProviderException {
    DiceWords words = new DiceWords();
    SecureRandom rand = SecureRandom.getInstance("SHA1PRNG", "SUN");

    byte[] bytes = new byte[8];
    rand.nextBytes(bytes); // initialize seed

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 12; i++) {
      String word = words.getDiceWd(rand.nextInt(words.getCount()));
      sb.append(word).append(' ');
    }
    sb.deleteCharAt(sb.length() - 1);
    return sb.toString();
  }

}
