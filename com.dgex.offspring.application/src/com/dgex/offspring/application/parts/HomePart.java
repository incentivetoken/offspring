package com.dgex.offspring.application.parts;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.jfree.chart.JFreeChart;

import com.dgex.offspring.application.ui.home.BuyOrderTable;
import com.dgex.offspring.application.ui.home.BuyOrderTableInput;
import com.dgex.offspring.application.ui.home.BuyOrderTableViewer;
import com.dgex.offspring.application.ui.home.OffspringChartComposite;
import com.dgex.offspring.application.ui.home.SellOrderTable;
import com.dgex.offspring.application.ui.home.SellOrderTableInput;
import com.dgex.offspring.application.ui.home.SellOrderTableViewer;
import com.dgex.offspring.dataprovider.service.IDataProviderListener;
import com.dgex.offspring.providers.dgex.DGEX3HMovingAvarage;
import com.dgex.offspring.providers.dgex.DGEXBuyOrderProvider;
import com.dgex.offspring.providers.dgex.DGEXCurrentRateProvider;
import com.dgex.offspring.providers.dgex.DGEXSellOrderProvider;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.IRate;

public class HomePart {

  private static Logger logger = Logger.getLogger(HomePart.class);

  private Composite mainComposite;

  private BuyOrderTableViewer buyOrderTableViewer;

  private SellOrderTableViewer sellOrderTableViewer;

  @PostConstruct
  public void postConstruct(Composite parent, final UISynchronize sync) {
    parent.setLayout(new FillLayout());
    parent.addControlListener(new ControlAdapter() {

      @Override
      public void controlResized(final ControlEvent e) {
        sync.asyncExec(new Runnable() {

          @Override
          public void run() {
            calculateSizes();
          }
        });
      }
    });

    mainComposite = new Composite(parent, SWT.NONE);
    GridLayoutFactory.fillDefaults().numColumns(2).applyTo(mainComposite);

    final OffspringChartComposite composite = new OffspringChartComposite(
        mainComposite, SWT.NONE, sync);

    GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).hint(600, 300)
        .span(2, 1).applyTo(composite);

    composite.refresh(DGEX3HMovingAvarage.getInstance(), Currencies.BTC,
        Currencies.NXT);

    DGEXCurrentRateProvider.getInstance().addDataProviderListener(
        new IDataProviderListener() {

          @Override
          public void update() {
            sync.asyncExec(new Runnable() {

              @Override
              public void run() {
                if (composite != null && !composite.isDisposed()) {
                  JFreeChart chart = composite.getChart();
                  if (chart != null) {
                    chart.setTitle(createChartTitle());
                    chart.fireChartChanged();
                  }
                }
              }
            });
          }
        });
    DGEXCurrentRateProvider.getInstance().bump();

    Composite buyOrderComposite = new Composite(mainComposite, SWT.NONE);
    Composite sellOrderComposite = new Composite(mainComposite, SWT.NONE);

    GridDataFactory.fillDefaults().grab(true, true).align(SWT.END, SWT.FILL)
        .applyTo(buyOrderComposite);
    GridDataFactory.fillDefaults().grab(true, true)
        .align(SWT.BEGINNING, SWT.FILL).applyTo(sellOrderComposite);

    buyOrderComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1)
        .create());
    sellOrderComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(1)
        .create());

    Label label = new Label(buyOrderComposite, SWT.NONE);
    label.setText("Buy Orders");
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);

    label = new Label(sellOrderComposite, SWT.NONE);
    label.setText("Sell Orders");
    GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(label);

    buyOrderTableViewer = new BuyOrderTableViewer(buyOrderComposite, sync);
    sellOrderTableViewer = new SellOrderTableViewer(sellOrderComposite, sync);

    GridDataFactory.fillDefaults().grab(false, true)
        .align(SWT.CENTER, SWT.FILL).applyTo(buyOrderTableViewer.getControl());
    GridDataFactory.fillDefaults().grab(false, true)
        .align(SWT.CENTER, SWT.FILL).applyTo(sellOrderTableViewer.getControl());

    buyOrderTableViewer.setInput(new BuyOrderTableInput(DGEXBuyOrderProvider
        .getInstance(), Currencies.NXT.getPair(Currencies.BTC)));

    sellOrderTableViewer.setInput(new SellOrderTableInput(DGEXSellOrderProvider
        .getInstance(), Currencies.NXT.getPair(Currencies.BTC)));

  }

  private String createChartTitle() {
    String title = "";
    List<IRate> rates = DGEXCurrentRateProvider.getInstance().getRates(
        Currencies.BTC, Currencies.NXT);
    if (rates.size() > 0) {
      IRate rate = rates.get(0);
      String price = new DecimalFormat("0.00000000").format(rate.getPrice());
      title = "DGEX.com BTC / NXT 3hr moving average\n" + "Last: " + price
          + " " + new Date().toLocaleString();
    }
    return title;
  }

  private void calculateSizes() {
    GC gc = new GC(mainComposite);

    /* Calculate column widths */
    for (int i = 0; i < BuyOrderTable.getColumns().length; i++) {
      int id = BuyOrderTable.getColumns()[i];
      String text = BuyOrderTable.getColumnTextExtent(id);
      buyOrderTableViewer.getTable().getColumns()[i].setWidth(gc
          .textExtent(text).x);
    }
    for (int i = 0; i < SellOrderTable.getColumns().length; i++) {
      int id = SellOrderTable.getColumns()[i];
      String text = SellOrderTable.getColumnTextExtent(id);
      sellOrderTableViewer.getTable().getColumns()[i].setWidth(gc
          .textExtent(text).x);
    }

    gc.dispose();
    mainComposite.layout();

  }

  // private void calculateBuyOrderColumnWidths() {
  // GC gc = new GC(buyOrderTableViewer.getTable());
  // try {
  // buyOrderTableViewer.getTable().getColumn(0)
  // .setWidth(gc.textExtent("0.00000000").x);
  // FontMetrics fm = gc.getFontMetrics();
  // // fm.
  //
  // }
  // finally {
  // gc.dispose();
  // }
  //
  // buyOrderTableViewer.getTable().getColumn(0).setWidth(width);
  //
  // GC gc = new GC(labels[i]);
  // FontMetrics fm = gc.getFontMetrics();
  // int charWidth = fm.getAverageCharWidth();
  // gc.dispose();
  //
  // }
  //
  // private void calcWidth(GC gc, TableColumn column, String text) {
  //
  // }

  // composite = new OffspringChartComposite(mainComposite, SWT.NONE, sync);
  // GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
  // composite.refresh(PerMinute24HSliding.getInstance(), Currencies.USD,
  // Currencies.BTC);
  //
  // composite = new OffspringChartComposite(mainComposite, SWT.NONE, sync);
  // GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
  // composite.refresh(PerMinute24HSliding.getInstance(), Currencies.EUR,
  // Currencies.BTC);
  //
  // composite = new OffspringChartComposite(mainComposite, SWT.NONE, sync);
  // GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
  // composite.refresh(PerHourMonthlyUSD.getInstance(), Currencies.USD,
  // Currencies.BTC);
  //
  // composite = new OffspringChartComposite(mainComposite, SWT.NONE, sync);
  // GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
  // composite.refresh(PerHourMonthlyEUR.getInstance(), Currencies.EUR,
  // Currencies.BTC);
  // composite = new OffspringChartComposite(mainComposite, SWT.NONE, sync);
  // GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
  // composite.refresh(PerHourMonthlyCNY.getInstance(), Currencies.CNY,
  // Currencies.BTC);
}