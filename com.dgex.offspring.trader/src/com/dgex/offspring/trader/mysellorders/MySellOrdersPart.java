package com.dgex.offspring.trader.mysellorders;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.Asset;
import nxt.NxtException.ValidationException;
import nxt.Transaction;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import com.dgex.offspring.nxtCore.service.IAccount;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.TransactionException;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.trader.api.IAssetExchange;
import com.dgex.offspring.ui.PromptFeeDeadline;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MySellOrdersPart {

  private static Logger logger = Logger.getLogger(MySellOrdersPart.class);
  private Composite mainContainer;
  private MySellOrdersViewer viewer;
  private PaginationContainer paginationContainer;

  @Inject
  public MySellOrdersPart() {}

  @PostConstruct
  public void postConstruct(final Composite parent,
      final IUserService userService, final INxtService nxt,
      IAssetExchange exchange) {
    parent.setLayout(new FillLayout());
    mainContainer = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().spacing(0, 0).margins(0, 0).numColumns(1)
        .applyTo(mainContainer);

    paginationContainer = new PaginationContainer(mainContainer, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    viewer = new MySellOrdersViewer(paginationContainer.getViewerParent(), nxt,
        exchange, userService);
    paginationContainer.setTableViewer(viewer, 200);

    Menu contextMenu = new Menu(viewer.getTable());
    viewer.getTable().setMenu(contextMenu);

    MenuItem itemCancelSell = new MenuItem(contextMenu, SWT.PUSH);
    itemCancelSell.setText("Cancel Sell Order");
    itemCancelSell.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) viewer
            .getSelection();
        Object order = selection.getFirstElement();
        if (order instanceof OrderWrapper) {

          Shell shell = parent.getShell();
          IAccount sender = userService.getActiveUser().getAccount();

          PromptFeeDeadline dialog = new PromptFeeDeadline(shell);
          dialog.setMinimumFee(1);
          dialog.setFee(1);
          if (dialog.open() != Window.OK) {
            showErrorMessage(shell, "Invalid fee and deadline");
            return;
          }

          int fee = dialog.getFee();
          short deadline = dialog.getDeadline();

          try {
            Transaction t = nxt.createCancelAskOrderTransaction(sender,
                ((OrderWrapper) order).getId(), deadline, fee, null);

            showMessage(
                shell,
                "Successfully canceled sell order.\n\nTransaction: "
                    + t.getStringId());

            parent.getDisplay().timerExec(100, new Runnable() {

              @Override
              public void run() {
                viewer.setInput(1);
              }
            });
          }
          catch (ValidationException e1) {
            showErrorMessage(shell, e1.getMessage());
          }
          catch (TransactionException e1) {
            showErrorMessage(shell, e1.getMessage());
          }
        }
      }
    });
  }

  public void showErrorMessage(Shell shell, String message) {
    MessageDialog.openError(shell, "Cancel Buy Order Error", message);
  }

  public void showMessage(Shell shell, String message) {
    MessageDialog.openInformation(shell, "Cancel Buy Order", message);
  }

  @Inject
  @Optional
  private void onActiveUserChanged(
      @UIEventTopic(IUserService.TOPIC_ACTIVEUSER_CHANGED) IUser user) {
    if (viewer != null && !viewer.getControl().isDisposed()) {
      viewer.refresh();
    }
  }

  @Inject
  @Optional
  public void partActivation(
      @UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
    if (viewer != null && !viewer.getControl().isDisposed()) {
      viewer.refresh();
    }
  }

  @Inject
  @Optional
  private void onAssetSelected(
      @UIEventTopic(IAssetExchange.TOPIC_ASSET_SELECTED) Asset asset) {
    if (viewer != null && !viewer.getControl().isDisposed()) {
      viewer.refresh();
    }
  }
}