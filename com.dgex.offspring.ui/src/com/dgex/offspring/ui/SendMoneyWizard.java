package com.dgex.offspring.ui;

import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.swt.wizard.GenericTransactionWizard;
import com.dgex.offspring.swt.wizard.IGenericTransaction;
import com.dgex.offspring.swt.wizard.IGenericTransactionField;
import com.dgex.offspring.user.service.IUserService;

public class SendMoneyWizard extends GenericTransactionWizard {

  static Logger logger = Logger.getLogger(SendMoneyWizard.class);

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
      if (accountId != null)
        textRecipient.setText(Convert.toUnsignedLong(accountId));
      else
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

  final IGenericTransactionField fieldAmount = new IGenericTransactionField() {

    private Text textAmount;
    private Text textAmountReadonly;

    @Override
    public String getLabel() {
      return "Amount";
    }

    @Override
    public Object getValue() {
      return Integer.parseInt(textAmount.getText().trim());
    }

    @Override
    public Control createControl(Composite parent) {
      textAmount = new Text(parent, SWT.BORDER);
      textAmount.setText("0");
      textAmount.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textAmount;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textAmountReadonly = new Text(parent, SWT.BORDER);
      textAmountReadonly.setEditable(false);
      return textAmountReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String amountValue = textAmount.getText().trim();
      try {
        int amount = Integer.parseInt(amountValue);
        if (amount <= 0 || amount >= Constants.MAX_BALANCE) {
          message[0] = "Incorrect amount";
          return false;
        }
      }
      catch (NumberFormatException e) {
        message[0] = "Amount must be numeric";
        return false;
      }
      textAmountReadonly.setText(amountValue);
      return true;
    }
  };

  private Long accountId = null;

  public SendMoneyWizard(final IUserService userService, final INxtService nxt,
      Long accountId) {
    super(userService);
    this.accountId = accountId;
    setWindowTitle("Send Money");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {

        IAccount sender = user.getAccount();
        Long recipient = (Long) fieldRecipient.getValue();
        int amount = (Integer) fieldAmount.getValue();

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        int fee = dialog.getFee();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createPaymentTransaction(sender, recipient,
              amount, deadline, fee, 0l);
          return t.getStringId();
        }
        catch (TransactionException e) {
          message[0] = e.getMessage();
        }
        catch (ValidationException e) {
          message[0] = e.getMessage();
        }
        return null;
      }

      @Override
      public IGenericTransactionField[] getFields() {
        return new IGenericTransactionField[] { fieldSender, fieldRecipient,
            fieldAmount };
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
        if (user.getAccount().getBalance() < 1) {
          message[0] = "Insufficient balance";
          return false;
        }
        return true;
      }
    });
  }
}
