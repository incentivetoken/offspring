package com.dgex.offspring.ui;

import java.text.DecimalFormat;

import nxt.Asset;
import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.util.Convert;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.swt.wizard.GenericTransactionWizard;
import com.dgex.offspring.swt.wizard.IGenericTransaction;
import com.dgex.offspring.swt.wizard.IGenericTransactionField;
import com.dgex.offspring.user.service.IUserService;

public class PlaceBidOrderWizard extends GenericTransactionWizard {

  static String THIRD_COLUMN = "000000000000";

  final IGenericTransactionField fieldAsset = new IGenericTransactionField() {

    private Text textAsset;
    private Text textAssetReadonly;
    public Label labelAssetName;
    private Label labelAssetNameReadonly;
    static final String EMPTY_NAME = "                "; // 15 characters

    @Override
    public String getLabel() {
      return "Asset";
    }

    @Override
    public Object getValue() {
      String id_as_string = textAsset.getText().trim();
      Long id = id_as_string.isEmpty() ? null : Convert
          .parseUnsignedLong(id_as_string);
      if (id != null) {
        return Asset.getAsset(id);
      }
      return null;
    }

    @Override
    public Control createControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 0)
          .margins(0, 0).applyTo(composite);

      textAsset = new Text(composite, SWT.BORDER);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(textAsset);

      String id = presetAssetId != null ? Convert.toUnsignedLong(presetAssetId)
          : "";
      textAsset.setText(id);
      textAsset.addModifyListener(new ModifyListener() {

        @Override
        public void modifyText(ModifyEvent e) {
          requestVerification();
        }
      });

      labelAssetName = new Label(composite, SWT.NONE);

      GC gc = new GC(labelAssetName);
      GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
          .hint(gc.textExtent(THIRD_COLUMN).x, SWT.DEFAULT)
          .applyTo(labelAssetName);
      gc.dispose();

