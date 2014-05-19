package com.dgex.offspring.ui;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Alias;
import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.swt.wizard.GenericTransactionWizard;
import com.dgex.offspring.swt.wizard.IGenericTransaction;
import com.dgex.offspring.swt.wizard.IGenericTransactionField;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class TransferAliasWizard extends GenericTransactionWizard {

  static Logger logger = Logger.getLogger(TransferAliasWizard.class);

  final IGenericTransactionField fieldRecipient = new IGenericTransactionField() {

    private Text textRecipient;
    private Text textRecipientReadonly;

    @Override
    public String getLabel() {
      return "Recipient";
    }

    @Override
    public Object getValue() {
      String recipientValue = textRecipient.getText().trim();
      try {
        return Convert.parseUnsignedLong(recipientValue);
      }
      catch (RuntimeException e) {
        logger.error("Parse Recipient ID", e);
      }
      return null;
    }

    @Override
    public Control createControl(Composite parent) {
      textRecipient = new Text(parent, SWT.BORDER);
      textRecipient.setText("");
      textRecipient.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textRecipient;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textRecipientReadonly = new Text(parent, SWT.BORDER);
      textRecipientReadonly.setText("");
      textRecipientReadonly.setEditable(false);
      return textRecipientReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String recipientValue = textRecipient.getText().trim();
      if ("0".equals(recipientValue)) {
        message[0] = "Missing recipient";
        return false;
      }

      try {
        Convert.parseUnsignedLong(recipientValue);
      }
      catch (RuntimeException e) {
        message[0] = "Incorrect recipient";
        return false;
      }
      textRecipientReadonly.setText(recipientValue);
      return true;
    }
  };

  final IGenericTransactionField fieldAlias = new IGenericTransactionField() {

    private final List<Alias> aliases = new ArrayList<Alias>();
    private Combo comboAlias;
    private Combo comboAliasReadonly;
    private IUser currentUser;
    private final SelectionListener selectionListener = new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        requestVerification();
      }
    };

    @Override
    public String getLabel() {
      return "Asset";
    }

    @Override
    public Object getValue() {
      return aliases.get(comboAlias.getSelectionIndex()); // Alias
    }

    private void populateAliases(Account account) {
      comboAlias.removeAll();
      aliases.clear();
      
      for (Alias alias : Alias.getAllAliases()) {
        if (alias.getAccount().equals(account)) {
          comboAlias.add(createLabel(account, alias));
          aliases.add(alias);
        }
      }
    }

    private String createLabel(Account account, Alias alias) {
      return "Alias: " + alias.getAliasName() + " URI: " + alias.getURI().substring(0, Math.min(alias.getURI().length(), 25));
    }

    @Override
    public Control createControl(Composite parent) {
      comboAlias = new Combo(parent, SWT.READ_ONLY);

      currentUser = (IUser) fieldSender.getValue();
      if (!currentUser.getAccount().isReadOnly()) {
        populateAliases(currentUser.getAccount().getNative());
      }
      comboAlias.select(0);
      comboAlias.addSelectionListener(selectionListener);
      return comboAlias;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      comboAliasReadonly = new Combo(parent, SWT.READ_ONLY);
      return comboAliasReadonly;
    }

    @Override
    public boolean verify(String[] message) {

      /* Update readonly combo */
      Account account = user.getAccount().getNative();
      Alias alias = (Alias) getValue();
      comboAliasReadonly.removeAll();
      if (alias != null) {
        comboAliasReadonly.add(createLabel(account, alias));
        comboAliasReadonly.select(0);
      }

      /* There might have been a user change must update the list of assets */
      IUser sender = (IUser) fieldSender.getValue();
      if (!sender.equals(currentUser)) {
        currentUser = sender;
        comboAlias.removeAll();
        if (!sender.getAccount().isReadOnly()) {
          populateAliases(sender.getAccount().getNative());
        }
        comboAlias.removeSelectionListener(selectionListener);
        comboAlias.select(0);
        comboAlias.addSelectionListener(selectionListener);
      }
      return true;
    }
  };


  public TransferAliasWizard(final IUserService userService,
      final INxtService nxt) {
    super(userService);
    setWindowTitle("Transfer Asset");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {

        IAccount sender = user.getAccount();
        Long recipient = (Long) fieldRecipient.getValue();
        Alias alias = (Alias) fieldAlias.getValue();

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        long feeNQT = dialog.getFeeNQT();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createTransferAliasTransaction(sender, recipient, alias.getId(), deadline, feeNQT, null);
          return t.getStringId();
        }
        catch (ValidationException e) {
          e.printStackTrace();
          message[0] = e.getMessage();
        }
        catch (TransactionException e) {
          e.printStackTrace();
          message[0] = e.getMessage();
        }
        return null;
      }

      @Override
      public IGenericTransactionField[] getFields() {
        return new IGenericTransactionField[] { fieldSender, fieldRecipient,
            fieldAlias };
      }

      @Override
      public boolean verifySender(String message[]) {
        if (user == null) {
          message[0] = "Invalid sender";
          return false;
        }
        if (user.getAccount().isReadOnly()) {
          message[0] = "This is a readonly account";
          return false;
        }

        Alias alias = (Alias) fieldAlias.getValue();
        if (alias == null) {
          message[0] = "No such Alias";
          return false;
        }

        if (user.getAccount().getBalanceNQT() < Constants.ONE_NXT) {
          message[0] = "Insufficient Balance";
          return false;
        }
        return true;
      }
    });
  }
}
