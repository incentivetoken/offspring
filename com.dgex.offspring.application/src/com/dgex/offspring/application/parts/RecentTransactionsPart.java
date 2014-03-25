package com.dgex.offspring.application.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.IPageableStructeredContentProvider;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.user.service.IUserService;

public class RecentTransactionsPart {

  // Since the pushing of blocks and adding of transaction coincide, we use this
  // event for refreshing the transactions list.

  static Logger logger = Logger.getLogger(RecentTransactionsPart.class);

  static int REFRESH_INTERVAL = 1984; // slightly off from blocks timer

  @Inject
  private IEventBroker broker;

  private Composite mainComposite = null;
  private RecentTransactionsViewer viewer;
  private PaginationContainer paginationContainer;
  private boolean needs_refresh = false;
  private Runnable refreshPoll;

  /* BLOCK PUSHED + BLOCK REMOVED HANDLER */
  private final EventHandler blockPushedListener = new EventHandler() {

    @Override
    public void handleEvent(final Event e) {
      needs_refresh = true;
    }
  };

  @PostConstruct
  public void postConstruct(Composite parent, INxtService nxt,
      IUserService userService, IStylingEngine engine, UISynchronize sync) {

    /* Interval that checks for needs_refresh flag */
    refreshPoll = new Runnable() {

      @Override
      public void run() {
        if (needs_refresh) {
          if (mainComposite != null && !mainComposite.isDisposed()
              && mainComposite.isVisible()) {
            needs_refresh = false;
            refresh();
          }
        }

        Display display = Display.getCurrent();
        if (display != null && !display.isDisposed()) {
          display.timerExec(REFRESH_INTERVAL, this);
        }
      }
    };

    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(mainComposite);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(mainComposite);

    paginationContainer = new PaginationContainer(mainComposite, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    viewer = new RecentTransactionsViewer(
        paginationContainer.getViewerParent(), nxt, engine, userService, sync,
        ContactsService.getInstance());
    paginationContainer.setTableViewer(viewer, 100);

    /* If Nxt is scanning we can wait for TOPIC_BLOCK_SCANNER_FINISHED */
    if (!nxt.isScanning()) {
      needs_refresh = true;
    }

    /* Start the poller */
    parent.getDisplay().timerExec(10, refreshPoll);
  }

  private void refresh() {
    IPageableStructeredContentProvider contentProvider = (IPageableStructeredContentProvider) viewer
        .getGenericTable().getContentProvider();
    contentProvider.reset(viewer);
    viewer.setInput(1);
  }

  @Focus
  public void onFocus() {
    viewer.getControl().setFocus();
  }

  @Inject
  @Optional
  private void onBlockScanStart(
      @UIEventTopic(INxtService.TOPIC_BLOCK_SCANNER_START) int dummy) {
    broker.unsubscribe(blockPushedListener);
  }

  @Inject
  @Optional
  private void onBlockScanFinished(
      @UIEventTopic(INxtService.TOPIC_BLOCK_SCANNER_FINISHED) int dummy) {
    broker.subscribe(INxtService.TOPIC_BLOCK_PUSHED, blockPushedListener);
    broker.subscribe(INxtService.TOPIC_BLOCK_POPPED, blockPushedListener);
    needs_refresh = true;
  }

  @Inject
  @Optional
  public void partActivation(
      @UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
    needs_refresh = true;
  }

}