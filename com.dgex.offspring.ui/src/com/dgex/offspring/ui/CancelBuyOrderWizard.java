package com.dgex.offspring.ui;

import nxt.Constants;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.swt.wizard.GenericTransactionWizard;
import com.dgex.offspring.swt.wizard.IGenericTransaction;
import com.dgex.offspring.swt.wizard.IGenericTransactionField;
import com.dgex.offspring.user.service.IUserService;

public class CancelBuyOrderWizard extends GenericTransactionWizard {

  /* This wizard demos how to create an Issue Asset transaction */

  /*
   * The fields we must add (apart from the standard Sender field) are: name,
   * description and quantity
   */

  final IGenericTransactionField fieldName = new IGenericTransactionField() {

    private Text textName;
    private Text textNameReadonly;

    @Override
    public String getLabel() {
      return "Name";
    }

    @Override
    public Object getValue() {
      // TODO Auto-generated method stub
      return null;
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
      String text = textName.getText().trim();
      int l = text.length();
      if (l < 1) {
        message[0] = "Length must be greater than 1";
        return false;
      }
      if (l > 10) {
        message[0] = "Length must be less than 10";
        return false;
      }
      textNameReadonly.setText(text);
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
      return null;
    };

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
      String text = textDescr.getText().trim();
      int l = text.length();
      if (l < 1) {
        message[0] = "Length must be greater than 1";
        return false;
      }
      if (l > 1000) {
        message[0] = "Length must be less than 1000";
        return false;
      }
      textDescrReadonly.setText(text);
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
      return null;
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
      String text = textQuantity.getText().trim();
      int i;
      try {
        i = Integer.parseInt(text);
      }
      catch (NumberFormatException nfe) {
        message[0] = "Value must be numeric";
        return false;
      }

      if (i < 1) {
        message[0] = "Value must be greater than 1";
        return false;
      }
      textQuantityReadonly.setText(text);
      return true;
    }
  };

  public CancelBuyOrderWizard(final IUserService userService) {
    super(userService);
    setWindowTitle("Cancel Buy Order");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {
        message[0] = "Oh oh something went wrong";
        return "12345678901234567890";
      }

      @Override
      public IGenericTransactionField[] getFields() {
        return new IGenericTransactionField[] { fieldSender, fieldName,
            fieldDescription, fieldQuantity };
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
