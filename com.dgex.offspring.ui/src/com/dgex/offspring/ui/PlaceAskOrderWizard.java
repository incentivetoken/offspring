package com.dgex.offspring.ui;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Asset;
import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.Transaction;

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

public class PlaceAskOrderWizard extends GenericTransactionWizard {

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
        for (Long assetId : account.getAssetBalances().keySet()) {
          Asset asset = Asset.getAsset(assetId);
          comboAsset.add(createLabel(account, asset));
          assets.add(asset);
        }
      }
    }

    private String createLabel(Account account, Asset asset) {
      int balance = account.getAssetBalances().get(asset.getId());
      return "Asset: " + asset.getName() + " Balance: " + balance;
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
      // comboAssetReadonly.add("..");
      // comboAssetReadonly.select(0);
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
      return "Quantity";
    }

    @Override
    public Object getValue() {
      try {
        return Integer.parseInt(textQuantity.getText().trim());
      }
      catch (NumberFormatException e) {
        return -1;
      }
    };

    @Override
    public Control createControl(Composite parent) {
      textQuantity = new Text(parent, SWT.BORDER);
      textQuantity.setText(Integer.toString(presetQuantity));
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
      int quantity;
      try {
        quantity = Integer.parseInt(text);
      }
      catch (NumberFormatException nfe) {
        message[0] = "Value must be numeric";
        return false;
      }

      if (quantity < 1) {
        message[0] = "Value must be greater than 1";
        return false;
      }

      Asset asset = (Asset) fieldAsset.getValue();
      if (asset != null) {
        if (quantity > asset.getQuantity()) {
          message[0] = "There where only " + asset.getQuantity()
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
    private boolean ignore = false;
    private final ModifyListener modificationListener = new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        ignore = true;
        requestVerification();
        ignore = false;
      }
    };

    @Override
    public String getLabel() {
      return "Price";
    }

    @Override
    public Object getValue() {
      try {
        double p = Double.parseDouble(textPrice.getText().trim());
        p = (double) Math.round(p * 100) / 100; // round to two decimals
        return (long) (p * 100); // price in cents
      }
      catch (NumberFormatException e) {
        return -1;
      }
    };

    @Override
    public Control createControl(Composite parent) {
      textPrice = new Text(parent, SWT.BORDER);

      if (presetAssetId != null && presetPrice > 0) {
        Double p = new Long(presetPrice).doubleValue() / 100;
        p = (double) Math.round(p * 100) / 100;
        textPrice.setText(Double.toString(p));
      }
      else {
        textPrice.setText("0");
      }

      textPrice.addModifyListener(modificationListener);
      return textPrice;
    }

    @Override
    public Control createReadonlyControl(Composite parent) {
      textPriceReadonly = new Text(parent, SWT.BORDER);
      textPriceReadonly.setEditable(false);
      return textPriceReadonly;
    }

    @Override
    public boolean verify(String[] message) {
      String text = textPrice.getText().trim();
      long priceInCents; // in NXT cents
      try {
        Double p = Double.parseDouble(text);

        // round the double to two places after comma
        p = (double) Math.round(p * 100) / 100;

        // correct the user input to remove the to high precision
        if (ignore == false) {
          textPrice.removeModifyListener(modificationListener);
          textPrice.setText(Double.toString(p));
          textPrice.addModifyListener(modificationListener);
        }
        priceInCents = (long) (p * 100);
      }
      catch (NumberFormatException e) {
        message[0] = "Value must be numeric";
        return false;
      }

      if (priceInCents < 1) {
        message[0] = "Value must be greater than 0.01";
        return false;
      }

      if (priceInCents > Constants.MAX_BALANCE * 100L) {
        message[0] = "Value must be less than " + Constants.MAX_BALANCE;
        return false;
      }
      textPriceReadonly.setText(text);
      return true;
    }
  };

  private Long presetAssetId;
  private int presetQuantity;
  private long presetPrice; // in cents per asset

  public PlaceAskOrderWizard(final IUserService userService,
      final INxtService nxt, Long assetId, int quantity, long price) {
    super(userService);

    this.presetAssetId = assetId;
    this.presetQuantity = quantity;
    this.presetPrice = price;

    setWindowTitle("Place Sell Order");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {
        IAccount sender = user.getAccount();
        Asset asset = (Asset) fieldAsset.getValue();
        int quantity = (int) fieldQuantity.getValue();
        long price = (long) fieldPrice.getValue(); // price is in cents

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        dialog.setMinimumFee(1);
        dialog.setFee(1);
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        int fee = dialog.getFee();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createPlaceAskOrderTransaction(sender,
              asset.getId(), quantity, price, deadline, fee, null);
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
            fieldQuantity, fieldPrice };
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
        int amount = (int) fieldQuantity.getValue();
        if (asset != null) {
          Integer assetBalance = account.getUnconfirmedAssetBalance(asset
              .getId());
          if (assetBalance == null || amount > assetBalance) {
            message[0] = "Insufficient Asset Balance";
            return false;
          }
        }

        if (user.getAccount().getBalance() < 1) {
          message[0] = "Insufficient Balance";
          return false;
        }
        return true;
      }
    });
  }
}
