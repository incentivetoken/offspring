package com.dgex.offspring.trader.myassets;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.Asset;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.nxtCore.service.Utils;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.trader.api.IAssetExchange;
import com.dgex.offspring.ui.PlaceAskOrderWizard;
import com.dgex.offspring.ui.PlaceBidOrderWizard;
import com.dgex.offspring.user.service.IUser;
import com.dgex.offspring.user.service.IUserService;

public class MyAssetsPart {

  private Composite mainComposite;
  private MyAssetsViewer viewer;
  private Combo comboSender;
  private IUserService userService;
  private final List<IUser> senders = new ArrayList<IUser>();
  private IAssetExchange exchange;
  private PaginationContainer paginationContainer;

  @PostConstruct
  public void postConstruct(final Composite parent, final INxtService nxt,
      final IUserService userService, UISynchronize sync,
      IStylingEngine engine, final IAssetExchange exchange) {

    this.userService = userService;
    this.exchange = exchange;

    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(mainComposite);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(mainComposite);

    /* top bar - account selector */
    createAccountCombo(mainComposite);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
        .grab(true, false).applyTo(comboSender);

    paginationContainer = new PaginationContainer(mainComposite, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    viewer = new MyAssetsViewer(paginationContainer.getViewerParent(), nxt,
        userService, ContactsService.getInstance(), sync, engine);
    paginationContainer.setTableViewer(viewer, 100);

    viewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) viewer
            .getSelection();
        Object assetId = selection.getFirstElement();
        if (assetId instanceof Long) {
          Asset asset = Asset.getAsset((Long) assetId);
          if (asset instanceof Asset) {
            exchange.setSelectedAsset(asset);
          }
        }
      }
    });

    Menu contextMenu = new Menu(viewer.getTable());
    viewer.getTable().setMenu(contextMenu);

    MenuItem itemSellOrder = new MenuItem(contextMenu, SWT.PUSH);
    itemSellOrder.setText("Place Sell Order");
    itemSellOrder.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) viewer
            .getSelection();
        Object assetId = selection.getFirstElement();
        if (assetId instanceof Long) {
          Asset asset = Asset.getAsset((Long) assetId);
          if (asset != null) {
            Shell shell = parent.getShell();
            int quantity = 1;
            long price = 0;
            new WizardDialog(shell, new PlaceAskOrderWizard(userService, nxt,
                (Long) assetId, quantity, price)).open();
          }
        }
      }
    });

    MenuItem itemBuyOrder = new MenuItem(contextMenu, SWT.PUSH);
    itemBuyOrder.setText("Place Buy Order");
    itemBuyOrder.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) viewer
            .getSelection();
        Object assetId = selection.getFirstElement();
        if (assetId instanceof Long) {
          Asset asset = Asset.getAsset((Long) assetId);
          if (asset != null) {
            Shell shell = parent.getShell();
            int quantity = 1;
            long price = 0;
            new WizardDialog(shell, new PlaceBidOrderWizard(userService, nxt,
                (Long) assetId, quantity, price)).open();
          }
        }
      }
    });
  }

  private void createAccountCombo(Composite parent) {
    comboSender = new Combo(parent, SWT.READ_ONLY);
    for (IUser user : userService.getUsers()) {
      senders.add(user);
      comboSender.add(createLabel(user));
    }

    int index = senders.indexOf(userService.getActiveUser());
    index = index == -1 ? 0 : index;
    comboSender.select(index);

    comboSender.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IUser user = senders.get(comboSender.getSelectionIndex());
        if (user != null) {
          userService.setActiveUser(user);
        }
      }
    });
  }
  
  private String createLabel(IUser user) {
    return "# " + user.getAccount().getStringId() + " " + user.getName() + " " + 
        (user.getAccount().isReadOnly() ? "-" : (Utils.quantToString(user.getAccount().getBalanceNQT(),8) + " NXT"));
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

}