      labelAssetName.setText(EMPTY_NAME);
      return composite;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 0)
          .margins(0, 0).applyTo(composite);

      textAssetReadonly = new Text(composite, SWT.BORDER);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(textAssetReadonly);
      textAssetReadonly.setText("");
      textAssetReadonly.setEditable(false);

      labelAssetNameReadonly = new Label(composite, SWT.NONE);

      GC gc = new GC(labelAssetNameReadonly);
      GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER)
          .hint(gc.textExtent(THIRD_COLUMN).x, SWT.DEFAULT)
          .applyTo(labelAssetNameReadonly);
      gc.dispose();

      labelAssetNameReadonly.setText("");
      return composite;
    }

    @Override
    public boolean verify(String[] message) {
      String id_as_string = textAsset.getText().trim();
      Long id = id_as_string.isEmpty() ? null : Convert
          .parseUnsignedLong(id_as_string);
      if (id == null) {
        labelAssetName.setText(EMPTY_NAME);
        message[0] = "Invalid id";
        return false;
      }

      Asset asset = Asset.getAsset(id);
      if (asset == null) {
        labelAssetName.setText(EMPTY_NAME);
        message[0] = "Asset does not exist";
        return false;
      }
      labelAssetName.setText(asset.getName());
      textAssetReadonly.setText(id_as_string);
      labelAssetNameReadonly.setText(asset.getName());
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
    };

    @Override
    public Control createControl(Composite parent) {
      textQuantity = new Text(parent, SWT.BORDER);
      textQuantity.setText(Utils.quantToString(presetQuantityQNT));
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
      Long quantityQNT = Utils.getQuantityQNT(text);
      if (quantityQNT == null) {
        message[0] = "Invalid value";
        return false;
      }

      Asset asset = (Asset) fieldAsset.getValue();
      if (asset != null) {
        if (quantityQNT > asset.getQuantityQNT()) {
          message[0] = "There where only "
              + Utils.quantToString(asset.getQuantityQNT())
              + " assets issued";
          return false;
        }
      }
      textQuantityReadonly.setText(text);
      return true;
    }
  };

  /* Price is expressed in NXT sends! User enters price in NXT with precision */
  final IGenericTransactionField fieldPrice = new IGenericTransactionField() {

    private Text textPrice;
    private Text textPriceReadonly;
    private Label labelPriceTotal;
    private Label labelPriceTotalReadonly;
    private final DecimalFormat formatDouble = new DecimalFormat("#.##");

    private final ModifyListener modificationListener = new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        requestVerification();
      }
    };

    @Override
    public String getLabel() {
      return "Price";
    }

    @Override
    public Object getValue() {
      return Utils.getAmountNQT(textPrice.getText().trim());
    };

    @Override
    public Control createControl(Composite parent) {

      String price;
      if (presetAssetId != null && presetPriceNQT > 0) {
        price = Utils.quantToString(presetPriceNQT);
      }
      else {
        price = "0.00";
      }

      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 0)
          .margins(0, 0).applyTo(composite);

      textPrice = new Text(composite, SWT.BORDER);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(textPrice);
      textPrice.setText(price);
      textPrice.addModifyListener(modificationListener);

      labelPriceTotal = new Label(composite, SWT.NONE);
      GC gc = new GC(labelPriceTotal);
      GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
          .hint(gc.textExtent(THIRD_COLUMN).x, SWT.DEFAULT)
          .applyTo(labelPriceTotal);
      gc.dispose();
      labelPriceTotal.setText("0.0");
      return composite;

      // ------------------
      // textPrice = new Text(parent, SWT.BORDER);
      //
      // if (presetAssetId != null && presetPrice > 0) {
      // Double p = new Long(presetPrice).doubleValue() / 100;
      // p = (double) Math.round(p * 100) / 100;
      // textPrice.setText(Double.toString(p));
      // }
      // else {
      // textPrice.setText("0");
      // }
      //
      // textPrice.addModifyListener(modificationListener);
      // return textPrice;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 0)
          .margins(0, 0).applyTo(composite);

      textPriceReadonly = new Text(composite, SWT.BORDER);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
          .grab(true, false).applyTo(textPriceReadonly);
      textPriceReadonly.setText("");

      labelPriceTotalReadonly = new Label(composite, SWT.NONE);
      GC gc = new GC(labelPriceTotalReadonly);
      GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER)
          .hint(gc.textExtent(THIRD_COLUMN).x, SWT.DEFAULT)
          .applyTo(labelPriceTotalReadonly);
      gc.dispose();
      labelPriceTotalReadonly.setText("0.0");
      return composite;

      // textPriceReadonly = new Text(parent, SWT.BORDER);
      // textPriceReadonly.setEditable(false);
      // return textPriceReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      labelPriceTotalReadonly.setText("");
      labelPriceTotal.setText("");

      String text = textPrice.getText().trim();
      Long priceNQT = Utils.getAmountNQT(text);
      if (priceNQT == null) {
        message[0] = "Incorrect value";
        return false;
      }
      textPriceReadonly.setText(text);

      /* calculate the total */
      Long quantityQNT = (Long) fieldQuantity.getValue();
      if (quantityQNT != null) {
        String txt = Utils.quantToString(quantityQNT);
        labelPriceTotalReadonly.setText(txt);
        labelPriceTotal.setText(txt);
      }
      return true;
    }
  };

  // final IGenericTransactionField fieldTotal = new IGenericTransactionField()
  // {
  //
  // private Label labelTotal;
  // private Label labelTotalReadonly;
  //
  // @Override
  // public String getLabel() {
  // return "Total";
  // }
  //
  // @Override
  // public Object getValue() {
  // return null;
  // }
  //
  // @Override
  // public Control createControl(Composite parent) {
  // labelTotal = new Label(parent, SWT.NONE);
  // if (presetAssetId != null && presetPrice > 0) {
  // Double p = new Long(presetPrice).doubleValue() / 100;
  // p = p * presetQuantity;
  // p = (double) Math.round(p * 100) / 100;
  // labelTotal.setText(Double.toString(p));
  // }
  // else {
  // labelTotal.setText("0");
  // }
  // return labelTotal;
  // }
  //
  // @Override
  // public Control createReadonlyControl(Composite parent) {
  // labelTotalReadonly = new Label(parent, SWT.NONE);
  // labelTotalReadonly.setText(labelTotal.getText());
  // return labelTotalReadonly;
  // }
  //
  // @Override
  // public boolean verify(String[] message) {
  // long price = (long) fieldPrice.getValue(); // price in cents
  // int quantity = (int) fieldQuantity.getValue();
  // String text = "";
  // if (price != -1 && quantity != -1) {
  // Double p = new Long(price).doubleValue() / 100;
  // p = p * presetQuantity;
  // p = (double) Math.round(p * 100) / 100;
  // text = Double.toString(p);
  // }
  // labelTotal.setText(text);
  // labelTotalReadonly.setText(text);
  // return true;
  // }
  //
  // };

  private Long presetAssetId;
  private long presetQuantityQNT;
  private long presetPriceNQT; // in cents per asset

  public PlaceBidOrderWizard(final IUserService userService,
      final INxtService nxt, Long assetId, long quantityQNT, long priceNQT) {
    super(userService);
    this.presetAssetId = assetId;
    this.presetQuantityQNT = quantityQNT;
    this.presetPriceNQT = priceNQT;

    setWindowTitle("Place Buy Order");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {
        IAccount sender = user.getAccount();
        Asset asset = (Asset) fieldAsset.getValue();
        long quantityQNT = (int) fieldQuantity.getValue();
        long priceNQT = (long) fieldPrice.getValue(); // price is in cents

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        dialog.setMinimumFeeNQT(Constants.ONE_NXT);
        dialog.setFeeNQT(Constants.ONE_NXT);
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        long feeNQT = dialog.getFeeNQT();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createPlaceBidOrderTransaction(sender,
              asset.getId(), quantityQNT, priceNQT, deadline, feeNQT, null);
          return t.getStringId();
        }
        catch (ValidationException e) {
          message[0] = e.getMessage();
        }
        catch (TransactionException e) {
          message[0] = e.getMessage();
        }
        return null;
      }

      @Override
      public IGenericTransactionField[] getFields() {
        return new IGenericTransactionField[] { fieldSender, fieldAsset,
            fieldQuantity, fieldPrice /* , fieldTotal */};
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

        Long quantityQNT = (long) fieldQuantity.getValue();
        Long priceNQT = (long) fieldPrice.getValue();
        if (quantityQNT != null && priceNQT != null) {
          try {
            long totalPriceNQT = Convert.safeMultiply(quantityQNT, priceNQT);
            if (totalPriceNQT > user.getAccount().getBalanceNQT()) {
              message[0] = "Insufficient balance";
              return false;
            }
          }
          catch (ArithmeticException e) {
            message[0] = "ArithmeticException";
            return false;
          }
        }
        return true;
      }
    });
  }
}
