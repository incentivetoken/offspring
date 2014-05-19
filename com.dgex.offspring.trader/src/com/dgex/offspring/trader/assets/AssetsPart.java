package com.dgex.offspring.trader.assets;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import nxt.Asset;

import org.apache.log4j.Logger;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;

import com.dgex.offspring.config.ContactsService;
import com.dgex.offspring.nxtCore.service.INxtService;
import com.dgex.offspring.swt.table.IFilteredStructuredContentProvider;
import com.dgex.offspring.swt.table.PaginationContainer;
import com.dgex.offspring.trader.api.IAssetExchange;
import com.dgex.offspring.ui.InspectAccountDialog;
import com.dgex.offspring.ui.PlaceBidOrderWizard;
import com.dgex.offspring.user.service.IUserService;

public class AssetsPart {

  private static Logger logger = Logger.getLogger(AssetsPart.class);

  private Composite mainComposite;
  private Text filterText;
  private AssetsViewer assetsViewer;
  private PaginationContainer paginationContainer;
  private final Runnable applyFilterDelayed = new Runnable() {

    @Override
    public void run() {
      if (assetsViewer != null && assetsViewer.getControl() != null
          && !assetsViewer.getControl().isDisposed()) {
        assetsViewer.refresh();
      }
    }
  };

  @Inject
  public AssetsPart() {}

  @PostConstruct
  public void postConstruct(final Composite parent,
      final IAssetExchange exchange, final INxtService nxt,
      final IUserService userService, final IStylingEngine engine,
      final UISynchronize sync) {

    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(1).spacing(5, 2).margins(0, 0)
        .applyTo(mainComposite);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(mainComposite);

    filterText = new Text(mainComposite, SWT.BORDER);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER)
        .grab(true, false).applyTo(filterText);
    filterText.addModifyListener(new ModifyListener() {

      @Override
      public void modifyText(ModifyEvent e) {
        if (assetsViewer != null) {
          IFilteredStructuredContentProvider filteredProvider = (IFilteredStructuredContentProvider) assetsViewer
              .getGenericTable().getContentProvider();

          String text = filterText.getText().trim();
          if (!text.isEmpty() && !text.endsWith("*")) {
            text = text + "*";
          }
          filteredProvider.setFilter("NAME.FILTER", text.isEmpty() ? null
              : text);

          parent.getDisplay().timerExec(-1, applyFilterDelayed);
          parent.getDisplay().timerExec(500, applyFilterDelayed);
        }
      }
    });

    paginationContainer = new PaginationContainer(mainComposite, SWT.NONE);
    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true)
        .applyTo(paginationContainer);

    assetsViewer = new AssetsViewer(paginationContainer.getViewerParent(), nxt,
        ContactsService.getInstance(), engine, userService, sync);
    paginationContainer.setTableViewer(assetsViewer, 200);

    assetsViewer.addSelectionChangedListener(new ISelectionChangedListener() {

      @Override
      public void selectionChanged(SelectionChangedEvent event) {
        IStructuredSelection selection = (IStructuredSelection) assetsViewer
            .getSelection();
        Object asset = selection.getFirstElement();
        if (asset instanceof Asset) {
          exchange.setSelectedAsset((Asset) asset);
        }
      }
    });

    assetsViewer.setInput(true);
    assetsViewer.refresh();

    Menu contextMenu = new Menu(assetsViewer.getTable());
    assetsViewer.getTable().setMenu(contextMenu);

    MenuItem itemQuickBuy = new MenuItem(contextMenu, SWT.PUSH);
    itemQuickBuy.setText("Place Buy Order");
    itemQuickBuy.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) assetsViewer
            .getSelection();
        Object asset = selection.getFirstElement();
        if (asset instanceof Asset) {
          Shell shell = parent.getShell();
          Long assetId = ((Asset) asset).getId();
          int quantity = 1;
          long price = 0;

          new WizardDialog(shell, new PlaceBidOrderWizard(userService, nxt,
              assetId, quantity, price)).open();
        }
      }
    });

    MenuItem itemIssuer = new MenuItem(contextMenu, SWT.PUSH);
    itemIssuer.setText("Show Asset Issuer");
    itemIssuer.addSelectionListener(new SelectionAdapter() {

      @Override
      public void widgetSelected(SelectionEvent e) {
        IStructuredSelection selection = (IStructuredSelection) assetsViewer
            .getSelection();
        Object asset = selection.getFirstElement();
        if (asset instanceof Asset) {
          Long id = ((Asset) asset).getAccountId();
          if (id != null) {
            InspectAccountDialog.show(id, nxt, engine, userService, sync,
                ContactsService.getInstance());
          }
        }
      }
    });

  }

  @Inject
  @Optional
  private void onInitializationFinished(
      @UIEventTopic(INxtService.TOPIC_INITIALIZATION_FINISHED) int dummy) {
    if (assetsViewer != null && !assetsViewer.getControl().isDisposed()) {
      assetsViewer.refresh();
    }
  }

  @Inject
  @Optional
  public void partActivation(
      @UIEventTopic(UIEvents.UILifeCycle.ACTIVATE) Event event) {
    if (assetsViewer != null && !assetsViewer.getControl().isDisposed()) {
      assetsViewer.refresh();
    }
  }

  @Inject
  @Optional
  private void onAssetSelected(
      @UIEventTopic(IAssetExchange.TOPIC_ASSET_SELECTED) Asset asset) {
    if (assetsViewer != null && !assetsViewer.getControl().isDisposed()) {
      IStructuredSelection selection = (IStructuredSelection) assetsViewer
          .getSelection();
      Object selectedAsset = selection.getFirstElement();
      if (selectedAsset instanceof Asset && asset instanceof Asset) {
        if (!selectedAsset.equals(asset)) {
          assetsViewer.setSelection(new StructuredSelection(asset));
        }
      }
    }
  }

}