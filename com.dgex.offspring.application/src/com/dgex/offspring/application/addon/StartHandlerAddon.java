package com.dgex.offspring.application.addon;

import it.sauronsoftware.junique.JUnique;

import java.lang.reflect.InvocationTargetException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import nxt.Nxt;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.ProgressProvider;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.dgex.offspring.application.dialogs.StartupDialog;
import com.dgex.offspring.config.Config;
import com.dgex.offspring.dataprovider.service.IDataProviderPool;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.providers.bitcoinaverage.PerHourMonthlyCNY;
import com.dgex.offspring.providers.bitcoinaverage.PerHourMonthlyEUR;
import com.dgex.offspring.providers.bitcoinaverage.PerHourMonthlyUSD;
import com.dgex.offspring.providers.bitcoinaverage.PerMinute24HSliding;
import com.dgex.offspring.providers.bitcoinaverage.TickerAllProvider;
import com.dgex.offspring.providers.dgex.DGEX3HMovingAvarage;
import com.dgex.offspring.providers.dgex.DGEXBuyOrderProvider;
import com.dgex.offspring.providers.dgex.DGEXCurrentRateProvider;
import com.dgex.offspring.providers.dgex.DGEXSellOrderProvider;
import com.dgex.offspring.providers.dgex.DGEXTradeProvider;
import com.dgex.offspring.trader.api.IAssetExchange;
import com.dgex.offspring.user.service.IUserService;
import com.dgex.offspring.wallet.INXTWalletAccount;
import com.dgex.offspring.wallet.IWallet;
import com.dgex.offspring.wallet.IWallet.WalletNotInitializedException;
import com.dgex.offspring.wallet.IWalletAccount;

public class StartHandlerAddon {

  static String STATUS_BAR_ID = "com.dgex.offspring.application.toolcontrol.statusstatistics";
  static String MAINWINDOW_ID = "com.dgex.offspring.application.mainwindow";

  private final Logger logger = Logger.getLogger(StartHandlerAddon.class);

  @Inject
  private IEventBroker broker;

  @Inject
  private EModelService modelService;

  @Inject
  private UISynchronize sync;

  private IUserService userService;
  private IWallet wallet;
  private IDataProviderPool pool;
  private MApplication application;
  private INxtService nxt;
  private IAssetExchange exchange;
  private final IRunnableWithProgress startup = new IRunnableWithProgress() {

    volatile boolean DONE = false;

    @Override
    public void run(final IProgressMonitor monitor) {

      Thread cancelThread = new Thread(new Runnable() {

        @Override
        public void run() {
          while (!DONE) {
            try {
              Thread.sleep(500);
              if (monitor.isCanceled()) {
                System.exit(-1);
                return;
              }
            }
            catch (InterruptedException e) {
              return;
            }
          }
        }

      });
      cancelThread.start();

      /* Initialize the user service */

      monitor.beginTask("Adding Users", IProgressMonitor.UNKNOWN);
      userService.initialize(broker, nxt);
      try {
        for (IWalletAccount account : wallet.getAccounts()) {
          if (account instanceof INXTWalletAccount) {
            userService.createUser(account.getLabel(),
                ((INXTWalletAccount) account).getPrivateKey(),
                ((INXTWalletAccount) account).getAccountNumber());
          }
        }
      }
      catch (WalletNotInitializedException e) {
        logger.error("Wallet", e);
      }

      /* Set the active user */

      monitor.setTaskName("Set Active User");
      if (userService.getUsers().size() > 0)
        userService.setActiveUser(userService.getUsers().get(0));

      /* Kick off NXT startup */

      monitor.beginTask("Initializing NXT " + Nxt.VERSION
          + " (might take several minutes)",
          IProgressMonitor.UNKNOWN);
      nxt.initialize(broker, sync);

      /* Immediately register for shutdown */

      Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

        @Override
        public void run() {
          try {
            nxt.shutdown();
          }
          finally {
            String appId = "com.dgex.offspring.application.lifecycle.LifeCycleManager";
            JUnique.releaseLock(appId);
          }
        }
      }));

      monitor.setTaskName("Initializing Services");
      sync.syncExec(new Runnable() {

        @Override
        public void run() {
          MWindow window = (MWindow) modelService.find(MAINWINDOW_ID,
              application);
          window.setLabel(Config.getWindowTitle(nxt.getSoftwareVersion()));
        }
      });

      exchange.initialize(broker);

      monitor.setTaskName("Start Dataprovider Service");

      pool.addProvider(DGEXCurrentRateProvider.getInstance());
      pool.addProvider(DGEX3HMovingAvarage.getInstance());
      pool.addProvider(DGEXTradeProvider.getInstance());
      pool.addProvider(DGEXBuyOrderProvider.getInstance());
      pool.addProvider(DGEXSellOrderProvider.getInstance());
      pool.addProvider(TickerAllProvider.getInstance());
      pool.addProvider(PerMinute24HSliding.getInstance());
      pool.addProvider(PerHourMonthlyUSD.getInstance());
      pool.addProvider(PerHourMonthlyEUR.getInstance());
      pool.addProvider(PerHourMonthlyCNY.getInstance());

      DONE = true;
      monitor.done();
    }
  };

  private final EventHandler eventHandler = new EventHandler() {

    @Override
    public void handleEvent(final Event inEvent) {

      /* Update window title */
      final MWindow window = (MWindow) modelService.find(MAINWINDOW_ID,
          application);
      window.setLabel(Config.getWindowTitle("-"));

      Shell shell = Display.getCurrent().getActiveShell();
      final StartupDialog dialog = new StartupDialog(shell, sync);
      dialog.setBlockOnOpen(false);
      dialog.create();
      dialog.open();

      Job startupJob = new Job("Startup Job") {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
          try {
            startup.run(monitor);
          }
          catch (InvocationTargetException e) {
            e.printStackTrace();
          }
          catch (InterruptedException e) {
            e.printStackTrace();
          }
          return Status.OK_STATUS;
        }
      };
      Job.getJobManager().setProgressProvider(new ProgressProvider() {

        @Override
        public IProgressMonitor createMonitor(Job job) {
          return dialog.getProgressMonitor();
        }
      });
      startupJob.schedule();

    }
  };

  @PostConstruct
  void hookListeners(MApplication application, IUserService userService,
      IWallet wallet, IDataProviderPool pool, INxtService nxt,
      IAssetExchange exchange) {
    this.application = application;
    this.userService = userService;
    this.wallet = wallet;
    this.pool = pool;
    this.nxt = nxt;
    this.exchange = exchange;
    broker.subscribe(UIEvents.UILifeCycle.APP_STARTUP_COMPLETE, eventHandler);

  }

  @PreDestroy
  void unhookListeners() {
    broker.unsubscribe(eventHandler);
  }
}