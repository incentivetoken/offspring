package com.dgex.offspring.application.handlers;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

import com.dgex.offspring.application.dialogs.AddAccountDialog;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUserService;
import com.dgex.offspring.wallet.INXTWalletAccount;
import com.dgex.offspring.wallet.IWallet;
import com.dgex.offspring.wallet.IWallet.WalletNotInitializedException;
import com.dgex.offspring.wallet.IWalletAccount;

public class CreateAccountHandler {

  private static Logger logger = Logger.getLogger(CreateAccountHandler.class);

  @Execute
  public void execute(Display display, IWallet wallet, INxtService nxt,
      IUserService userService, UISynchronize sync) {

    AddAccountDialog dialog = new AddAccountDialog(display.getActiveShell(),
        wallet, nxt);
    if (dialog.open() == Window.OK) {
      try {
        boolean select = userService.getActiveUser() == null;
        for (IWalletAccount walletAccount : wallet.getAccounts()) {
          if (walletAccount instanceof INXTWalletAccount) {
            userService.createUser(walletAccount.getLabel(),
                ((INXTWalletAccount) walletAccount).getPrivateKey(),
                ((INXTWalletAccount) walletAccount).getAccountNumber());
          }
        }
        if (select && userService.getUsers().size() > 0)
          userService.setActiveUser(userService.getUsers().get(0));
      }
      catch (WalletNotInitializedException e) {
        logger.error("Wallet not initialized", e);
      }
    }
  }
}
