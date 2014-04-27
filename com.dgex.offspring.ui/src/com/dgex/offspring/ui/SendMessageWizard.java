package com.dgex.offspring.ui;

import java.io.UnsupportedEncodingException;

import nxt.Account;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.config.Config;
import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.swt.wizard.GenericTransactionWizard;
import com.dgex.offspring.swt.wizard.IGenericTransaction;
import com.dgex.offspring.swt.wizard.IGenericTransactionField;
import com.dgex.offspring.swt.wizard.IMultiLineTextTransactionField;
import com.dgex.offspring.ui.messaging.MessageCrypto;
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
      textRecipient.setMessage("account number");
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
      buttonEncrypt
          .setText("Encrypt message (only you and recipient can read this message)");
      buttonEncrypt.setSelection(useEncryption);
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
      buttonEncryptReadonly
          .setText("Private message (Private/Public key encryption)");
      buttonEncryptReadonly.setSelection(true);
      return buttonEncryptReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      buttonEncryptReadonly.setSelection((boolean) getValue());
      return true;
    }
  };

  final IMultiLineTextTransactionField fieldMessage = new IMultiLineTextTransactionField() {

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
      textMessage = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.WRAP
          | SWT.V_SCROLL | SWT.H_SCROLL);
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
      textMessageReadonly = new Text(parent, SWT.BORDER);
      textMessageReadonly.setText("");
      textMessageReadonly.setEditable(false);
      return textMessageReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String text = textMessage.getText().trim();

      /* Message cannot excede max length */
      try {
        int length = text.getBytes("UTF-8").length;
        if ((Boolean) fieldEncrypt.getValue()) {
          length += Config.MAGIC_ENCRYPTED_MESSAGE_NUMBER.length + 32;
        }
        else {
          length += Config.MAGIC_UNENCRYPTED_MESSAGE_NUMBER.length;
        }
        if (length > Constants.MAX_ARBITRARY_MESSAGE_LENGTH) {
          message[0] = "Message is to long (max 1000 bytes, yours is " + length
              + "bytes)";
          return false;
        }
      }
      catch (UnsupportedEncodingException e) {
        e.printStackTrace();
        message[0] = "UTF-8 character set not supported on your machine";
        return false;
      }

      textMessageReadonly.setText(text);
      return true;
    }
  };

  private Long accountId = null;
  private Long referencedTransactionId = null;
  private boolean useEncryption = true;

  public SendMessageWizard(final IUserService userService, final INxtService nxt,
 Long accountId, Long referencedTransactionId,
      boolean useEncryption) {
    super(userService);
    this.accountId = accountId;
    this.referencedTransactionId = referencedTransactionId;
    this.useEncryption = useEncryption;
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
        long feeNQT = dialog.getFeeNQT();
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
            byte[] magic = Config.MAGIC_UNENCRYPTED_MESSAGE_NUMBER;
            byte[] bytes = messageValue.getBytes("UTF-8");
            data = new byte[magic.length + bytes.length];

            System.arraycopy(magic, 0, data, 0, magic.length);
            System.arraycopy(bytes, 0, data, magic.length, bytes.length);
          }

          Transaction transaction = Nxt.getBlockchain().getTransaction(
              referencedId);
          
          Transaction t = nxt.createSendMessageTransaction(sender, recipient,
              data, deadline, feeNQT, transaction.getFullHash());
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
        if (user.getAccount().getBalanceNQT() < Constants.ONE_NXT) {
          message[0] = "Insufficient balance";
          return false;
        }
        return true;
      }
    });
  }
}
