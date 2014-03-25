package com.dgex.offspring.application.ui.home;

import java.awt.BasicStroke;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Hour;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

import com.dgex.offspring.dataprovider.service.IDataProviderListener;
import com.dgex.offspring.providers.dgex.DGEXCurrentRateProvider;
import com.dgex.offspring.providers.service.Currencies;
import com.dgex.offspring.providers.service.ICurrency;
import com.dgex.offspring.providers.service.IRate;
import com.dgex.offspring.providers.service.IRateProvider;

// CategoryDataset dataset = new CategoryDataset();
//
// Multi axes chart (price + volume)
// https://code.google.com/p/grape/source/browse/trunk/code/dealWithExcel/src/shai/jfreechart/PriceVolumeDemo1.java?r=50
//
// Time series seems to show price and time
// https://github.com/anilbharadia/jFreeChart-Examples/blob/master/src/TimeSeriesDemo1.java
//
// Add a moving avarage line to time series
// http://www.java2s.com/Code/Java/Chart/JFreeChartTimeSeriesDemo8.htm
//
// Stacked Area chart (two area charts)
// https://code.google.com/p/socr/source/browse/trunk/SOCR2.0/src/edu/ucla/stat/SOCR/chart/demo/StackedXYAreaChartDemo1.java?spec=svn70&r=67

public class OffspringChartComposite extends Composite {

  private static Logger logger = Logger
      .getLogger(OffspringChartComposite.class);

  private IRateProvider rateProvider;
  private IDataProviderListener listener;
  private JFreeChart chart;
  private final ChartComposite chartComposite;
  private final UISynchronize sync;
  private ICurrency base;
  private ICurrency quote;

  public OffspringChartComposite(Composite parent, int style, UISynchronize sync) {
    super(parent, style);
    setLayout(new FillLayout());

    this.sync = sync;
    chartComposite = new ChartComposite(this, SWT.BORDER, null, true);
    chartComposite.setDisplayToolTips(true);
    chartComposite.setHorizontalAxisTrace(true);
    chartComposite.setVerticalAxisTrace(true);
  }

  public JFreeChart getChart() {
    return chart;
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

  public void refresh(final IRateProvider provider, final ICurrency base,
      final ICurrency quote) {
    if (listener != null && rateProvider != null) {
      rateProvider.removeDataProviderListener(listener);
      listener = null;
    }

    this.rateProvider = provider;
    this.base = base;
    this.quote = quote;

    String temp = createChartTitle();

    final String title = temp.isEmpty() ? (provider.getLabel() + " "
        + base.getId() + "/" + quote.getId()) : temp;

    chart = createChart(title);
    chartComposite.setChart(chart);
    chart.fireChartChanged();

    listener = new IDataProviderListener() {

      @Override
      public void update() {
        sync.asyncExec(new Runnable() {

          @Override
          public void run() {
            String temp = createChartTitle();
            final String title = temp.isEmpty() ? "DGEX.com BTC / NXT 3hr moving average"
                : temp;

            if (chartComposite != null && !chartComposite.isDisposed()) {
              chart = createChart(title);
              chartComposite.setChart(chart);
              chart.fireChartChanged();
            }
          }
        });
      }
    };
    rateProvider.addDataProviderListener(listener);
  }

  public void setTitle(String title) {
    chart.setTitle(title);
    chart.fireChartChanged();
  }

  private JFreeChart createChart(String title) {
    XYDataset priceDataset = createPriceDataset();
    XYDataset volumeDataset = createVolumeDataset();

    JFreeChart jfreechart = ChartFactory.createTimeSeriesChart(title,        // title
        "Date",       // x title
        "Price",      // y title
        priceDataset, // dataset
        false,        // legend
        true,         // tooltips
        false);

    XYPlot xyplot = (XYPlot) jfreechart.getPlot();

    NumberAxis numberaxis = (NumberAxis) xyplot.getRangeAxis();
    numberaxis.setLowerMargin(0.40000000000000002D);

    DecimalFormat decimalformat = new DecimalFormat(quote.getDecimalFormat());
    numberaxis.setNumberFormatOverride(decimalformat);

    if (volumeDataset != null) {
      XYItemRenderer xyitemrenderer = xyplot.getRenderer();
      xyitemrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
          "{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"),
          new DecimalFormat("0.00")));
      xyitemrenderer.setSeriesStroke(0, new BasicStroke(3));

      NumberAxis numberaxis1 = new NumberAxis("Volume");
      numberaxis1.setUpperMargin(1.0D);

      xyplot.setRangeAxis(1, numberaxis1);
      xyplot.setDataset(1, volumeDataset);
      xyplot.setRangeAxis(1, numberaxis1);
      xyplot.mapDatasetToRangeAxis(1, 1);

      XYBarRenderer xybarrenderer = new XYBarRenderer(0.20000000000000001D);
      xybarrenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(
          "{0}: ({1}, {2})", new SimpleDateFormat("d-MMM-yyyy"),
          new DecimalFormat("0,000.00")));
      xyplot.setRenderer(1, xybarrenderer);
    }

    return jfreechart;
  }

  private XYDataset createPriceDataset() {
    List<IRate> rates = rateProvider.getRates(base, quote);
    TimeSeries series = new TimeSeries("Price");
    if (rates != null) {
      for (IRate rate : rates) {
        Date date = new Date(rate.getTimestamp());
        series.addOrUpdate(new Hour(date), rate.getPrice());
      }
    }
    TimeSeriesCollection dataset = new TimeSeriesCollection(series);
    dataset.setDomainIsPointsInTime(true);
    return dataset;
  }

  private IntervalXYDataset createVolumeDataset() {
    List<IRate> rates = rateProvider.getRates(base, quote);
    TimeSeries series = new TimeSeries("Volume");
    boolean non_zero_found = false;
    if (rates != null) {
      for (IRate rate : rates) {
        Date date = new Date(rate.getTimestamp());
        double vol = rate.getTotalVol();
        series.addOrUpdate(new Hour(date), vol);
        if (vol > 0)
          non_zero_found = true;
      }
    }
    if (non_zero_found == false)
      return null;

    TimeSeriesCollection dataset = new TimeSeriesCollection(series);
    dataset.setDomainIsPointsInTime(true);
    return dataset;
  }
}
