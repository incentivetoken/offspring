package com.dgex.offspring.application.lifecycle;

import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.dgex.offspring.application.dialogs.LoginDialog;
import com.dgex.offspring.config.Config;
import com.dgex.offspring.dataprovider.service.IDataProviderPool;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.user.service.IUserService;
import com.dgex.offspring.wallet.IWallet;

@SuppressWarnings("restriction")
public class LifeCycleManager {

  static Logger logger = Logger.getLogger(LifeCycleManager.class);

  @PostContextCreate
  void postContextCreate(IApplicationContext context, Display display,
      final IEventBroker broker, final INxtService nxt, IWallet wallet,
      UISynchronize sync, IUserService userService, IDataProviderPool pool) {

    logger.info("LifeCycleManager.postContextCreate");

    String appId = "com.dgex.offspring.application.lifecycle.LifeCycleManager";
    boolean alreadyRunning;
    try {
      JUnique.acquireLock(appId);
      alreadyRunning = false;
    }
    catch (AlreadyLockedException e) {
      alreadyRunning = true;
    }
    if (alreadyRunning) {
      File home = new File(System.getProperty("user.home") + File.separator
          + ".junique");

      MessageDialog
          .openWarning(
              display.getActiveShell(),
              "Offspring Already Running",
              "Offspring is already running.\n\n"
                  + "If you keep seeing this dialog close Offspring with your taskmanager.\n\n"
                  + "Cannot find Offspring in your taskmanager?\n"
                  + "Then delete this folder " + home.getAbsolutePath());
      System.exit(0);
      return;
    }

    context.applicationRunning();

    Shell shell = Display.getCurrent().getActiveShell();

    /* DEBUG */
    // final UpdateCenterDialog dialog = new UpdateCenterDialog(shell, sync);
    // if (dialog.open() != Integer.MAX_VALUE) {
    // System.exit(0);
    // return;
    // }

    final LoginDialog loginDialog = new LoginDialog(shell, wallet);
    loginDialog.setBlockOnOpen(true);

    if (loginDialog.open() != Window.OK) {
      System.exit(0);
      return;
    }

    /* Only allow upgrade after login */
    UpgradeManager.init(sync);
    UpgradeManager.checkForNXTUpdate(shell);

    /* Must re-initialize if user selected to use test net (write new config) */
    if (Config.nxtIsTestNet) {
      Config.initialize();
    }
  }

}
