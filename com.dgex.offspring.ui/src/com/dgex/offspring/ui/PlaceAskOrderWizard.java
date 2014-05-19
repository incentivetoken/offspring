package com.dgex.offspring.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nxt.Account;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Combo;
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
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class PlaceAskOrderWizard extends GenericTransactionWizard {
  
  static String THIRD_COLUMN = "000000000000";

  final IGenericTransactionField fieldAsset = new IGenericTransactionField() {

    private final List<Asset> assets = new ArrayList<Asset>();
    private Combo comboAsset;
    private Combo comboAssetReadonly;
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
      int index = comboAsset.getSelectionIndex();
      return index >= 0 ? assets.get(comboAsset.getSelectionIndex()) : null; // Asset
    }

    private void populateAssets(Account account) {
      comboAsset.removeAll();
      assets.clear();
      if (account != null) {
        Map<Long, Long> map = account.getAssetBalancesQNT();
        if (map != null) {
          for (Long assetId : map.keySet()) {
            Asset asset = Asset.getAsset(assetId);
            comboAsset.add(createLabel(account, asset));
            assets.add(asset);
          }
        }
      }
    }

    private String createLabel(Account account, Asset asset) {
      Map<Long, Long> map = account.getAssetBalancesQNT();
      long balanceQNT = map != null ? map.get(asset.getId()) : 0l;
      return "Asset: " + asset.getName() + " Balance: " + Utils.quantToString(balanceQNT, asset.getDecimals());
    }

    @Override
    public Control createControl(Composite parent) {
      comboAsset = new Combo(parent, SWT.READ_ONLY);

      currentUser = (IUser) fieldSender.getValue();
      if (!currentUser.getAccount().isReadOnly()) {
        populateAssets(currentUser.getAccount().getNative());
      }
      int index = 0;
      if (presetAssetId != null) {
        Asset asset = Asset.getAsset(presetAssetId);
        if (asset != null) {
          index = assets.indexOf(asset);
          index = index == -1 ? 0 : index;
        }
      }
      comboAsset.select(index);
      comboAsset.addSelectionListener(selectionListener);
      return comboAsset;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      comboAssetReadonly = new Combo(parent, SWT.READ_ONLY);
      return comboAssetReadonly;
    }

    @Override
    public boolean verify(String[] message) {

      /* Update readonly combo */
      Account account = user.getAccount().getNative();
      Asset asset = (Asset) getValue();
      comboAssetReadonly.removeAll();
      if (asset != null) {
        comboAssetReadonly.add(createLabel(account, asset));
        comboAssetReadonly.select(0);
      }

      /* There might have been a user change must update the list of assets */
      IUser sender = (IUser) fieldSender.getValue();
      if (!sender.equals(currentUser)) {
        currentUser = sender;
        comboAsset.removeAll();
        if (!sender.getAccount().isReadOnly()) {
          populateAssets(sender.getAccount().getNative());
        }
        comboAsset.removeSelectionListener(selectionListener);
        comboAsset.select(0);
        comboAsset.addSelectionListener(selectionListener);
      }
      return true;
    }
  };

  final IGenericTransactionField fieldQuantity = new IGenericTransactionField() {

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
      return Utils.parseQNT(textQuantity.getText().trim(),
          ((Asset) fieldAsset.getValue()).getDecimals());
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

  final IGenericTransactionField fieldPrice = new IGenericTransactionField() {

    private Text textPrice;
    private Text textPriceReadonly;
    private final ModifyListener modificationListener = new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        requestVerification();
      }
    };
    private Label labelPriceTotal;
    private Label labelPriceTotalReadonly;

    @Override
    public String getLabel() {
      return "Price (NXT/Asset)";
    }

    @Override
    public Object getValue() {
      return Utils.parseQNT(textPrice.getText().trim(), 8);
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
      
      Long quantityQNT = (Long) fieldQuantity.getValue();
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
      }      
      
      textPriceReadonly.setText(text);
      return true;
    }
  };

  private Long presetAssetId;
  private long presetQuantityQNT;
  private long presetPriceNQT;

  public PlaceAskOrderWizard(final IUserService userService,
      final INxtService nxt, Long assetId, long quantityQNT, long priceNQT) {
    super(userService);

    this.presetAssetId = assetId;
    this.presetQuantityQNT = quantityQNT;
    this.presetPriceNQT = priceNQT;

    setWindowTitle("Place Sell Order");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {
        IAccount sender = user.getAccount();
        Asset asset = (Asset) fieldAsset.getValue();
        long quantityQNT = (Long) fieldQuantity.getValue();
        long priceNQT = (Long) fieldPrice.getValue();
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
          Transaction t = nxt.createPlaceAskOrderTransaction(sender, asset.getId(), quantityQNT, pricePerQuantityQNT, deadline, feeNQT, null);
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
        return new IGenericTransactionField[] { fieldSender, fieldAsset, fieldQuantity, fieldPrice };
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

        Account account = user.getAccount().getNative();
        Asset asset = (Asset) fieldAsset.getValue();
        Long quantityQNT = (Long) fieldQuantity.getValue();
        if (asset != null && quantityQNT != null) {
          Long assetBalanceQNT = account.getUnconfirmedAssetBalanceQNT(asset.getId());
          if (assetBalanceQNT == null || quantityQNT > assetBalanceQNT) {
            message[0] = "Insufficient Asset Balance";
            return false;
          }
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
