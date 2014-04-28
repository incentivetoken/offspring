package com.dgex.offspring.ui;

import nxt.Constants;
import nxt.Nxt;
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
import com.dgex.offspring.nxtCore.service.Utils;
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
      return Utils.getAmountNQT(textAmount.getText().trim());
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
      Long amountNQT = Utils.getAmountNQT(amountValue);
      if (amountNQT == null) {
        message[0] = "Incorrect amount";
        return false;
      }
      textAmountReadonly.setText(amountValue);
      return true;
    }
  };

  final IGenericTransactionField fieldReferenced = new IGenericTransactionField() {

    private Text textReferenced;
    private Text textReferencedReadonly;

    @Override
    public String getLabel() {
      return "In-reply to";
    }

    @Override
    public Object getValue() {
      String referencedValue = textReferenced.getText().trim();
      if (referencedValue.isEmpty()) {
        return null;
      }
      try {
        return Convert.parseUnsignedLong(referencedValue);
      }
      catch (RuntimeException e) {
        logger.error("Parse Recipient ID", e);
      }
      return null;
    }

    @Override
    public Control createControl(Composite parent) {
      textReferenced = new Text(parent, SWT.BORDER);
      textReferenced.setMessage("transaction id or empty");
      textReferenced.setText("");
      textReferenced.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textReferenced;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textReferencedReadonly = new Text(parent, SWT.BORDER);
      textReferencedReadonly.setText("");
      textReferencedReadonly.setEditable(false);
      return textReferencedReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String referencedValue = textReferenced.getText().trim();
      if (!referencedValue.isEmpty()) {
        try {
          Long id = Convert.parseUnsignedLong(referencedValue);
          Transaction t = Nxt.getBlockchain().getTransaction(id);
          if (t == null) {
            message[0] = "Referenced transaction does not exist";
            return false;
          }
        }
        catch (RuntimeException e) {
          message[0] = "Incorrect referenced transaction";
          return false;
        }
      }
      textReferencedReadonly.setText(referencedValue);
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
        long amountNQT = (Long) fieldAmount.getValue();

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        long feeNQT = dialog.getFeeNQT();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createPaymentTransaction(sender, recipient,
              amountNQT, deadline, feeNQT, null);
          return t.getStringId();
        }
        catch (TransactionException e) {
          e.printStackTrace();
          message[0] = e.getMessage();
        }
        catch (ValidationException e) {
          e.printStackTrace();
          message[0] = e.getMessage();
        }
        return null;
      }

      @Override
      public IGenericTransactionField[] getFields() {
        return new IGenericTransactionField[] { fieldSender, fieldRecipient,
            fieldAmount, fieldReferenced };
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
        if (user.getAccount().getBalanceNQT() <= Constants.ONE_NXT) {
          message[0] = "Insufficient balance";
          return false;
        }
        return true;
      }
    });
  }
}
