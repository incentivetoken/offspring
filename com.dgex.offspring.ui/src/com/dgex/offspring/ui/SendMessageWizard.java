package com.dgex.offspring.ui;

import java.io.UnsupportedEncodingException;

import nxt.Account;
import nxt.Nxt;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.util.Convert;

import org.apache.log4j.Logger;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.swt.wizard.GenericTransactionWizard;
import com.dgex.offspring.swt.wizard.IGenericTransaction;
import com.dgex.offspring.swt.wizard.IGenericTransactionField;
import com.dgex.offspring.ui.controls.MessageCrypto;
import com.dgex.offspring.user.service.IUserService;

public class SendMessageWizard extends GenericTransactionWizard {

  static Logger logger = Logger.getLogger(SendMessageWizard.class);

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

  final IGenericTransactionField fieldReferenced = new IGenericTransactionField() {

    private Text textReferenced;
    private Text textReferencedReadonly;

    @Override
    public String getLabel() {
      return "In-reply to";
    }

    @Override
    public Object getValue() {
      String recipientValue = textReferenced.getText().trim();
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
      textReferenced = new Text(parent, SWT.BORDER);
      if (referencedTransactionId != null)
        textReferenced.setText(Convert.toUnsignedLong(referencedTransactionId));
      else
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
      String recipientValue = textReferenced.getText().trim();
      try {
        Long id = Convert.parseUnsignedLong(recipientValue);
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
      textReferencedReadonly.setText(recipientValue);
      return true;
    }
  };

  final IGenericTransactionField fieldEncrypt = new IGenericTransactionField() {

    private Button buttonEncrypt;
    private Button buttonEncryptReadonly;

    @Override
    public String getLabel() {
      return "Encrypt";
    }

    @Override
    public Object getValue() {
      return Boolean.valueOf(buttonEncrypt.getSelection());
    }

    @Override
    public Control createControl(Composite parent) {
      buttonEncrypt = new Button(parent, SWT.CHECK);
      buttonEncrypt.setText("Private message");
      buttonEncrypt.setSelection(true);
      buttonEncrypt.addSelectionListener(new SelectionAdapter() {

        @Override
        public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
          requestVerification();
        };
      });
      return buttonEncrypt;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      buttonEncryptReadonly = new Button(parent, SWT.CHECK);
      buttonEncryptReadonly.setText("Private message");
      buttonEncryptReadonly.setSelection(true);
      return buttonEncryptReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      buttonEncryptReadonly.setSelection((boolean) getValue());
      return true;
    }
  };

  final IGenericTransactionField fieldMessage = new IGenericTransactionField() {

    private Text textMessage;
    private Text textMessageReadonly;

    @Override
    public String getLabel() {
      return "Message";
    }

    @Override
    public Object getValue() {
      return textMessage.getText().trim();
    }

    @Override
    public Control createControl(Composite parent) {
      textMessage = new Text(parent, SWT.BORDER);
      GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200)
          .align(SWT.FILL, SWT.FILL).applyTo(textMessage);

      textMessage.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textMessage;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textMessageReadonly = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP
          | SWT.V_SCROLL);
      textMessageReadonly.setText("");
      textMessageReadonly.setEditable(false);
      return textMessageReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String text = textMessage.getText().trim();
      textMessageReadonly.setText(text);
      return true;
    }
  };

  private Long accountId = null;
  private Long referencedTransactionId = null;

  public SendMessageWizard(final IUserService userService, final INxtService nxt,
 Long accountId, Long referencedTransactionId) {
    super(userService);
    this.accountId = accountId;
    this.referencedTransactionId = referencedTransactionId;
    setWindowTitle("Send Message");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {

        IAccount sender = user.getAccount();
        Long recipient = (Long) fieldRecipient.getValue();
        Long referencedId = (Long) fieldReferenced.getValue();
        boolean encrypt = (Boolean) fieldEncrypt.getValue();
        String messageValue = (String) fieldMessage.getValue();

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        int fee = dialog.getFee();
        short deadline = dialog.getDeadline();

        try {
          byte[] data;
          if (encrypt) {
            byte[] theirPublicKey = Account.getAccount(recipient)
                .getPublicKey();
            data = MessageCrypto.encrypt(messageValue, sender.getPrivateKey(),
                theirPublicKey);
          }
          else {
            data = messageValue.getBytes("UTF-8");
          }

          Transaction t = nxt.createSendMessageTransaction(sender, recipient,
              data, deadline, fee, referencedId);
          return t.getStringId();
        }
        catch (TransactionException e) {
          message[0] = e.getMessage();
        }
        catch (ValidationException e) {
          message[0] = e.getMessage();
        }
        catch (UnsupportedEncodingException e) {
          message[0] = e.getMessage();
        }
        return null;
      }

      @Override
      public IGenericTransactionField[] getFields() {
        return new IGenericTransactionField[] { fieldSender, fieldRecipient,
            fieldReferenced, fieldEncrypt, fieldMessage };
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
