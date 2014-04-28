package com.dgex.offspring.trader.charts;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

import nxt.Asset;
import nxt.Constants;
import nxt.Trade;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.experimental.chart.swt.ChartComposite;

public class AEChartComposite extends Composite {

  private static Logger logger = Logger.getLogger(AEChartComposite.class);

  private JFreeChart chart;
  private final ChartComposite chartComposite;
  private Asset asset;

  public AEChartComposite(Composite parent, int style) {
    super(parent, style);
    setLayout(new FillLayout());

    chartComposite = new ChartComposite(this, SWT.BORDER, null, true);
    chartComposite.setDisplayToolTips(true);
    chartComposite.setHorizontalAxisTrace(true);
    chartComposite.setVerticalAxisTrace(true);
  }

  public JFreeChart getChart() {
    return chart;
  }

  public void refresh(Asset asset) {
    this.asset = asset;
    String title = asset.getName() + " / NXT";
    chart = createChart(asset, title);
    chartComposite.setChart(chart);
    chart.fireChartChanged();
  }

  private JFreeChart createChart(Asset asset, String title) {
    XYDataset priceDataset = createPriceDataset();
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

    StringBuilder format = new StringBuilder();
    format.append("#");
    int count = asset.getDecimals();
    if (count > 0) {
      format.append(".");
      for (int i = 0; i < count; i++) {
        format.append("#");
      }
    }

    DecimalFormat decimalformat = new DecimalFormat(format.toString());
    numberaxis.setNumberFormatOverride(decimalformat);

    return jfreechart;
  }

  private XYDataset createPriceDataset() {
    TimeSeries series = new TimeSeries("Price");
    List<Trade> trades = Trade.getTrades(asset.getId());
    for (Trade trade : trades) {
      Date date = new Date(((trade.getTimestamp()) * 1000l)
          + (Constants.EPOCH_BEGINNING - 500L));
      series.addOrUpdate(new Minute(date), (double) trade.getPriceNQT()
          / Constants.ONE_NXT);
    }
    TimeSeriesCollection dataset = new TimeSeriesCollection(series);
    dataset.setDomainIsPointsInTime(true);
    return dataset;
  }
}
