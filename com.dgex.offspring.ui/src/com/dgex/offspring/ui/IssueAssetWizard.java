package com.dgex.offspring.ui;

import nxt.Asset;
import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.Transaction;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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

public class IssueAssetWizard extends GenericTransactionWizard {

  final IGenericTransactionField fieldName = new IGenericTransactionField() {

    private Text textName;
    private Text textNameReadonly;

    @Override
    public String getLabel() {
      return "Name";
    }

    @Override
    public Object getValue() {
      return textName.getText().trim();
    }

    @Override
    public Control createControl(Composite parent) {
      textName = new Text(parent, SWT.BORDER);
      textName.setText("");
      textName.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textName;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textNameReadonly = new Text(parent, SWT.BORDER);
      textNameReadonly.setText("");
      textNameReadonly.setEditable(false);
      return textNameReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String name = textName.getText().trim();
      if (name.length() < 3 || name.length() > 10) {
        message[0] = "Length must be between 3 and 10 characters";
        return false;
      }

      String normalizedName = name.toLowerCase();
      for (int i = 0; i < normalizedName.length(); i++) {
        if (Constants.ALPHABET.indexOf(normalizedName.charAt(i)) < 0) {
          message[0] = "Incorrect asset name";
          return false;
        }
      }

      try {
        if (Asset.getAssets(normalizedName) != null) {
          message[0] = "Asset name already used";
          return false;
        }
      }
      catch (NullPointerException e) {}

      textNameReadonly.setText(name);
      return true;
    }
  };

  final IGenericTransactionField fieldDescription = new IGenericTransactionField() {

    private Text textDescr;
    private Text textDescrReadonly;

    @Override
    public String getLabel() {
      return "Description";
    }

    @Override
    public Object getValue() {
      return textDescr.getText().trim();
    }

    @Override
    public Control createControl(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(1).applyTo(comp);

      textDescr = new Text(comp, SWT.BORDER | SWT.MULTI);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
          .hint(SWT.DEFAULT, 100).applyTo(textDescr);

      textDescr.setText("");
      textDescr.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return comp;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      Composite comp = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(1).applyTo(comp);

      textDescrReadonly = new Text(comp, SWT.BORDER | SWT.MULTI);
      textDescrReadonly.setEditable(false);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
          .hint(SWT.DEFAULT, 100).applyTo(textDescrReadonly);

      return comp;
    }

    @Override
    public boolean verify(String[] message) {
      String description = textDescr.getText().trim();

      if (description.length() > 1000) {
        message[0] = "Description must be between 0 and 1000 characters";
        return false;
      }

      textDescrReadonly.setText(description);
      return true;
    }
  };

  final IGenericTransactionField fieldQuantity = new IGenericTransactionField() {

    private Text textQuantity;
    private Text textQuantityReadonly;

    @Override
    public String getLabel() {
      return "Quantity";
    }

    @Override
    public Object getValue() {
      return Utils.getQuantityQNT(textQuantity.getText().trim());
    }

    @Override
    public Control createControl(Composite parent) {
      textQuantity = new Text(parent, SWT.BORDER);
      textQuantity.setText("0");
      textQuantity.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textQuantity;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textQuantityReadonly = new Text(parent, SWT.BORDER);
      textQuantityReadonly.setEditable(false);
      return textQuantityReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String quantityValue = textQuantity.getText().trim();
      Long quantityQNT = Utils.getQuantityQNT(quantityValue);
      if (quantityQNT == null) {
        message[0] = "Incorect asset quantity";
        return false;
      }
      textQuantityReadonly.setText(quantityValue);
      return true;
    }
  };

  final IGenericTransactionField fieldDecimals = new IGenericTransactionField() {

    private Text textDecimals;
    private Text textDecimalsReadonly;

    @Override
    public String getLabel() {
      return "Decimals";
    }

    @Override
    public Object getValue() {
      return Utils.parseDecimals(textDecimals.getText().trim());
    }

    @Override
    public Control createControl(Composite parent) {
      textDecimals = new Text(parent, SWT.BORDER);
      textDecimals.setText("8");
      textDecimals.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });
      return textDecimals;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textDecimalsReadonly = new Text(parent, SWT.BORDER);
      textDecimalsReadonly.setEditable(false);
      return textDecimalsReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String decimalValue = textDecimals.getText().trim();
      Byte decimals = Utils.parseDecimals(decimalValue);
      if (decimals == null) {
        message[0] = "Incorect asset decimals";
        return false;
      }
      textDecimalsReadonly.setText(decimalValue);
      return true;
    }
  };

  public IssueAssetWizard(final IUserService userService, final INxtService nxt) {
    super(userService);
    setWindowTitle("Issue Asset");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {
        IAccount sender = user.getAccount();
        String name = (String) fieldName.getValue();
        String description = (String) fieldDescription.getValue();
        long quantityQNT = (Long) fieldQuantity.getValue();
        byte decimals = (Byte) fieldDecimals.getValue();

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        dialog.setMinimumFeeNQT(1000 * Constants.ONE_NXT);
        dialog.setFeeNQT(1000 * Constants.ONE_NXT);
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        long feeNQT = dialog.getFeeNQT();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createIssueAssetTransaction(sender, name,
              description, quantityQNT, decimals, deadline, feeNQT, null);
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
        return new IGenericTransactionField[] { fieldSender, fieldName,
            fieldDescription, fieldQuantity, fieldDecimals };
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
        if (user.getAccount().getBalanceNQT() < (1000 * Constants.ONE_NXT)) {
          message[0] = "Insufficient balance";
          return false;
        }
        return true;
      }
    });
  }
}
