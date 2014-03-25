package com.dgex.offspring.ui;

import java.util.ArrayList;
import java.util.List;

import nxt.Account;
import nxt.Asset;
import nxt.Constants;
import nxt.NxtException.ValidationException;
import nxt.Transaction;
import nxt.util.Convert;

import org.apache.log4j.Logger;
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

public class TransferAssetWizard extends GenericTransactionWizard {

  // In order to send an asset we need the following fields;
  //
  // String recipientValue = req.getParameter("recipient");
  // String assetValue = req.getParameter("asset");
  // String quantityValue = req.getParameter("quantity");
  //
  // Since a user can only send assets he actually owns the assetValue string is
  // selected from a dropdown that lists all assets owned by an account

  static Logger logger = Logger.getLogger(TransferAssetWizard.class);

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
      return assets.get(comboAsset.getSelectionIndex()); // Asset
    }

    private void populateAssets(Account account) {
      comboAsset.removeAll();
      assets.clear();
      for (Long assetId : account.getAssetBalances().keySet()) {
        Asset asset = Asset.getAsset(assetId);
        comboAsset.add(createLabel(account, asset));
        assets.add(asset);
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
      comboAsset.select(0);
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

  final IGenericTransactionField fieldAmount = new IGenericTransactionField() {

    private Text textAmount;
    private Text textAmountReadonly;

    @Override
    public String getLabel() {
      return "Amount";
    }

    @Override
    public Object getValue() {
      return Integer.parseInt(textAmount.getText().trim());
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
      try {
        int amount = Integer.parseInt(amountValue);
        if (amount <= 0 || amount >= Constants.MAX_ASSET_QUANTITY) {
          message[0] = "Incorrect amount";
          return false;
        }
      }
      catch (NumberFormatException e) {
        message[0] = "Amount must be numeric";
        return false;
      }
      textAmountReadonly.setText(amountValue);
      return true;
    }
  };

  public TransferAssetWizard(final IUserService userService,
      final INxtService nxt) {
    super(userService);
    setWindowTitle("Transfer Asset");
    setTransaction(new IGenericTransaction() {

      @Override
      public String sendTransaction(String[] message) {

        IAccount sender = user.getAccount();
        Long recipient = (Long) fieldRecipient.getValue();
        int amount = (Integer) fieldAmount.getValue();
        Asset asset = (Asset) fieldAsset.getValue();

        PromptFeeDeadline dialog = new PromptFeeDeadline(getShell());
        if (dialog.open() != Window.OK) {
          message[0] = "Invalid fee and deadline";
          return null;
        }
        int fee = dialog.getFee();
        short deadline = dialog.getDeadline();

        try {
          Transaction t = nxt.createTransferAssetTransaction(sender, recipient,
              asset.getId(), amount, deadline, fee, null);
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
        return new IGenericTransactionField[] { fieldSender, fieldRecipient,
            fieldAsset, fieldAmount };
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
        int amount = (Integer) fieldAmount.getValue();
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
