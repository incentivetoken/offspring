package com.dgex.offspring.ui;

import java.math.BigInteger;
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
      Long id = id_as_string.isEmpty() ? null : Convert.parseUnsignedLong(id_as_string);
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
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textAsset);

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

  /**
   * Quantity is a double and can have as many decimals after the comma as are
   * set on the asset. The getValue method returns the quantity in it's smallest
   * possible unit.
   */
  final IGenericTransactionField fieldQuantityQNT = new IGenericTransactionField() {

    private Text textQuantity;
    private Text textQuantityReadonly;

    @Override
    public String getLabel() {
      return "Quantity (Asset)";
    }

    @Override
    public Object getValue() {
      if (fieldAsset.getValue() == null) {
        return null;
      }
      return Utils.parseQNT(textQuantity.getText().trim(), ((Asset) fieldAsset.getValue()).getDecimals()); 
    };

    @Override
    public Control createControl(Composite parent) {
      textQuantity = new Text(parent, SWT.BORDER);

      textQuantity.setText("");
      if (presetAssetId != null) {
        Asset asset = Asset.getAsset(presetAssetId);
        if (asset != null) {
          textQuantity.setText(Utils.quantToString(presetQuantityQNT, asset.getDecimals()));
        }
      }

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
      if (fieldAsset.getValue() == null) {
        message[0] = "Must select asset first";
        return false;
      }
      
      Asset asset = (Asset)fieldAsset.getValue();
      String text = textQuantity.getText().trim();
      if (!Utils.vaildateQuantityDecimalsForOrder(text, ((Asset) fieldAsset.getValue()).getDecimals())) {
        message[0] = "To many decimals after comma, max allowed is " + asset.getDecimals();
        return false;
      }
      
      Long quantityQNT = Utils.parseQNT(text, ((Asset) fieldAsset.getValue()).getDecimals());
      if (quantityQNT == null) {
        message[0] = "Invalid value";
        return false;
      }
      if (quantityQNT < 1) {
        message[0] = "Value to small";
        return false;
      }

      if (quantityQNT > asset.getQuantityQNT()) {
        String amount = Utils.quantToString(asset.getQuantityQNT(), ((Asset) fieldAsset.getValue()).getDecimals());
        message[0] = "There where only " + amount + " assets issued";
        return false;
      }
      textQuantityReadonly.setText(text);
      return true;      
    }
  };

  /**
   * Price is expressed in NXT sends! User enters price in NXT with precision.
   * NXT is defined in quants (8 decimals after the comma). The getValue method
   * returns the price in NQT.
   * */
  final IGenericTransactionField fieldPriceNQT = new IGenericTransactionField() {

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
      return "Price (NXT/Asset)";
    }

    @Override
    public Object getValue() {
      return Utils.getAmountNQT(textPrice.getText().trim());
    };

    @Override
    public Control createControl(Composite parent) {
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 0).margins(0, 0).applyTo(composite);

      textPrice = new Text(composite, SWT.BORDER);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textPrice);
      if (presetAssetId != null && presetPriceNQT > 0) {
        textPrice.setText(Utils.quantToString(presetPriceNQT, 8));
      }
      else {
        textPrice.setText("0");
      }
      textPrice.addModifyListener(modificationListener);

      labelPriceTotal = new Label(composite, SWT.NONE);
      GC gc = new GC(labelPriceTotal);
      GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).hint(gc.textExtent(THIRD_COLUMN).x, SWT.DEFAULT).applyTo(labelPriceTotal);
      gc.dispose();
      labelPriceTotal.setText("0.0");
      
      return composite;      
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      
      Composite composite = new Composite(parent, SWT.NONE);
      GridLayoutFactory.fillDefaults().numColumns(2).spacing(10, 0).margins(0, 0).applyTo(composite);

      textPriceReadonly = new Text(composite, SWT.BORDER);
      GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(textPriceReadonly);
      textPriceReadonly.setText("");

      labelPriceTotalReadonly = new Label(composite, SWT.NONE);
      GC gc = new GC(labelPriceTotalReadonly);
      GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).hint(gc.textExtent(THIRD_COLUMN).x, SWT.DEFAULT).applyTo(labelPriceTotalReadonly);
      gc.dispose();
      labelPriceTotalReadonly.setText("0.0");

      return composite;      
    }

    @Override
    public boolean verify(String[] message) {
      labelPriceTotal.setText("");
      labelPriceTotalReadonly.setText("");
      if (fieldAsset.getValue() == null) {
        message[0] = "Must select asset first";
        return false;
      }
      Asset asset = (Asset)fieldAsset.getValue();
      String text = textPrice.getText().trim();
      Long priceNQT = Utils.parseQNT(text, 8);
      if (priceNQT == null) {
        message[0] = "Incorrect value";
        return false;
      }
      
      if (!Utils.validatePriceDecimalsForOrder(text, asset.getDecimals())) {
        message[0] = "To many decimals after comma, max allowed is " + (8 - asset.getDecimals());
        return false;
      }      
      
      Long quantityQNT = (Long) fieldQuantityQNT.getValue();
      if (quantityQNT != null) {
        long pricePerQuantityQNT = Utils.getPricePerQuantityQNT(priceNQT, asset.getDecimals());
        long orderTotalNQT = Utils.calculateOrderTotalNQT(pricePerQuantityQNT, quantityQNT);
        String total = Utils.quantToString(orderTotalNQT, 8);

        labelPriceTotal.setText(total);
        labelPriceTotalReadonly.setText(total);
        labelPriceTotal.pack();
        labelPriceTotal.getParent().layout();
        labelPriceTotalReadonly.pack();
        labelPriceTotalReadonly.getParent().layout();
        
//        StringBuilder b = new StringBuilder();
//        b.append("Price.text="+text+"\n");
//        b.append("Price.nqt="+Long.toString(priceNQT)+"\n");
//        b.append("Quantity.qnt="+Long.toString(quantityQNT)+"\n");
//        b.append("pricePerQuantityQNT="+Long.toString(pricePerQuantityQNT)+"\n");
//        b.append("orderTotalNQT="+Long.toString(orderTotalNQT)+"\n");
//        b.append("-------------------------------------------------------------");
//        System.out.println(b.toString());
        
      }      
      
      textPriceReadonly.setText(text);
      return true;      
    }
  };

  private Long presetAssetId;
  private long presetQuantityQNT;
  private long presetPriceNQT; // in cents per asset

  public PlaceBidOrderWizard(final IUserService userService, final INxtService nxt, Long assetId, long quantityQNT, long priceNQT) {
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
        long quantityQNT = (Long) fieldQuantityQNT.getValue();
        long priceNQT = (Long) fieldPriceNQT.getValue();
        long pricePerQuantityQNT = Utils.getPricePerQuantityQNT(priceNQT, asset.getDecimals());
        
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
          Transaction t = nxt.createPlaceBidOrderTransaction(sender, asset.getId(), quantityQNT, pricePerQuantityQNT, deadline, feeNQT, null);
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
        return new IGenericTransactionField[] { fieldSender, fieldAsset,
            fieldQuantityQNT, fieldPriceNQT /* , fieldTotal */};
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
        if (fieldAsset.getValue() == null) {
          message[0] = "Must set asset first";
          return false;
        }

        Long quantityQNT = (Long) fieldQuantityQNT.getValue();
        Long priceNQT = (Long) fieldPriceNQT.getValue();
        if (quantityQNT != null && priceNQT != null) {
          try {
            Asset asset = ((Asset)fieldAsset.getValue());
            long pricePerQuantityQNT = Utils.getPricePerQuantityQNT(priceNQT, asset.getDecimals());
            long orderTotalNQT = Utils.calculateOrderTotalNQT(pricePerQuantityQNT, quantityQNT);
            if (orderTotalNQT > user.getAccount().getBalanceNQT()) {
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
