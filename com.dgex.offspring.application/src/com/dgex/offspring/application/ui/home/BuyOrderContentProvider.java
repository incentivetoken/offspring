package com.dgex.offspring.application.ui.home;

import java.util.List;

import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.dgex.offspring.dataprovider.service.IDataProviderListener;
import com.dgex.offspring.providers.service.IBuyOrder;

public class BuyOrderContentProvider implements IStructuredContentProvider,
    IDataProviderListener {

  private BuyOrderTableInput input = null;
  private Viewer viewer = null;
  private final UISynchronize sync;

  public BuyOrderContentProvider(UISynchronize sync) {
    this.sync = sync;
  }

  @Override
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    this.viewer = null;

    /* Remove listener from previous input */
    if (this.input != null) {
      this.input.getProvider().removeDataProviderListener(this);
    }
    this.input = (BuyOrderTableInput) newInput;
    if (this.input != null) {
      this.input.getProvider().addDataProviderListener(this);
    }
    this.viewer = viewer;
  }

  @Override
  public Object[] getElements(Object inputElement) {
    if (input == null) { return new Object[0]; }
    List<IBuyOrder> orders = input.getProvider().getBuyOrders(
        input.getPair().getBase(), input.getPair().getQuote());
    if (orders == null) { return new Object[0]; }

    /* Calculate cumulatives */
    calcRunningTotal(orders);

    return orders.toArray(new Object[orders.size()]);
  }

  private void calcRunningTotal(List<IBuyOrder> orders) {
    double total = 0;
    for (IBuyOrder order : orders) {
      total += order.getPrice() * order.getQuantity();
      order.setCumulative(total);
    }
  }

  @Override
  public void dispose() {
    input = null;
  }

  @Override
  public void update() {
    sync.asyncExec(new Runnable() {

      @Override
      public void run() {
        if (viewer != null && viewer.getControl() != null
            && !viewer.getControl().isDisposed())
          viewer.refresh();
      }
    });
  }
}
