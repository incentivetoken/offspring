package com.dgex.offspring.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import com.dgex.offspring.nxtCore.service.Utils;
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
      return "Asset: " + asset.getName() + " Balance: "
          + Utils.quantToString(balanceQNT);
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
      return Utils.getQuantityQNT(textQuantity.getText().trim());
    };

    @Override
    public Control createControl(Composite parent) {
      textQuantity = new Text(parent, SWT.BORDER);
      textQuantity.setText(Long.toString(presetQuantityQNT));
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
        message[0] = "Value must be numeric";
        return false;
      }
      if (quantityQNT < 1) {
        message[0] = "Value to small";
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
      textPrice = new Text(parent, SWT.BORDER);
      if (presetAssetId != null && presetPriceNQT > 0) {
        textPrice.setText(Utils.quantToString(presetPriceNQT));
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
      Long priceNQT = Utils.getAmountNQT(text);
      if (priceNQT == null) {
        message[0] = "Incorrect value";
        return false;
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
        long quantityQNT = (long) fieldQuantity.getValue();
        long priceNQT = (long) fieldPrice.getValue();

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
          Transaction t = nxt.createPlaceAskOrderTransaction(sender,
              asset.getId(), quantityQNT, priceNQT, deadline, feeNQT, null);
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
        Long quantityQNT = (Long) fieldQuantity.getValue();
        if (asset != null && quantityQNT != null) {
          Long assetBalanceQNT = account.getUnconfirmedAssetBalanceQNT(asset
              .getId());
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
